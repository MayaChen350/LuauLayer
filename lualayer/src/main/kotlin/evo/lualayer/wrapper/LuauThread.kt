package evo.lualayer.wrapper

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import net.hollowcube.luau.LuaState
import net.hollowcube.luau.LuaType
import java.lang.ref.WeakReference

class LuauThread(val parent: State): State(parent.config, parent.lua) {

    override val lua: LuaState = parent.lua.newThread()
    override val lifecycle = parent.lifecycle
    val ref: Int

    init {
        require(parent.lua.type(-1) == LuaType.THREAD) {
            "Object on stack must be a thread, got: ${parent.lua.typeName(-1)}"
        }
        ref = parent.lua.ref(-1)
        parent.threads.add(WeakReference(this))
    }

    override fun close() {
        lua.top = 0
        parent.lua.run {
            getref(ref)
            unref(ref)
            pop(2)
        }
    }

    override fun initialize() = Unit /* no-op */

    /**
     * Sandbox this thread (think it just sets __G and everything inside it to read-only)
     */
    override fun sandbox() {
        if (!sandboxed) {
            lua.sandboxThread()
            sandboxed = true
            lua.getGlobal("events")
            lua.setReadOnly(-1, false)
            lua.pop(1)
        } else {
            log("Thread is already sandboxed", LogType.WARNING)
        }
    }

    override fun toString(): String {
        return "LuauThread#${this.hashCode().toString(16)}(parent=$parent) "
    }
}