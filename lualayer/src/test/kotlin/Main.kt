import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.annotations.LuauFunction
import evo.lualayer.generated.SyntheticLuauLibs
import evo.lualayer.setup.LuauConfig
import evo.lualayer.runSandboxed
import evo.lualayer.wrapper.State
import net.hollowcube.luau.internal.vm.lua_h

val config = LuauConfig(
    paths = setOf(
        "lualayer/src/test/resources"
    ),
    libs = SyntheticLuauLibs.ALL,  // or use setOf(SyntheticLuauLibs.MISC, SyntheticLuauLibs.FOO) if you want specific libs
    debug = false
)

@LuauFunction(lib = "misc")
fun foo(bool: Boolean): String {
    return "Foo returned: $bool"
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
    State(config = config).addLibs(config.libs).runSandboxed { state ->
        val test = """
                    fibonacci(2)
                """.trimIndent()
        val compiled = config.compiler.compile(test)

        state.newThread().runSandboxed { thread ->
            var i = 0
            try {
                repeat(10000) {
                    i = it
                    val script = thread.load("test$it.luau", compiled)
                    script.run()
                }
            } catch (e: Exception) {
                throw e
            } finally {
                log("last index: $i", LogType.INFORMATION)
            }
        }
    }
}

