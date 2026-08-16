package com.nemo.deploy.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DockerManager {

    @Value("${docker.base.port:8080}")
    private int basePort;

    @Value("${docker.container.prefix:vercel-app}")
    private String containerPrefix;

    public String createAndRunContainer(String sessionId, String jarPath, int port) {
        try {
            String containerName = containerPrefix + "-" + sessionId;
            String imageName = "openjdk:21-jre-slim";

            log.info("Creating Docker container: {} for session: {}", containerName, sessionId);

            String jarFileName = Paths.get(jarPath).getFileName().toString();
            String containerJarPath = "/app/" + jarFileName;

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(
                "docker", "run", "-d",
                "--name", containerName,
                "-p", port + ":8080",
                "-v", jarPath + ":" + containerJarPath,
                imageName,
                "java", "-jar", containerJarPath
            );

            Process process = processBuilder.start();
            boolean completed = process.waitFor(30, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                throw new RuntimeException("Docker container creation timed out");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                throw new RuntimeException("Docker container creation failed: " + errorOutput.toString());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String containerId = reader.readLine();

            log.info("Docker container created successfully. Container ID: {}, Port: {}", containerId, port);
            return containerId;

        } catch (IOException | InterruptedException e) {
            log.error("Error creating Docker container: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Docker container", e);
        }
    }

    public void stopAndRemoveContainer(String sessionId) {
        try {
            String containerName = containerPrefix + "-" + sessionId;

            log.info("Stopping and removing Docker container: {}", containerName);

            ProcessBuilder stopBuilder = new ProcessBuilder("docker", "stop", containerName);
            Process stopProcess = stopBuilder.start();
            stopProcess.waitFor(10, TimeUnit.SECONDS);

            ProcessBuilder removeBuilder = new ProcessBuilder("docker", "rm", containerName);
            Process removeProcess = removeBuilder.start();
            removeProcess.waitFor(10, TimeUnit.SECONDS);

            log.info("Docker container stopped and removed: {}", containerName);

        } catch (IOException | InterruptedException e) {
            log.error("Error stopping/removing Docker container: {}", e.getMessage(), e);
        }
    }

    public boolean isContainerRunning(String sessionId) {
        try {
            String containerName = containerPrefix + "-" + sessionId;

            ProcessBuilder processBuilder = new ProcessBuilder("docker", "ps", "--filter", "name=" + containerName, "--format", "{{.Names}}");
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();

            return result != null && result.trim().equals(containerName);

        } catch (IOException e) {
            log.error("Error checking container status: {}", e.getMessage());
            return false;
        }
    }

    public int getNextAvailablePort() {
        int port = basePort;
        while (isPortInUse(port)) {
            port++;
        }
        return port;
    }

    private boolean isPortInUse(int port) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker", "ps", "--filter", "publish=" + port, "--format", "{{.Names}}");
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();

            return result != null && !result.trim().isEmpty();

        } catch (IOException e) {
            log.error("Error checking port usage: {}", e.getMessage());
            return false;
        }
    }
} 