package com.nemo.deploy.controller;

import com.nemo.deploy.requestprocessor.request.DeployRequest;
import com.nemo.deploy.requestprocessor.response.DeployResponse;
import com.nemo.deploy.utils.RedisPublisher;
import com.nemo.deploy.utils.RedisService;
import com.nemo.deploy.utils.SessionIdGenerator;
import com.nemo.deploy.utils.UploadToObjectStore;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@Slf4j
public class VercelController {

    private final SessionIdGenerator generateSession;
    private final UploadToObjectStore uploadToObjectStore;
    private final RedisService redisService;
    private final RedisPublisher redisPublisher;

    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static final String QUEUE_NAME = "build-queue";


    @Value("${accessKey}")
    private String accessKey;

    @Value("${accountId}")
    private String accountId;

    @Value("${secretKey}")
    private String secretKey;



    public VercelController(SessionIdGenerator generateSession, UploadToObjectStore uploadToObjectStore, RedisService redisService, RedisPublisher redisPublisher) {
        this.generateSession = generateSession;
        this.uploadToObjectStore = uploadToObjectStore;
        this.redisService = redisService;
        this.redisPublisher = redisPublisher;
    }

    @PostMapping("/deploy")
    public DeployResponse deployService(@RequestBody DeployRequest request) throws GitAPIException {

        log.info(request.getRepoUrl());
        String sessionId = generateSession.generateSessionId();
        DeployResponse response = new DeployResponse();
        File localPath = new File("output/" + sessionId);
        try {
            System.out.println("Cloning into: " + localPath.getAbsolutePath());

            try (Git git = Git.cloneRepository()
                    .setURI(request.getRepoUrl())
                    .setDirectory(localPath)
                    .call()) {
                System.out.println("Cloned repo to: " + git.getRepository().getDirectory());
            }

            uploadToObjectStore.upload(localPath,sessionId,"vercel",accessKey,secretKey,accountId);

        } catch (Exception e) {
            System.err.println("Cloning failed: " + e.getMessage());
            e.printStackTrace();
        }

        redisPublisher.lpush("build-queue", sessionId);
        response.setId(sessionId);

        return response;

    }

    @GetMapping("/status")
    public String checkStatus(@RequestParam("id") String id){
        String nextSessionId = redisService.getNextSessionId();
        return nextSessionId;
    }
}
