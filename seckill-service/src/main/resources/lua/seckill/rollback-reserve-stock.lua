local stockKey = KEYS[1]
local userOrderKey = KEYS[2]
local reservationKey = KEYS[3]
local idempotentKey = KEYS[4]
local quantity = tonumber(ARGV[1])
local reservationId = ARGV[2]

local reservationExists = redis.call('exists', reservationKey)
local userOrderValue = redis.call('get', userOrderKey)
local idempotentValue = redis.call('get', idempotentKey)

if reservationExists == 0 and userOrderValue ~= reservationId and idempotentValue ~= reservationId then
    return {'ALREADY_RELEASED'}
end

if userOrderValue == reservationId then
    redis.call('del', userOrderKey)
end

if idempotentValue == reservationId then
    redis.call('del', idempotentKey)
end

if reservationExists == 1 then
    redis.call('del', reservationKey)
end

redis.call('incrby', stockKey, quantity)
return {'SUCCESS'}
