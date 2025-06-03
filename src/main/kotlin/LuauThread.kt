package evo

import net.hollowcube.luau.LuaState

class LuauThread(private val parent: LuaState) : State(lua = parent) {

    override val lua: LuaState = parent.newThread()

    override fun sandbox() {
        lua.sandbox()
    }

    override fun close() {
        parent.pop(1)
    }
}