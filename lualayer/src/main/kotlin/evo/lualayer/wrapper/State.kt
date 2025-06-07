package evo.lualayer.wrapper

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.dumpStack
import evo.lualayer.lifecycle.Event
import evo.lualayer.lifecycle.LifecycleState
import evo.lualayer.setup.LuauConfig
import kotlinx.coroutines.flow.MutableStateFlow
import net.hollowcube.luau.LuaFunc
import net.hollowcube.luau.LuaState
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a Lua state
 *
 * @property config The configuration for the Lua environment.
 * @property lua The Lua state instance being wrapped.
 */
open class State(
    override var config: LuauConfig,
    override val lua: LuaState = LuaState.newState(),
) : WrappedLuauState, AutoCloseable {
    var sandboxed = false
        internal set

    val lifecycle = MutableStateFlow(LifecycleState.RUNNING)
    // val eventHandlers = ConcurrentHashMap<String, Int>()

    fun setupEventTable() {
        lua.newMetaTable("events")
        lua.setGlobal("events")
        log("Initialized Lua state with global 'events' table", LogType.DEBUG)
    }

    fun callEvent(
        event: Event
    ): Any? {
        log("Calling event: $event", LogType.DEBUG)
        lua.getGlobal("events") // TODO: abstract globals
        if (lua.isNil(-1)) {
            lua.pop(1) // pop?
            log("Events table does not exist, cannot call event: $event", LogType.WARNING)
            return null
        }
        lua.getField(-1, event.name) // get events[eventName]
        if (lua.isNil(-1)) {
            lua.pop(2)
            log("Event $event does not exist, cannot call it", LogType.WARNING)
            return null
        }

        val args = event.apply(this)
        val results = 1
        pcall(args, results)

        lua.dumpStack()
        if (lua.isNil(-1)) {
            log("Event $event returned nil", LogType.WARNING)
            lua.pop(2)
            return null
        }
        val result = lua.toString(-1)
        lua.pop(1 + results)
        log("Event $event returned: $result", LogType.DEBUG)

        return result
    }

    /**
     * A Lua function for requiring modules. Searches for the module in the global scope
     * or loads it from the configured paths.
     */
    private val require = LuaFunc { state: LuaState -> // TODO: migrate to @LuaFunction
        val moduleName = state.checkStringArg(1)
        log("Requiring module: $moduleName", LogType.DEBUG)

        state.getGlobal(moduleName)
        if (!state.isNil(-1)) {
            log("Module $moduleName found in global scope", LogType.DEBUG)
            return@LuaFunc 1
        }
        state.pop(1)

        val file = config.paths.asSequence()
            .map { File(it, "$moduleName.luau") }
            .firstOrNull { it.exists() } ?: run {
                log("Module $moduleName not found in paths: ${config.paths.joinToString(", ")}", LogType.ERROR)
                state.pushNil()
                return@LuaFunc 1
            }

        val fileBytes = file.readBytes()
        val bytecode = config.compiler.compile(fileBytes)
        state.load(moduleName, bytecode)

        state.pcall(0, 1)
        if (state.isNil(-1)) {
            log("Error loading module $moduleName: ${state.toString(-1)}", LogType.ERROR)
            state.pushNil()
            return@LuaFunc 1
        }

        state.pushValue(-1)
        state.setGlobal(moduleName)

        1
    }

    init {
        initialize()
    }

    /**
     * Loads a Lua script from a file and compiles it into bytecode.
     *
     * @param file The file containing the Lua script.
     * @return A `LuauScript` instance representing the loaded script.
     */
    fun hotload(file: File): LuauScript {
        val fileBytes = file.readBytes()
        val bytecode = config.compiler.compile(fileBytes)

        return load(file.name, bytecode)
    }

    /**
     * Loads a Lua script from the configured paths.
     *
     * @param name The name of the script to load.
     * @return A `LuauScript` instance representing the loaded script.
     * @throws IllegalArgumentException If the file does not exist in the configured paths.
     */
    fun load(name: String): LuauScript {
        val file = File(config.paths.firstOrNull() ?: ".", name)
        if (!file.exists()) {
            throw IllegalArgumentException("File $name does not exist in paths: ${config.paths.joinToString(", ")}")
        }
        val bytecode = config.compiler.compile(file.readBytes())
        return load(name, bytecode)
    }

    protected open fun initialize() {
        setupEventTable()
        addLibs(config.libs)
    }

    /**
     * Adds libraries to the Lua state and initializes them.
     *
     * @param libs A set of libraries to add.
     * @return The current `State` instance.
     */
    open fun addLibs(libs: Set<LuauLib>): State {
        lua.openLibs()
        addGlobal("require", require)
        log("Adding libraries: ${libs.joinToString(", ") { it.name }}", LogType.DEBUG)
        libs.forEach { lib ->
            if (lib.functions.isEmpty()) {
                log("Library ${lib.name} has no functions to register", LogType.WARNING)
                return@forEach
            }
            if (lib.isGlobal) {
                lib.functions.forEach(::addGlobal)
            } else {
                lua.registerLib(lib.name, lib.functions)
            }
        }
        return this
    }

    private fun addGlobal(name: String, func: LuaFunc) {
        lua.pushCFunction(func, name)
        lua.setGlobal(name)
    }

    override fun sandbox() {
        if (!sandboxed) {
            lua.sandbox()
            sandboxed = true
        } else {
            log("State is already sandboxed", LogType.WARNING)
        }
    }

    internal val scriptRefs = ConcurrentHashMap<Int, Int>() // maybe I should generalize memory management
    protected var threadRefs = mutableSetOf<Int>()

    override fun close() {
        cleanupScripts()
        cleanupThreads()
        lua.close()
        log("Lua state closed", LogType.DEBUG)
    }

    protected fun cleanupScripts() {
        for ((_, ref) in scriptRefs) {
            lua.run {
                getref(ref)
                unref(ref)
                pop(2)
            }
        }
        scriptRefs.clear()
    }

    protected fun cleanupThreads() {
        for (ref in threadRefs) {
            lua.run {
                getref(ref)
                unref(ref)
                pop(2)
            }
        }
        threadRefs.clear()
    }

    /**
     * Creates a new Lua thread associated with the current Lua state.
     *
     * @return A new instance of `LuauThread`.
     */
    fun newThread(): LuauThread = LuauThread(this)

    fun load(name: String, bytecode: ByteArray): LuauScript {
        return LuauScript( // TODO: cache compiled scripts
            state = this,
            name = name,
            bytecode = bytecode,
            config = config
        )
    }
}