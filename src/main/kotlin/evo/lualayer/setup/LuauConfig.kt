package evo.lualayer.setup

import evo.lualayer.wrapper.LuauLib
import evo.lualayer.wrapper.State
import net.hollowcube.luau.compiler.LuauCompiler

class LuauConfig(
    val paths: Set<String> = setOf("run/scripts"),
    val compiler: LuauCompiler = Compilers.DEBUG,
    val libs: Set<LuauLib> = emptySet(),
) {
    init {
        println("LuauConfig initialized with paths: ${paths.joinToString(", ")}")
    }
}