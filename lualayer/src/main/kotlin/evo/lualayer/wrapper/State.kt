package evo.lualayer.wrapper

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaFunc
import net.hollowcube.luau.LuaState
import java.io.File

/**
 * Represents a Lua state
 *
 * @property config The configuration for the Lua environment.
 * @property lua The Lua state instance being wrapped.
 */
open class State(
    override var config: LuauConfig,
    override val lua: LuaState = LuaState.newState(),
) : WrappedLuauState {
    var sandboxed = false
        internal set
    /**
     * A Lua function for requiring modules. Searches for the module in the global scope
     * or loads it from the configured paths.
     */
    val require = LuaFunc { state: LuaState -> // TODO: move out of here
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

    /**
     * Adds libraries to the Lua state and initializes them.
     *
     * @param libs A set of libraries to add.
     * @return The current `State` instance.
     */
    fun addLibs(libs: Set<LuauLib>): State {
        openLibs()
        addGlobal("require", require)
        libs.forEach { lib ->
            lua.registerLib(lib.name, lib.functions)
        }
        return this
    }

    override fun sandbox() {
        if (!sandboxed) {
            lua.sandbox()
            sandboxed = true
        } else {
            log("State is already sandboxed", LogType.WARNING)
        }
    }
}