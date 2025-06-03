local os = require("os")
print("clock: " .. os.clock())
for key, value in pairs(os) do
    if key ~= 'time' then
        print("Removing os." .. key .. " from sandbox")
        os[key] = nil
    end
end
print("clock after sandbox: " .. os.clock())