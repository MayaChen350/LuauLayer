package evo.evo

import evo.State
import net.hollowcube.luau.compiler.LuauCompiler

class LuauConfig(
    val paths: Set<String> = setOf("run/scripts"),
    val compiler: LuauCompiler = Compilers.DEBUG,
) {
    val state = State(config = this)
}