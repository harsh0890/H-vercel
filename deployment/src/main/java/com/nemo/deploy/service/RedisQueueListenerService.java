package com.nemo.deploy.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
                try {
                    String result = redisTemplate.opsForList().rightPop(QUEUE_NAME, Duration.ofSeconds(10));

                    if (result != null && !result.isEmpty()) {
                        deployImpl(result);
                    }else {
                        System.out.println("Queue empty. Thread entering sleep mode...");
                        Thread.sleep(10000);
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
