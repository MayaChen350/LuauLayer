package evo

import net.hollowcube.luau.LuaState
import net.hollowcube.luau.LuaStatus

class LGlobal {
    val state = State()
}

interface LuaStateWrapper {
    val lua: LuaState

    fun openLibs() = lua.openLibs()
    fun sandbox() = lua.sandbox()
    fun pcall(args: Int, results: Int) = lua.pcall(args, results)
    fun close() = lua.close()
    fun newThread(): Thread = Thread(lua)
    fun load(name: String, bytecode: ByteArray): Script = Script(lua, name, bytecode)
}

class State(override val lua: LuaState = LuaState.newState()) : LuaStateWrapper

class Thread(private val parent: LuaState) : LuaStateWrapper {

    override val lua: LuaState = parent.newThread()

    override fun sandbox() {
        lua.sandbox()
    }

    override fun close() {
        parent.pop(1)
    }
}

class Script(override val lua: LuaState, name: String, bytecode: ByteArray) : LuaStateWrapper {
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