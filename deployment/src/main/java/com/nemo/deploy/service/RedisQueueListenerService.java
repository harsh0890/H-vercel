package com.nemo.deploy.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
@Slf4j
public class RedisQueueListenerService {

    private static final String QUEUE_NAME = "build-queue";

    @PostConstruct
    public void startListening() {
        Thread listenerThread = new Thread(() -> {
            while (true) {
                try (Jedis jedis = new Jedis("localhost", 6379)){
                    String result = jedis.lpop(QUEUE_NAME);
                    if (result != null && !result.isEmpty()) {
                        deployImpl(result);
                    }else {
                        log.debug("No Data Found : Thread entering sleep mode");
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
