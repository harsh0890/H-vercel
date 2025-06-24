package com.nemo.deploy.utils;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;


@Component
public class UploadToObjectStore {

    public void upload(File directory, String sessionId, String bucketName,
                       String accessKey, String secretKey, String r2AccountId){

        try (S3Client s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create("https://" + r2AccountId + ".r2.cloudflarestorage.com"))
                .forcePathStyle(true)
                .build(); Stream<Path> paths = Files.walk(directory.toPath())) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                String key = sessionId + "/" + directory.toPath().relativize(path).toString().replace("\\", "/");
                s3Client.putObject(PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build(),
                        RequestBody.fromFile(path));
                System.out.println("Uploaded: " + key);
            });
        } catch (IOException e) {
            System.err.println("Error walking through directory: " + e.getMessage());
        }

        System.out.println("Upload complete for session: " + sessionId);
    }
}

