package evo.lualayer.lifecycle

import evo.lualayer.wrapper.State

abstract class Event(val name: String) {
    abstract fun apply(state: State): Int
}

data class ChatMessageEvent(
    val message: String
) : Event("chat_message_event") {
    override fun apply(state: State): Int {
        state.lua.pushString(message)
        return 1
    }
}