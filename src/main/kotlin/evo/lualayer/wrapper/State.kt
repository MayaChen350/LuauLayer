package evo.lualayer.wrapper

import evo.lualayer.setup.LuauConfig
import net.hollowcube.luau.LuaFunc
import net.hollowcube.luau.LuaState
import java.io.File

open class State(
    override var config: LuauConfig,
    override val lua: LuaState = LuaState.newState(),
) : LuaStateWrapper {

    val require = LuaFunc { state: LuaState -> // TODO: Add wrapper for LuaFunc
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

    fun hotload(file: File): LuauScript {
        val fileBytes = file.readBytes()
        val bytecode = config.compiler.compile(fileBytes)

        return load(file.name, bytecode)
    }

    fun loadFromPaths(name: String): LuauScript {
        val file = File(config.paths.firstOrNull() ?: ".", name)
        if (!file.exists()) {
            throw IllegalArgumentException("File $name.luau does not exist in paths: ${config.paths.joinToString(", ")}")
        }
        val bytecode = config.compiler.compile(file.readBytes())
        return load(name, bytecode)
    }

    fun addGlobal(name: String, func: LuaFunc) {
        lua.pushCFunction(func, name)
        lua.setGlobal(name)
    }

    fun addLibs(libs: Set<LuauLib>): State {
        openLibs()
        addGlobal("require", require)
        libs.forEach { lib ->
            lua.registerLib(lib.name, lib.functions)
        }
        return this
    }
}

