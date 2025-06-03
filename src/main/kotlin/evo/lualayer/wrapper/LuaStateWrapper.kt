package evo.lualayer.wrapper

import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaState

interface LuaStateWrapper {
    val lua: LuaState
    val config: LuauConfig

    fun openLibs() = lua.openLibs()
    fun sandbox() = lua.sandbox()
    fun pcall(args: Int, results: Int) = lua.pcall(args, results)
    fun close() = lua.close()
    fun newThread(): LuauThread = LuauThread(config = config, parent = lua)
    fun load(name: String, bytecode: ByteArray): LuauScript = LuauScript(
        lua = lua,
        name = name,
        bytecode = bytecode,
        config = config
    )

}