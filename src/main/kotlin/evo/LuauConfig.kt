package evo.evo

import evo.LuauLib
import evo.State
import net.hollowcube.luau.compiler.LuauCompiler

class LuauConfig(
    val paths: Set<String> = setOf("run/scripts"),
    val compiler: LuauCompiler = Compilers.DEBUG,
    libs: Set<LuauLib> = emptySet(),
) {
    val state = State().addLibs(libs)
}