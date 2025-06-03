import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
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
        thread.sandbox()
        val script = thread.loadFromPaths("needy.luau")
        val script1 = thread.loadFromPaths("requireable.luau")
        log("status0:" + script.run(), LogType.RUNTIME)
        log("status1:" + script1.run(), LogType.RUNTIME)

        thread.close()
    } finally {
        state.close();
    }
}