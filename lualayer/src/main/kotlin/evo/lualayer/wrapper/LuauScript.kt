package evo.lualayer.wrapper

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaState
import net.hollowcube.luau.LuaStatus
import net.hollowcube.luau.LuaType

/**
 * Represents a Lua script loaded into a Lua state.
 *
 * @property lua The Lua state instance where the script is loaded.
 * @property config The configuration for the Lua environment.
 * @constructor Loads the provided bytecode into the Lua state with the given name.
 * @param name The name of the script.
 * @param bytecode The compiled bytecode of the script.
 */
class LuauScript(val state: State, name: String, bytecode: ByteArray, val config: LuauConfig) {

    val lua: LuaState = state.lua

    val ref: Int
    var args: Int = 0
        private set
    var results: Int = 0

    init {
        val hash = name.hashCode() + bytecode.contentHashCode()
        if (state.scriptRefs.containsKey(hash)) {
            if (config.debug) log("Script with hash $hash already loaded, reusing existing reference", LogType.DEBUG)
            ref = state.scriptRefs[hash] ?: throw IllegalStateException("Script reference not found")
        } else {
            require(state.lua.checkStack(2)) {
                throw RuntimeException("Lua stack overflow: insufficient stack space to load script")
            }
            state.lua.load(name, bytecode)
            ref = state.createRef()
            state.scriptRefs[hash] = ref
        }
    }

    /**
     * Executes the Lua script in the Lua state.
     *
     * @return The status of the script execution, either `LuaStatus.OK` or `LuaStatus.ERRRUN`.
     */
    fun run(): LuaStatus = try { // TODO: return values
        if (config.debug) log("Running script with ref: <bold>$ref", LogType.DEBUG)
        lua.getref(ref)
        lua.pcall(0, 0)
        LuaStatus.OK
    } catch (e: Exception) {
        log("Error running script: ${lua.toString(-1)}", LogType.ERROR)
        lua.pop(1)
        if (!e.message.isNullOrEmpty()) {
            e.printStackTrace()
        }
        LuaStatus.ERRRUN // TODO: more specific error handling
    } finally {
        //lua.pop(1) // TODO: manage this automatically, rn we can have 8k-ish scripts loaded at once, if we pop we will lose the reference
    }
    /**
    * Runs the Lua script and pops the function from the stack after execution.
    * */
    fun runOnce(): LuaStatus { // TODO: return values
        return run().also {
            require(lua.type(-1) == LuaType.FUNCTION) {
                "Expected a function on the stack, got: ${lua.typeName(-1)}"
            }
            state.scriptRefs.remove(ref)
            lua.pop(1)
        }
    }
}