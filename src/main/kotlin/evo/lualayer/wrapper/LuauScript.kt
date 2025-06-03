package evo.lualayer.wrapper

import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaState
import net.hollowcube.luau.LuaStatus

/**
 * Represents a Lua script loaded into a Lua state.
 *
 * @property lua The Lua state instance where the script is loaded.
 * @property config The configuration for the Lua environment.
 * @constructor Loads the provided bytecode into the Lua state with the given name.
 * @param name The name of the script.
 * @param bytecode The compiled bytecode of the script.
 */
class LuauScript(
    override val lua: LuaState,
    name: String,
    bytecode: ByteArray,
    override val config: LuauConfig
) : LuaStateWrapper {
    init {
        lua.load(name, bytecode)
    }

    /**
     * Executes the Lua script in the Lua state.
     *
     * @return The status of the script execution, either `LuaStatus.OK` or `LuaStatus.ERRRUN`.
     */
    fun run(args: Int = 0, results: Int = 0): LuaStatus = try { // TODO: verify stack before pcall
        pcall(args, results)
        LuaStatus.OK
    } catch (e: Exception) {
        e.printStackTrace()
        LuaStatus.ERRRUN // TODO: more specific error handling
    }
}