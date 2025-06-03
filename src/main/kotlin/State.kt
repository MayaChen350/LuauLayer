package evo

import evo.evo.LuauConfig
import net.hollowcube.luau.LuaFunc
import net.hollowcube.luau.LuaState
import net.hollowcube.luau.compiler.LuauCompiler
import java.io.File

class State(override val lua: LuaState = LuaState.newState(), val config: LuauConfig) : LuaStateWrapper {
    val require = LuaFunc { state: LuaState ->
        with(state) {
            val moduleName = checkStringArg(1)
            println("Requiring module: $moduleName")

            getGlobal(moduleName)
            if (!isNil(-1)) {
                return@LuaFunc 1
            }
            pop(1)

            val file = config.paths.asSequence()
                .map { File(it, "$moduleName.luau") }
                .firstOrNull { it.exists() }
            if (file == null) {
                pushNil()
                return@LuaFunc 1
            }

            hotload(file)

            pcall(0, 1)
            if (isNil(-1)) {
                pushNil()
                return@LuaFunc 1
            }

            pushValue(-1)
            setGlobal(moduleName)
        }

        1
    }

    fun hotload(file: File) {
        val fileBytes = file.readBytes()
        val result = config.compiler.compile(fileBytes)

        load(file.name, result)
    }
}