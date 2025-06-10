package evo.lualayer

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.lifecycle.LifecycleState
import evo.lualayer.lifecycle.conditionalAwait
import evo.lualayer.wrapper.LuauThread
import evo.lualayer.wrapper.State
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.newCoroutineContext
import net.hollowcube.luau.LuaState
import java.util.concurrent.Executors
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

val Dispatchers.LOOM: CoroutineDispatcher
    get() = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher().limitedParallelism(1)

@OptIn(InternalCoroutinesApi::class)
suspend fun <R> State.spawn(sandbox: Boolean = true, block: (LuauThread) -> R) {
    if (lifecycle.value == LifecycleState.BUSY && queue.poll() != null) {
        log("Awaiting for previous thread to finish spawning in coroutine context: $this", LogType.DEBUG)
        lifecycle.conditionalAwait { it == LifecycleState.RUNNING }
        log("Previous thread finished spawning, continuing with new thread", LogType.DEBUG)
    }

    val new: Deferred<LifecycleState> = CoroutineScope(Dispatchers.LOOM.newCoroutineContext(SupervisorJob())).async {
        lifecycle.value = LifecycleState.BUSY
        val thread = LuauThread(this@spawn)
        if (sandbox) thread.sandbox()
        block(thread)
        lifecycle.value = LifecycleState.RUNNING
        lifecycle.conditionalAwait { it == LifecycleState.STOPPED }
    }
    queue.add(new)
}

fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}