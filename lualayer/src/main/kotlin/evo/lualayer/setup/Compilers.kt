package evo.lualayer.setup

import net.hollowcube.luau.compiler.DebugLevel
import net.hollowcube.luau.compiler.LuauCompiler
import net.hollowcube.luau.compiler.OptimizationLevel

object Compilers {
    val DEBUG = LuauCompiler.builder() //TODO: Userdata
        .debugLevel(DebugLevel.DEBUGGER)
        .optimizationLevel(OptimizationLevel.NONE)
        .build()
    val RELEASE = LuauCompiler.DEFAULT
}