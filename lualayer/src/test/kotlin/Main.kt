import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.LOOM
import evo.lualayer.annotations.LuauFunction
import evo.lualayer.generated.SyntheticLuauLibs
import evo.lualayer.lifecycle.BingBongEvent
import evo.lualayer.lifecycle.BloopEvent
import evo.lualayer.lifecycle.LifecycleState
import evo.lualayer.setup.LuauConfig
import evo.lualayer.spawn
import evo.lualayer.tickerFlow
import evo.lualayer.wrapper.State
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration.Companion.seconds

val config = LuauConfig(
    paths = setOf(
        "lualayer/src/test/resources"
    ),
    libs = SyntheticLuauLibs.ALL,  // or use setOf(SyntheticLuauLibs.MISC, SyntheticLuauLibs.FOO) if you want specific libs
    debug = false
)

@LuauFunction(lib = "misc")
fun foo(bool: Boolean): String {
    return "Foo: $bool"
}

@LuauFunction // not specifying a namespace means it will be added to the global namespace
fun fibonacci(n: Int): Int {
    return if (n <= 1) n else fibonacci(n - 1) + fibonacci(n - 2)
}

// can also be defined in a separate (companion) object
object Object {
    @LuauFunction(lib = "veclib")
    fun double(a: Float): Float {
        return a * 2
    }
}

@LuauFunction
fun print(msg: String) {
    log(msg, LogType.USER_ACTION)
}

fun main(args: Array<String>) {
    val state = State(config = config)

    runBlocking {
        var count = 0
        tickerFlow(1.seconds)
            .onEach {
                if (count != 0) state.dispatchEvent(BingBongEvent("Bing bong"))
                if (count != 0) state.dispatchEvent(BloopEvent("Bloop!"))
                if (count++ > 2) {
                    log("Stopping Lua state", LogType.DEBUG)
                    state.lifecycle.value = LifecycleState.STOPPED
                }
            }
            .launchIn(CoroutineScope(SupervisorJob() + Dispatchers.LOOM))

        state.spawn { thread ->
            val test = """
                          print("Hello from 1!")
                          function events.bing_bong(msg)
                            return msg .. " @ " .. "BING"
                          end
                      """.trimIndent()
            val compiled = config.compiler.compile(test)

            val script = thread.load("test.luau", compiled)
            script.run()
        }
        state.spawn { thread -> }
        state.queue.awaitAll()
    }
}


