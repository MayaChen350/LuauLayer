# LuauLayer (Heavy WIP)

LuauLayer is a Kotlin JVM library that provides a primitive wrapper for Luau scripting built upon [`net.hollowcube:luau`](https://github.com/hollow-cube/luau-java).

## Project Structure 
###### _(already outdated)_
###### _(also I messed up the package names (lualayer vs lua**u**layer))_

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
    return "Foo returned: $bool"
}

@LuauFunction // not specifying a namespace means it will be added to the global namespace
fun fibonacci(n: Int): Int {
    return if (n <= 1) n else fibonacci(n - 1) + fibonacci(n - 2)
}

// also can be defined in a separate (companion) object or
object Object {
    @LuauFunction(lib = "veclib")
    fun double(a: Float): Float {
        return a * 2
    }
}

fun main(args: Array<String>) {
    State(config = config).addLibs(config.libs).runSandboxed { state ->
        val test = """
                    misc.foo(false) -- Should print: Foo returned: false            
                    local l = fibonacci(15) * 10 -- Should print: 6100
                """.trimIndent()
        val compiled = config.compiler.compile(test)

        state.newThread().runSandboxed { thread ->
            val script = thread.load("test.luau", compiled)
            script.run()
        }
    }
}
```

## Dependencies

*   `net.hollowcube:luau` for the core Luau binding.
*   `cz.lukynka:pretty-log` for enhanced logging.
*   `com.squareup.okio`, `kotlinx-datetime` for pretty-log.
*   `com.google.devtools.ksp`

For exact versions, see `gradle/libs.versions.toml`.

## Contributing

Feel free to open issues for bug reports or feature requests, but keep in mind that this is mostly a personal project for me to understand the internals of lua/luau.

---
