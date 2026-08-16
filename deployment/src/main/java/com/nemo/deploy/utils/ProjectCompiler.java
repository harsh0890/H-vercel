package com.nemo.deploy.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
@Slf4j
public class ProjectCompiler {

    public String compileProject(String projectPath) {
        try {
            Path pomPath = Paths.get(projectPath, "pom.xml");
            if (!Files.exists(pomPath)) {
                throw new RuntimeException("pom.xml not found in project");
            }

            log.info("Starting Maven compilation for project: {}", projectPath);

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(new File(projectPath));
            processBuilder.command("mvn", "clean", "package", "-DskipTests");

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("Maven output: {}", line);
            }

            boolean completed = process.waitFor(10, TimeUnit.MINUTES);
            if (!completed) {
                process.destroyForcibly();
                throw new RuntimeException("Maven compilation timed out");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                throw new RuntimeException("Maven compilation failed with exit code " + exitCode + ": " + errorOutput.toString());
            }

            String jarPath = findJarFile(projectPath);
            if (jarPath == null) {
                throw new RuntimeException("JAR file not found after compilation");
            }

            log.info("Project compiled successfully. JAR location: {}", jarPath);
            return jarPath;

        } catch (IOException | InterruptedException e) {
            log.error("Error during project compilation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to compile project", e);
        }
    }

    private String findJarFile(String projectPath) {
        Path targetDir = Paths.get(projectPath, "target");
        if (!Files.exists(targetDir)) {
            return null;
        }

        try {
            return Files.walk(targetDir)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .filter(path -> !path.toString().contains("sources") && !path.toString().contains("javadoc"))
                    .findFirst()
                    .map(Path::toString)
                    .orElse(null);
        } catch (IOException e) {
            log.error("Error searching for JAR file: {}", e.getMessage());
            return null;
        }
    }
} 