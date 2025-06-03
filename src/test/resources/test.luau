local l = require("luau.stdlib")
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