package com.nemo.deploy.service;

import com.nemo.deploy.model.DeploymentStatus;
import com.nemo.deploy.utils.DockerManager;
import com.nemo.deploy.utils.ProjectCompiler;
import com.nemo.deploy.utils.R2Downloader;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class RedisQueueListenerService {

    private static final String QUEUE_NAME = "build-queue";
    private static final String BUCKET_NAME = "vercel";

    private final R2Downloader r2Downloader;
    private final ProjectCompiler projectCompiler;
    private final DockerManager dockerManager;
    private final DeploymentStatusService deploymentStatusService;

    @Value("${accessKey}")
    private String accessKey;

    @Value("${accountId}")
    private String accountId;

    @Value("${secretKey}")
    private String secretKey;

    @Value("${download.path:/tmp/deployments}")
    private String downloadPath;

    public RedisQueueListenerService(R2Downloader r2Downloader, ProjectCompiler projectCompiler, 
                                   DockerManager dockerManager, DeploymentStatusService deploymentStatusService) {
        this.r2Downloader = r2Downloader;
        this.projectCompiler = projectCompiler;
        this.dockerManager = dockerManager;
        this.deploymentStatusService = deploymentStatusService;
    }

    @PostConstruct
    public void startListening() {
        Thread listenerThread = new Thread(() -> {
            while (true) {
                try (Jedis jedis = new Jedis("localhost", 6379)){
                    String result = jedis.lpop(QUEUE_NAME);
                    if (result != null && !result.isEmpty()) {
                        deployImpl(result);
                    } else {
                        log.debug("No Data Found : Thread entering sleep mode");
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    log.error("Error reading from Redis queue: {}", e.getMessage());
                }
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void deployImpl(String sessionId) {
        log.info("Starting deployment for session: {}", sessionId);
        
        try {
            deploymentStatusService.updateStatus(sessionId, "DOWNLOADING", null, null, null, null);

            Path projectPath = Paths.get(downloadPath, sessionId);
            Files.createDirectories(projectPath);

            r2Downloader.downloadProject(sessionId, BUCKET_NAME, accessKey, secretKey, accountId, downloadPath);

            deploymentStatusService.updateStatus(sessionId, "COMPILING", null, null, null, null);

            String jarPath = projectCompiler.compileProject(projectPath.toString());

            deploymentStatusService.updateStatus(sessionId, "DEPLOYING", null, null, jarPath, null);

            int port = dockerManager.getNextAvailablePort();
            String containerId = dockerManager.createAndRunContainer(sessionId, jarPath, port);

            deploymentStatusService.updateStatus(sessionId, "RUNNING", containerId, port, jarPath, null);

            log.info("Deployment completed successfully for session: {}. Container: {}, Port: {}", 
                    sessionId, containerId, port);

        } catch (Exception e) {
            log.error("Deployment failed for session {}: {}", sessionId, e.getMessage(), e);
            deploymentStatusService.updateStatus(sessionId, "FAILED", null, null, null, e.getMessage());
            
            try {
                dockerManager.stopAndRemoveContainer(sessionId);
            } catch (Exception cleanupError) {
                log.error("Error cleaning up failed deployment: {}", cleanupError.getMessage());
            }
        }
    }
}
