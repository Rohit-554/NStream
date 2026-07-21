package io.jadu.nstream.security

import io.jadu.nstream.config.ServerConfig
import io.lettuce.core.RedisClient

class RedisRateLimiter (config: ServerConfig) {
    private val commands by lazy { RedisClient.create(config.redisUri).connect().sync() }

    fun allows(action: String, identity: String, limit: Long = 5) : Boolean = runCatching {
        val key = "rate-limit:$action:${identity.lowercase()}" // rate-limit:login:user@email.com
        val count = commands.incr(key)
        if(count == 1L) commands.expire(key,60)
        count <= limit
    }.getOrDefault(true)
}