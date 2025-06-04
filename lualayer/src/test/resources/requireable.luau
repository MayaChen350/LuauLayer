print('init "requireable"')
local function test()
	print('   test function called')
	require("metoo")
	metoo.too()
end
return {
	test = test,
}