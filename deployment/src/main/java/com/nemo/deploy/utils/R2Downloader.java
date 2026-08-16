package com.nemo.deploy.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class R2Downloader {

    public void downloadProject(String sessionId, String bucketName, String accessKey, String secretKey, String accountId, String downloadPath) {
        try (S3Client s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                .forcePathStyle(true)
                .build()) {

            Path projectPath = Paths.get(downloadPath, sessionId);
            Files.createDirectories(projectPath);

            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(sessionId + "/")
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            for (S3Object s3Object : listResponse.contents()) {
                String key = s3Object.key();
                String relativePath = key.substring(sessionId.length() + 1);
                
                if (relativePath.isEmpty()) continue;

                Path filePath = projectPath.resolve(relativePath);
                Files.createDirectories(filePath.getParent());

                GetObjectRequest getRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                s3Client.getObject(getRequest, ResponseTransformer.toFile(filePath));
                log.info("Downloaded: {}", relativePath);
            }

            log.info("Project download complete for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Error downloading project: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download project", e);
        }
    }
} 