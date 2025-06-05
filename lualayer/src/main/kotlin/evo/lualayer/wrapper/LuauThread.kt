package evo.lualayer.wrapper

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.absRef
import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaState
import net.hollowcube.luau.LuaType

class LuauThread( // TODO: look into pushThread()
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

    val ref: Int

    init {
        require(parent.type(-1) == LuaType.THREAD) {
            "Parent Lua state must be a thread, but got ${parent.type(-1)}"
        }
        ref = parent.absRef(-1)
    }

    override fun close() {
        cleanup()
        parent.run {
            //getref(ref) TODO: test if this is needed
            unref(ref)
            pop(1)
        }
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