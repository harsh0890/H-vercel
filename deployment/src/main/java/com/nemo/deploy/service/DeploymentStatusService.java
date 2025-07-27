package com.nemo.deploy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nemo.deploy.model.DeploymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;

@Service
@Slf4j
public class DeploymentStatusService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String STATUS_PREFIX = "deployment:status:";

    public void updateStatus(String sessionId, String status, String containerId, Integer port, String jarPath, String errorMessage) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            DeploymentStatus existingStatus = getStatus(sessionId);
            
            DeploymentStatus deploymentStatus = new DeploymentStatus();
            deploymentStatus.setSessionId(sessionId);
            deploymentStatus.setStatus(status);
            deploymentStatus.setContainerId(containerId);
            deploymentStatus.setPort(port);
            deploymentStatus.setJarPath(jarPath);
            deploymentStatus.setErrorMessage(errorMessage);
            deploymentStatus.setUpdatedAt(LocalDateTime.now());

            if (existingStatus != null && existingStatus.getCreatedAt() != null) {
                deploymentStatus.setCreatedAt(existingStatus.getCreatedAt());
            } else {
                deploymentStatus.setCreatedAt(LocalDateTime.now());
            }

            String statusJson = objectMapper.writeValueAsString(deploymentStatus);
            jedis.set(STATUS_PREFIX + sessionId, statusJson);
            jedis.expire(STATUS_PREFIX + sessionId, 86400);

            log.info("Updated deployment status for session {}: {}", sessionId, status);
        } catch (JsonProcessingException e) {
            log.error("Error serializing deployment status: {}", e.getMessage());
        }
    }

    public DeploymentStatus getStatus(String sessionId) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            String statusJson = jedis.get(STATUS_PREFIX + sessionId);
            if (statusJson != null) {
                return objectMapper.readValue(statusJson, DeploymentStatus.class);
            }
            return null;
        } catch (Exception e) {
            log.error("Error retrieving deployment status: {}", e.getMessage());
            return null;
        }
    }

    public void deleteStatus(String sessionId) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            jedis.del(STATUS_PREFIX + sessionId);
            log.info("Deleted deployment status for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Error deleting deployment status: {}", e.getMessage());
        }
    }
} 