package evo.evo.lualayer.setup

import evo.evo.lualayer.wrapper.LuauLib
import evo.evo.lualayer.wrapper.State
import net.hollowcube.luau.compiler.LuauCompiler

class LuauConfig(
    val paths: Set<String> = setOf("run/scripts"),
    val compiler: LuauCompiler = Compilers.DEBUG,
    libs: Set<LuauLib> = emptySet(),
) {
    val state = State().addLibs(libs)
}