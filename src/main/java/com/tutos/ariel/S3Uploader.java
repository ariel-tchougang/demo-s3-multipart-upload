package com.tutos.ariel;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;

public class S3Uploader {

    private final Region region;

    public S3Uploader(String region) {
        this.region = Region.of(region);
    }

    public PutObjectResponse process(String file, String bucket, String key) {
        try(S3Client s3Client = S3Client.builder().build()){
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            return s3Client.putObject(putObjectRequest, RequestBody.fromFile(new File(file)));
        }
    }
}
