package com.linkedln.postservice.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class S3Service {
    private final S3Client s3Client;

    @Value("$(aws.s3.bucket-name)")
    private String bucketName;
    @Value("$(aws.region)")
    private String region;

    public String uploadFile(MultipartFile file, String keyPrefix) {
        try {
            String key = keyPrefix + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(key)
                    .contentType(file.getContentType()).build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            String url = "https://" + bucketName + ".s3" + region + ".amazons.com/" + key;

            log.info("File uploaded to S3: {}", url);
            return url;
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to upload file to S3: " + e.getMessage());
        }
    }
}
