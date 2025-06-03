package evo.evo.lualayer.wrapper

import net.hollowcube.luau.LuaFunc

data class LuauLib(
    val name: String,
    val functions: Map<String, LuaFunc>
)