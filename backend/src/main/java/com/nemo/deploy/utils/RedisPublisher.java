package com.nemo.deploy.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.time.Duration;


@Component
public class RedisPublisher {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void push(String queueName, String message) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {

            System.out.println("Ping: " + jedis.ping());
            System.out.println("Pushing to queue..."+queueName);
            long len = jedis.rpush(queueName, message);
            System.out.println("New queue length: " + len);
            System.out.println("Queued: " + message);
        }
    }

}

