import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.annotations.LuauFunction
import evo.lualayer.generated.SyntheticLuauLibs
import evo.lualayer.runSandboxed
import evo.lualayer.setup.LuauConfig
import evo.lualayer.spawn
import evo.lualayer.wrapper.State
import kotlin.time.measureTime

val config = LuauConfig(
    paths = setOf(
        "lualayer/src/test/resources"
    ),
    libs = SyntheticLuauLibs.ALL,  // or use setOf(SyntheticLuauLibs.MISC, SyntheticLuauLibs.FOO) if you want specific libs
    debug = true
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
    State(config = config).runSandboxed { state ->
        val test = """
                    fibonacci(11)
                """.trimIndent()
        val compiled = config.compiler.compile(test)

        var i = 0
        try {
            measureTime {
                state.spawn { thread ->
                    repeat(10000) {
                        i = it
                        val script = thread.load("test$it.luau", compiled)
                        script.run()
                    }
                }
            }.apply {
                log("Execution took: $this", LogType.INFORMATION)
            }
        } catch (e: Exception) {
            throw e
        } finally {
            log("last index: $i", LogType.INFORMATION)
        }
    }
}

