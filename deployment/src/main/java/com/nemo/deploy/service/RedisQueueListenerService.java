package com.nemo.deploy.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.time.Duration;

@Service
public class RedisQueueListenerService {

    private static final String QUEUE_NAME = "build-queue";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostConstruct
    public void startListening() {
        Thread listenerThread = new Thread(() -> {
            while (true) {
                try (Jedis jedis = new Jedis("localhost", 6379)){
                    String result = jedis.lpop(QUEUE_NAME);
                    if (result != null && !result.isEmpty()) {
                        deployImpl(result);
                    }else {
                        System.out.println("No Data Found : Entering Sleep Mode");
                        Thread.sleep(5000);
                    }

                } catch (Exception e) {
                    System.err.println("Error reading from Redis queue: " + e.getMessage());
                }
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void deployImpl(String id) {
        System.out.println("Received ID from Redis queue: " + id);
    }
}
