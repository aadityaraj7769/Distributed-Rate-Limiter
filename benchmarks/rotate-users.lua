-- wrk script: rotate X-User-Id across N distinct users per request.
-- Usage: wrk -t4 -c40 -d30s -s benchmarks/rotate-users.lua http://localhost:8080/ping
-- Override N via env: NUM_USERS=500 wrk ...

local num_users = tonumber(os.getenv("NUM_USERS")) or 100

math.randomseed(os.time())

request = function()
  local user_id = "user-" .. math.random(1, num_users)
  wrk.headers["X-User-Id"] = user_id
  return wrk.format("GET", "/ping")
end

-- Track status code distribution so we can see allowed vs rejected at the end.
local status_counts = {}

response = function(status, headers, body)
  status_counts[status] = (status_counts[status] or 0) + 1
end

done = function(summary, latency, requests)
  io.write("\n--- Status code distribution ---\n")
  for code, count in pairs(status_counts) do
    io.write(string.format("  %d : %d\n", code, count))
  end
  io.write(string.format("\np50 latency: %.2f ms\n", latency:percentile(50) / 1000))
  io.write(string.format("p95 latency: %.2f ms\n", latency:percentile(95) / 1000))
  io.write(string.format("p99 latency: %.2f ms\n", latency:percentile(99) / 1000))
end
