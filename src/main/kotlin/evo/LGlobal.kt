package evo.evo

import evo.State
import net.hollowcube.luau.compiler.LuauCompiler

class LuauConfig(
    val paths: Set<String>,
    val compiler: LuauCompiler = Compilers.DEBUG,
) {
    val state = State(config = this)

    companion object {
        val DEFAULT = LuauConfig(
            paths = setOf("run/scripts"),
            compiler = Compilers.DEBUG,
        )
    }
}