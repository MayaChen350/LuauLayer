package evo.lualayer.wrapper

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaState

class LuauThread(
    override var config: LuauConfig,
    private val parent: LuaState
) : State(
    config = config,
    lua = parent.newThread()
) {
    constructor(parent: WrappedLuauState) : this(
        config = parent.config,
        parent = parent.lua
    )

    /**
     * Closes this Luau thread by popping one value from the parent Lua state's stack.
     */
    override fun close() { // TODO: Verify if this is the correct way to close a thread
        parent.pop(1)
    }

    /**
     * Sandbox this thread
     */
    override fun sandbox() { // Holy fuck this has caused so many jvm crashes
        if (!sandboxed) {
            lua.sandboxThread()
            sandboxed = true
        } else {
            log("Thread is already sandboxed", LogType.WARNING)
        }
    }
}