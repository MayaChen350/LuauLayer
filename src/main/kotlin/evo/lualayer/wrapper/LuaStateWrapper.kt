package evo.lualayer.wrapper

import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaState

/**
 * Interface representing a wrapper for a Lua state, providing utility methods
 * to interact with and manage the Lua environment.
 */
interface LuaStateWrapper {
    /**
     * The Lua state instance being wrapped.
     */
    val lua: LuaState

    /**
     * Configuration for the Lua environment.
     */
    val config: LuauConfig

    /**
     * Opens standard libraries in the Lua state.
     */
    fun openLibs() = lua.openLibs()

    /**
     * Sandboxes the Lua state, restricting it from modifying the environment.
     */
    fun sandbox() = lua.sandbox()

    /**
     * Calls a Lua function with the specified number of arguments and expected results.
     *
     * @param args The number of arguments to pass to the Lua function.
     * @param results The number of results expected from the Lua function.
     */
    fun pcall(args: Int, results: Int) = lua.pcall(args, results)

    /**
     * Closes the Lua state, releasing any resources associated with it.
     */
    fun close() = lua.close()

    /**
     * Creates a new Lua thread associated with the current Lua state.
     *
     * @return A new instance of `LuauThread`.
     */
    fun newThread(): LuauThread = LuauThread(config = config, parent = lua)

    /**
     * Loads a Lua script into the Lua state.
     *
     * @param name The name of the script.
     * @param bytecode The compiled bytecode of the script.
     * @return A `LuauScript` instance representing the loaded script.
     */
    fun load(name: String, bytecode: ByteArray): LuauScript = LuauScript(
        lua = lua,
        name = name,
        bytecode = bytecode,
        config = config
    )
}
