package evo.lualayer.wrapper

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaState
import net.hollowcube.luau.LuaStatus
import kotlin.properties.Delegates

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

    var ref by Delegates.notNull<Int>()
        private set
    var args: Int = 0
        private set
    var results: Int = 0

    init {
        lua.load(name, bytecode)
        ref = lua.ref(-1)
    }

    /**
     * Executes the Lua script in the Lua state.
     *
     * @return The status of the script execution, either `LuaStatus.OK` or `LuaStatus.ERRRUN`.
     */
    fun run(args: Int = 0, results: Int = 0): LuaStatus = try {
        log("Running script with ref: <bold>$ref", LogType.DEBUG)
        pcall(this)
        LuaStatus.OK
    } catch (e: Exception) {
        e.printStackTrace()
        LuaStatus.ERRRUN // TODO: more specific error handling
    }
}