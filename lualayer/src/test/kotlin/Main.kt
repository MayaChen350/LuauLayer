import evo.lualayer.annotations.LuauFunction
import evo.lualayer.generated.SyntheticLuauLibs
import evo.lualayer.lifecycle.ChatMessageEvent
import evo.lualayer.lifecycle.LifecycleState
import evo.lualayer.setup.LuauConfig
import evo.lualayer.spawn
import evo.lualayer.tickerFlow
import evo.lualayer.wrapper.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
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

fun main(args: Array<String>) {
    val state = State(config = config)

    runBlocking {
        var count = 0
        tickerFlow(2.seconds)
            .onEach {
                state.callEvent(ChatMessageEvent("Bing bong"))
                if (count++ > 10) {
                    state.lifecycle.value = LifecycleState.STOPPED
                }
            }
            .launchIn(CoroutineScope(SupervisorJob() + Dispatchers.Default))

        state.spawn(true) { thread ->
            val test = """
                          print("Hello from Lua!")
                          function events.chat_message_event(msg)
                            return msg .. " @ " .. os.date("%H:%M:%S")
                          end
                      """.trimIndent()
            val compiled = config.compiler.compile(test)

            val script = thread.load("test.luau", compiled)
            script.run()
        }
    }
}

