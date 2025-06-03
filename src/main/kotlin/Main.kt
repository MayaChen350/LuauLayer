package evo

import evo.evo.LuauConfig

val config = LuauConfig(
    paths = setOf(
        "src/main/resources/lua",
        "src/main/resources/luau",
        "src/main/resources/luau/stdlib"
    )
)
fun main(args: Array<String>) {
    val state = config.state

    val test = """
        print("Hello, world!")
        local function add(a, b)
            return a + b
        end
        local function subtract(a, b)
            return a - b
        end
        local function multiply(a, b)
            return a * b
        end
        local function divide(a, b)
            if b == 0 then
                error("Division by zero is not allowed")
            end
            return a / b
        end
        local function main()
            print("Addition: " .. add(5, 3))
            print("Subtraction: " .. subtract(5, 3))
            print("Multiplication: " .. multiply(5, 3))
            print("Division: " .. divide(5, 3))
        end
        main()
    """.trimIndent()

    val bytecode = config.compiler.compile(test)

    try {
        state.sandbox()
        val thread = state.newThread()
        thread.sandbox()

        val script = thread.load("test.luau", bytecode)
        println("status:" + script.run())

        thread.close()
    } finally {
        state.close();
    }
}