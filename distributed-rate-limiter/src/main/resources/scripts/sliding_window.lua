-- Sliding-window rate limiter (atomic check-and-add).
--
-- KEYS[1] = sorted-set key for the user (e.g. "rate_limit:<userId>")
--
-- ARGV[1] = now              (current time in ms)
-- ARGV[2] = windowSizeMs     (window length in ms)
-- ARGV[3] = maxRequests      (max allowed requests in the window)
-- ARGV[4] = ttlSeconds       (TTL to apply to the key, in seconds)
-- ARGV[5] = member           (unique zset member for this request, e.g. "<now>:<uuid>")
--
-- Returns: { allowed, count, oldestTimestampMs }
--   allowed            = 1 if request is allowed, 0 if denied
--   count              = current number of requests in the window (after add, if allowed)
--   oldestTimestampMs  = score of the oldest entry in the window, or -1 if none

local key            = KEYS[1]
local now            = tonumber(ARGV[1])
local windowSizeMs   = tonumber(ARGV[2])
local maxRequests    = tonumber(ARGV[3])
local ttlSeconds     = tonumber(ARGV[4])
local member         = ARGV[5]

local windowStart = now - windowSizeMs

redis.call('ZREMRANGEBYSCORE', key, 0, windowStart)

local count = redis.call('ZCARD', key)

local function oldestScore()
  local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
  if oldest[2] then
    return tonumber(oldest[2])
  end
  return -1
end

if count >= maxRequests then
  return { 0, count, oldestScore() }
end

redis.call('ZADD', key, now, member)
redis.call('EXPIRE', key, ttlSeconds)

return { 1, count + 1, oldestScore() }
