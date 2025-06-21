package com.nemo.vercel.controller;

import com.nemo.vercel.requestprocessor.DeployRequest;
import com.nemo.vercel.utils.SessionIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class UploadController {

    private final SessionIdGenerator generateSession;

    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static final String QUEUE_NAME = "build-queue";

    public UploadController(SessionIdGenerator generateSession) {
        this.generateSession = generateSession;
    }

    @PostMapping("/deploy")
    public String deployService(@RequestBody DeployRequest request){

        log.info(request.getRepoUrl());
        String sessionId = generateSession.generateSessionId();


        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {

                jedis.lpush(QUEUE_NAME, sessionId);
                System.out.println("Published: " + sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Hello World";
    }

    @GetMapping("/status")
    public Map<String, String> checkStatus(){
        Map<String, String> response = new HashMap<>();
        response.put("id", "12123");
        return response;
    }
}
