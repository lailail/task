local stockKey = KEYS[1]
local userOrderKey = KEYS[2]
local reservationKey = KEYS[3]
local idempotentKey = KEYS[4]
local quantity = tonumber(ARGV[1])
local reservationId = ARGV[2]
local activityId = ARGV[3]
local ticketId = ARGV[4]
local userId = ARGV[5]
local status = ARGV[6]
local expireAt = ARGV[7]
local idempotencyKey = ARGV[8]
local requestId = ARGV[9]
local expireSeconds = tonumber(ARGV[10])

if redis.call('exists', idempotentKey) == 1 or redis.call('exists', userOrderKey) == 1 then
    return {'DUPLICATE', ''}
end

local stock = tonumber(redis.call('get', stockKey) or '0')
if stock < quantity then
    return {'OUT_OF_STOCK', ''}
end

redis.call('decrby', stockKey, quantity)
redis.call('set', userOrderKey, reservationId, 'EX', expireSeconds)
redis.call('set', idempotentKey, reservationId, 'EX', expireSeconds)
redis.call('hset', reservationKey,
    'reservationId', reservationId,
    'activityId', activityId,
    'ticketId', ticketId,
    'userId', userId,
    'quantity', tostring(quantity),
    'status', status,
    'expireAt', expireAt,
    'idempotencyKey', idempotencyKey,
    'requestId', requestId
)
redis.call('expire', reservationKey, expireSeconds)

return {'SUCCESS', expireAt}
