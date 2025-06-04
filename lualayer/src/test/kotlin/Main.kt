import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.annotations.LuauFunction
import evo.lualayer.generated.SyntheticLuauLibs
import evo.lualayer.setup.LuauConfig
import evo.lualayer.wrapper.State

val config = LuauConfig(
    paths = setOf(
        "lualayer/src/test/resources"
    ),
    libs = SyntheticLuauLibs.ALL  // or use setOf(SyntheticLuauLibs.MISC, SyntheticLuauLibs.FOO) if you want specific libs
)

@LuauFunction(lib = "misc")
fun foo(bool: Boolean): String {
    log("Foo function called with $bool", LogType.DEBUG)
    return "Foo returned: $bool"
}

@LuauFunction // not specifying lib means it will be added to the global namespace
fun fibonacci(n: Int): Int {
    return if (n <= 1) n else fibonacci(n - 1) + fibonacci(n - 2)
}

fun main(args: Array<String>) {
    val state = State(config = config).addLibs(config.libs)

    try {
        val thread = state.newThread() // TODO: Sandbox scope dsl maybe
        state.sandbox()
        thread.sandbox()
        val test = """
            print(misc.foo(false)) -- Should print: Foo returned: false
            print(fibonacci(15)) -- Should print: 610
        """.trimIndent()
        val compiled = config.compiler.compile(test)
        val script = thread.load("test.luau", compiled)
        log("status:" + script.run(), LogType.RUNTIME)

        thread.close()
    } finally {
        state.close()
    }
}