package evo.lualayer.wrapper

import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaState

class LuauThread(override var config: LuauConfig, private val parent: LuaState) : State(config = config, lua = parent.newThread()) {

    override fun close() {
        parent.pop(1)
    }
}