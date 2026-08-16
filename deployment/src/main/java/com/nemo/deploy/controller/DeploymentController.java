package com.nemo.deploy.controller;

import com.nemo.deploy.model.DeploymentStatus;
import com.nemo.deploy.service.DeploymentStatusService;
import com.nemo.deploy.utils.DockerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deployment")
@Slf4j
public class DeploymentController {

    private final DeploymentStatusService deploymentStatusService;
    private final DockerManager dockerManager;

    public DeploymentController(DeploymentStatusService deploymentStatusService, DockerManager dockerManager) {
        this.deploymentStatusService = deploymentStatusService;
        this.dockerManager = dockerManager;
    }

    @GetMapping("/status/{sessionId}")
    public ResponseEntity<DeploymentStatus> getDeploymentStatus(@PathVariable String sessionId) {
        DeploymentStatus status = deploymentStatusService.getStatus(sessionId);
        if (status != null) {
            if (status.getStatus().equals("RUNNING")) {
                boolean isRunning = dockerManager.isContainerRunning(sessionId);
                if (!isRunning) {
                    status.setStatus("STOPPED");
                    deploymentStatusService.updateStatus(sessionId, "STOPPED", status.getContainerId(), status.getPort(), status.getJarPath(), null);
                }
            }
            return ResponseEntity.ok(status);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/halt/{sessionId}")
    public ResponseEntity<String> stopDeployment(@PathVariable String sessionId) {
        try {
            dockerManager.stopAndRemoveContainer(sessionId);
            deploymentStatusService.updateStatus(sessionId, "STOPPED", null, null, null, null);
            return ResponseEntity.ok("Deployment stopped successfully");
        } catch (Exception e) {
            log.error("Error stopping deployment: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to stop deployment");
        }
    }

    @PostMapping("/restart/{sessionId}")
    public ResponseEntity<String> restartDeployment(@PathVariable String sessionId) {
        try {
            DeploymentStatus status = deploymentStatusService.getStatus(sessionId);
            if (status == null) {
                return ResponseEntity.notFound().build();
            }

            dockerManager.stopAndRemoveContainer(sessionId);
            
            if (status.getJarPath() != null && status.getPort() != null) {
                String containerId = dockerManager.createAndRunContainer(sessionId, status.getJarPath(), status.getPort());
                deploymentStatusService.updateStatus(sessionId, "RUNNING", containerId, status.getPort(), status.getJarPath(), null);
                return ResponseEntity.ok("Deployment restarted successfully");
            } else {
                return ResponseEntity.badRequest().body("Cannot restart deployment without JAR path or port information");
            }
        } catch (Exception e) {
            log.error("Error restarting deployment: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to restart deployment");
        }
    }
} 