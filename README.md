# LuauLayer (Heavy WIP)

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
val config = LuauConfig(
    paths = setOf(
        "lualayer/src/test/resources"
    ),
    libs = SyntheticLuauLibs.ALL  // or use setOf(SyntheticLuauLibs.MISC, SyntheticLuauLibs.FOO) if you want specific libs
)

@LuauFunction(lib = "misc")
fun foo(bool: Boolean): String {
    log("Foo function called with $bool", LogType.DEBUG)
    return "Foo returned: $bool"
}

@LuauFunction // not specifying lib means it will be added to the global namespace
fun fibonacci(n: Int): Int {
    return if (n <= 1) n else fibonacci(n - 1) + fibonacci(n - 2)
}

fun main(args: Array<String>) {
    val state = State(config = config).addLibs(config.libs)

    try {
        val thread = state.newThread() // TODO: Sandbox scope dsl maybe
        state.sandbox()
        thread.sandbox()
        val test = """
            print(misc.foo(false)) -- Should print: Foo returned: false
            print(fibonacci(15)) -- Should print: 610
        """.trimIndent()
        val compiled = config.compiler.compile(test)
        val script = thread.load("test.luau", compiled)
        log("status:" + script.run(), LogType.RUNTIME)

        thread.close()
    } finally {
        state.close()
    }
}
```

## Dependencies

*   `net.hollowcube:luau` for the core Luau binding.
*   `cz.lukynka:pretty-log` for enhanced logging.
*   `com.squareup.okio`, `kotlinx-datetime` for pretty-log.

For exact versions, see `gradle/libs.versions.toml`.

## Contributing

Feel free to open issues for bug reports or feature requests, but keep in mind that this is mostly a personal project for me to understand the internals of lua/luau.

---
