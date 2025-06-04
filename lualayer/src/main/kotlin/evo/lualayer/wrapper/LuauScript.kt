package evo.lualayer.wrapper

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
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
class LuauScript(val state: WrappedLuauState, name: String, bytecode: ByteArray, val config: LuauConfig) {

    val lua: LuaState = state.lua

    val ref: Int
    var args: Int = 0
        private set
    var results: Int = 0

    init {
        state.lua.load(name, bytecode)
        ref = state.createRef()
    }

    /**
     * Executes the Lua script in the Lua state.
     *
     * @return The status of the script execution, either `LuaStatus.OK` or `LuaStatus.ERRRUN`.
     */
    fun run(): LuaStatus = try {
        log("Running script with ref: <bold>$ref", LogType.DEBUG)
        state.invoke(this)
        LuaStatus.OK
    } catch (e: Exception) {
        log("Error running script: ${lua.toString(-1)}", LogType.ERROR)
        if (!e.message.isNullOrEmpty()) {
            e.printStackTrace()
        }
        LuaStatus.ERRRUN // TODO: more specific error handling
    }
}