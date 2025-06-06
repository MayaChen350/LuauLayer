package evo.lualayer

import evo.lualayer.wrapper.LuauThread
import evo.lualayer.wrapper.State
import net.hollowcube.luau.LuaState
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun LuaState.absRef(i: Int = -1): Int {
    return ref(absIndex(i))
}

@OptIn(ExperimentalContracts::class)
inline fun <T : State, R> T.runSandboxed(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    try {
        sandbox()
        return block(this)
    } finally {
        close()
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <R> State.spawn(sandbox: Boolean = true, block: (LuauThread) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    if (!sandboxed && sandbox) sandbox()
    return LuauThread(this).use {
        if (sandbox) it.sandbox()
        block(it)
    }
}