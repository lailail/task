local stockKey = KEYS[1]
local userOrderKey = KEYS[2]
local reservationKey = KEYS[3]
local releaseKey = KEYS[4]
local idempotentKey = KEYS[5]

local quantity = tonumber(ARGV[1])
local releasedStatus = ARGV[2]
local reason = ARGV[3]
local releaseKeyExpireSeconds = tonumber(ARGV[4])

if redis.call('exists', releaseKey) == 1 then
    return {'ALREADY_RELEASED'}
end

if redis.call('exists', reservationKey) == 0 then
    return {'ALREADY_RELEASED'}
end

local currentStatus = redis.call('hget', reservationKey, 'status')
if currentStatus == releasedStatus then
    redis.call('set', releaseKey, 'done', 'EX', releaseKeyExpireSeconds)
    return {'ALREADY_RELEASED'}
end

redis.call('incrby', stockKey, quantity)
redis.call('del', userOrderKey)
redis.call('del', idempotentKey)
redis.call('hset', reservationKey, 'status', releasedStatus, 'reason', reason)
redis.call('set', releaseKey, 'done', 'EX', releaseKeyExpireSeconds)

return {'SUCCESS'}
