import evo.lualayer.annotations.LuauFunction
import evo.lualayer.generated.SyntheticLuauLibs
import evo.lualayer.setup.LuauConfig
import evo.lualayer.runSandboxed
import evo.lualayer.wrapper.State

val config = LuauConfig(
    paths = setOf(
        "lualayer/src/test/resources"
    ),
    libs = SyntheticLuauLibs.ALL  // or use setOf(SyntheticLuauLibs.MISC, SyntheticLuauLibs.FOO) if you want specific libs
)

@LuauFunction(lib = "misc")
fun foo(bool: Boolean): String {
    return "Foo returned: $bool"
}

@LuauFunction // not specifying a namespace means it will be added to the global namespace
fun fibonacci(n: Int): Int {
    return if (n <= 1) n else fibonacci(n - 1) + fibonacci(n - 2)
}

// also can be defined in a separate (companion) object or
object Object {
    @LuauFunction(lib = "veclib")
    fun double(a: Float): Float {
        return a * 2
    }
}

fun main(args: Array<String>) {
    State(config = config).addLibs(config.libs).runSandboxed { state ->
        val test = """
                    misc.foo(false) -- Should print: Foo returned: false            
                    local l = fibonacci(15) * 10 -- Should print: 6100
                """.trimIndent()
        val compiled = config.compiler.compile(test)

        state.newThread().runSandboxed { thread ->
            val script = thread.load("test.luau", compiled)
            script.run()
        }
    }
}

