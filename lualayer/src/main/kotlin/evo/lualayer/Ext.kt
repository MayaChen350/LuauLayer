package evo.lualayer

import evo.lualayer.wrapper.State
import net.hollowcube.luau.LuaState

fun LuaState.absRef(i: Int = -1): Int {
    return ref(absIndex(i))
}

fun <T : State, R> T.runSandboxed(block: (State) -> R): R {
    try {
        sandbox()
        return block(this)
    } finally {
        close()
    }
}