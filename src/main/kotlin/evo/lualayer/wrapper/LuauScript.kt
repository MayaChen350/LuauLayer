package evo.lualayer.wrapper

import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaState
import net.hollowcube.luau.LuaStatus

class LuauScript(
    override val lua: LuaState,
    name: String,
    bytecode: ByteArray,
    override val config: LuauConfig
) : LuaStateWrapper {
    init {
        lua.load(name, bytecode)
    }

    fun run(): LuaStatus = try {
        pcall(0, 0)
        LuaStatus.OK
    } catch (e: Exception) {
        e.printStackTrace()
        LuaStatus.ERRRUN
    }
}