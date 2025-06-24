package com.nemo.deploy.utils;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class RedisService {

    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static final String QUEUE_NAME = "build-queue";

    public String getNextSessionId() {
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
            var result = jedis.brpop(0, QUEUE_NAME);
            return result != null ? result.get(1) : null;
        }
    }
}
