package evo.lualayer.wrapper

import evo.lualayer.absRef
import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaFunc
import net.hollowcube.luau.LuaState

/**
 * Interface representing a wrapper for a Lua state, providing utility methods
 * to interact with and manage the Lua environment.
 */
@Deprecated("Trying to get rid of this interface, use State instead")
interface WrappedLuauState {
    /**
     * The Lua state instance being wrapped.
     */
    val lua: LuaState

    /**
     * Configuration for the Lua environment.
     */
    val config: LuauConfig


    /**
     * Sandboxes the Lua state, restricting it from modifying the environment.
     */
    fun sandbox()

    /**
     * Calls a Lua function with the specified number of arguments and expected results.
     *
     * @param args The number of arguments to pass to the Lua function.
     * @param results The number of results expected from the Lua function.
     */
    fun pcall(args: Int, results: Int) = lua.pcall(args, results)

    fun invoke(script: LuauScript) { // TODO: abstract ref
        lua.getref(script.ref)
        pcall(script.args, script.results)
    }

    fun close()

    fun createRef(i: Int = -1): Int {
        return lua.absRef(i)
    }
}
