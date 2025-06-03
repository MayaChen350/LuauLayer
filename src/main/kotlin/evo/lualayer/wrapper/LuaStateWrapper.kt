package evo.evo.lualayer.wrapper

import net.hollowcube.luau.LuaState

interface LuaStateWrapper {
    val lua: LuaState

    fun openLibs() = lua.openLibs()
    fun sandbox() = lua.sandbox()
    fun pcall(args: Int, results: Int) = lua.pcall(args, results)
    fun close() = lua.close()
    fun newThread(): LuauThread = LuauThread(lua)
    fun load(name: String, bytecode: ByteArray): LuauScript = LuauScript(lua, name, bytecode)

}