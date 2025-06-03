import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import evo.lualayer.setup.LuauConfig
import evo.lualayer.wrapper.LuauLib
import evo.lualayer.wrapper.LuauThread
import evo.lualayer.wrapper.State
import net.hollowcube.luau.LuaFunc
import net.hollowcube.luau.LuaState

val config = LuauConfig(
    paths = setOf(
        "src/test/resources"
    )
)

fun main(args: Array<String>) {
    val state = State(config = config).addLibs(config.libs)

    try {
        val thread = LuauThread(state) // TODO: Sandbox scope dsl maybe
        state.sandbox()
        thread.sandbox()
        val script1 = thread.load("requireable.luau")
        val script = thread.load("needy.luau")
        log("status0:" + script.run(), LogType.RUNTIME)
        log("status1:" + script1.run(), LogType.RUNTIME)

        thread.close()
    } finally {
        state.close()
    }
}