import evo.lualayer.setup.LuauConfig
import evo.lualayer.wrapper.State

val config = LuauConfig(
    paths = setOf(
        "src/test/resources"
    )
)

fun main(args: Array<String>) {
    val state = State(config = config).addLibs(config.libs)

    try {
        val thread = state.newThread()
        state.sandbox()
        thread.sandbox()
        val script = thread.loadFromPaths("needy.luau")
        thread.pcall(0, 0)
        //println("status:" + script.run(0, 1))

        thread.close()
    } finally {
        state.close();
    }
}