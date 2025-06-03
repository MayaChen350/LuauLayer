package evo.evo.lualayer.wrapper

import net.hollowcube.luau.LuaState

class LuauThread(private val parent: LuaState) : State(lua = parent.newThread()) {

    override fun close() {
        parent.pop(1)
    }
}