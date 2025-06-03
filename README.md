# LuauLayer

LuauLayer is a Kotlin JVM library that provides a primitive wrapper for Luau scripting built upon [`net.hollowcube:luau`](https://github.com/hollow-cube/luau-java).

## Project Structure

*   `src/main/kotlin/evo/lualayer/`: Core library code.
    *   `setup/`: Configuration classes for `LuauConfig` and `Compilers`.
    *   `wrapper/`: Main wrapper classes like `State`, `LuauThread`, `LuauScript`, and `LuauLib`.
*   `src/test/kotlin/`: Example usage / tests.
*   `src/test/resources/`: Sample Luau scripts used in tests.

## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 21+
*   Gradle (wrapper included in the repository)
*   Your platform specific natives (luau natives are buggy on windows, so you'll have to get your own)

## Usage Example

```kotlin
fun main() {
    // Define a Kotlin function that Luau can call
    val greetingFunc = LuaFunc { luaState: LuaState ->
        val name = luaState.checkStringArg(1) // Get the first argument as a string
        println("Hello from Kotlin: $name!")
        luaState.pushString("Greeting processed!") // Return a string to Luau
        1 // No. of return values
    }

    // Create a LuauLib instance to bundle your functions
    val lib = LuauLib(
        name = "kt_utils", // This will be the global name in Luau (e.g., `kt_utils.greet()`)
        functions = mapOf(
            "greet" to greetingFunc
        )
    )

    val config = LuauConfig()
    // Add your custom library when initializing the state
    val state = State(config = config).addLibs(setOf(lib))

    try {
        val example = """
            local kt_utils = require("kt_utils")
            local result = kt_utils.greet("World")
            print(result)
        """.trimIndent()
        val compiled = config.compiler.compile(example)
        val script = state.load("example_script", compiled)
        log("status:" + script.run(), LogType.RUNTIME)

    } finally {
        state.close()
    }
}
```

## Dependencies

This project relies on:
*   `net.hollowcube:luau` for the core Luau binding.
*   `cz.lukynka:pretty-log` for enhanced logging.
*   `com.squareup.okio`, `kotlinx-serialization`, `kotlinx-datetime` for pretty-log.

For exact versions, see `gradle/libs.versions.toml`.

## Contributing

Feel free to open issues for bug reports or feature requests, but keep in mind that this is mostly a personal project for me to understand the internals of lua/luau.

---
