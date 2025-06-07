package evo.lualayer

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.lifecycle.LifecycleState
import evo.lualayer.lifecycle.conditionalAwait
import evo.lualayer.wrapper.LuauThread
import evo.lualayer.wrapper.State
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import net.hollowcube.luau.LuaState
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration

fun LuaState.absRef(i: Int = -1): Int {
    return ref(absIndex(i))
}

fun LuaState.dumpStack() {
    log(buildString {
        append("Lua stack dump:\n")
        for (i in 1..top) {
            append("          [$i]: ${typeName(i)}")
            if (isString(i)) {
                append(" = \"${toString(i)}\"")
            } else if (isNumber(i)) {
                append(" = ${toNumber(i)}")
            } else if (isBoolean(i)) {
                append(" = ${toBoolean(i)}")
            } else if (isNil(i)) {
                append(" = nil")
            }
            append("\n")
        }
    }, LogType.DEBUG)
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
suspend fun <R> State.spawn(sandbox: Boolean = true, block: (LuauThread) -> R): LuauThread {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    if (!sandboxed && sandbox) sandbox()
    LuauThread(this@spawn).use { thread ->
        if (sandbox) thread.sandbox()
        block(thread)
        this@spawn.lifecycle.conditionalAwait { it == LifecycleState.STOPPED }
        return thread
    }
}

fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}