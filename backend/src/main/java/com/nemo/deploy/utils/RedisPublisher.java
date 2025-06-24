package com.nemo.deploy.utils;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Component
public class RedisPublisher {
    public void lpush(String queueName, String message) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {

            System.out.println("Ping: " + jedis.ping());
            System.out.println("Pushing to queue...");
            long len = jedis.publish(queueName, message);
            System.out.println("New queue length: " + len);

            System.out.println("Queued: " + message);
        }
    }

}

