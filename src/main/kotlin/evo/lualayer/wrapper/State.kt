package evo.lualayer.wrapper

import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaFunc
import net.hollowcube.luau.LuaState
import java.io.File

/**
 * Represents a wrapper for a Lua state, providing methods to interact with the Lua environment.
 *
 * @property config The configuration for the Lua environment.
 * @property lua The Lua state instance being wrapped.
 */
open class State(
    override var config: LuauConfig,
    override val lua: LuaState = LuaState.newState(),
) : LuaStateWrapper {

    /**
     * A Lua function for requiring modules. Searches for the module in the global scope
     * or loads it from the configured paths.
     */
    val require = LuaFunc { state: LuaState ->
        with(state) {
            val moduleName = checkStringArg(1)
            println("Requiring module: $moduleName")

            getGlobal(moduleName)
            if (!isNil(-1)) {
                return@LuaFunc 1
            }
            pop(1)

            val file = config.paths.asSequence()
                .map { File(it, "$moduleName.luau") }
                .firstOrNull { it.exists() }
            if (file == null) {
                pushNil()
                return@LuaFunc 1
            }

            hotload(file)

            pcall(0, 1)
            if (isNil(-1)) {
                pushNil()
                return@LuaFunc 1
            }

            pushValue(-1)
            setGlobal(moduleName)
        }

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
    fun loadFromPaths(name: String): LuauScript {
        val file = File(config.paths.firstOrNull() ?: ".", name)
        if (!file.exists()) {
            throw IllegalArgumentException("File $name.luau does not exist in paths: ${config.paths.joinToString(", ")}")
        }
        val bytecode = config.compiler.compile(file.readBytes())
        return load(name, bytecode)
    }

    /**
     * Adds a global Lua function to the Lua state.
     *
     * @param name The name of the global function.
     * @param func The Lua function to add.
     */
    fun addGlobal(name: String, func: LuaFunc) {
        lua.pushCFunction(func, name)
        lua.setGlobal(name)
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
}