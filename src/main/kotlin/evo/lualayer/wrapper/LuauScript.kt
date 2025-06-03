package evo.evo.lualayer.wrapper

import net.hollowcube.luau.LuaState
import net.hollowcube.luau.LuaStatus

class LuauScript(override val lua: LuaState, name: String, bytecode: ByteArray) : LuaStateWrapper {
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