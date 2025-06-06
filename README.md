# LuauLayer (Heavy WIP)

An abstraction layer for Luau built upon [`net.hollowcube:luau`](https://github.com/hollow-cube/luau-java).

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

// can also be defined in a separate (companion) object
object Object {
    @LuauFunction(lib = "veclib")
    fun double(a: Float): Float {
        return a * 2
    }
}

fun main(args: Array<String>) {
    State(config = config).addLibs(config.libs).runSandboxed { state ->
        val test = """
                    print(misc.foo(false)) -- Should print: Foo returned: false            
                    local l = fibonacci(15) * 10
                    print(l) -- Should print: 6100
                """.trimIndent()
        val compiled = config.compiler.compile(test)

        state.newThread().runSandboxed { thread ->
            val script = thread.load("test.luau", compiled)
            script.run()
        }
    }
}
```

## Contributing

Feel free to open issues for bug reports or feature requests, but keep in mind that this is mostly a personal project for me to understand the internals of lua/luau.

---
