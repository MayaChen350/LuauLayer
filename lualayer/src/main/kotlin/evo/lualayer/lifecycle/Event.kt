package evo.lualayer.lifecycle

import evo.lualayer.wrapper.State

abstract class Event(val name: String) {
    abstract fun apply(state: State): Int
}

data class BingBongEvent(
    val message: String
) : Event("bing_bong") {
    override fun apply(state: State): Int {
        state.lua.pushString(message)
        return 1
    }
}

data class BloopEvent(
    val message: String
) : Event("bloop") {
    override fun apply(state: State): Int {
        state.lua.pushString(message)
        return 1
    }
}