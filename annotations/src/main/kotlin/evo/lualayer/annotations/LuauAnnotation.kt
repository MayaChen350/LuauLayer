package evo.lualayer.annotations

/**
 * Marks a Kotlin function to be exposed as a Luau function.
 * The function must ultimately be convertible into a `LuaFunc`.
 *
 * Parameters within the annotated function will correspond to arguments
 * received from Luau. Return values will be pushed back to Luau.
 *
 * Example: `fun myLuauFunction(arg1: String, arg2: Int): Boolean`
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class LuauFunction(val name: String = "", val lib: String = "GLOBAL")
