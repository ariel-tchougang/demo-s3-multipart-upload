package com.tutos.ariel;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class S3MultiPartUploader {

    private final Region region;

    public S3MultiPartUploader(String region) {
        this.region = Region.of(region);
    }

    // Starts Multipart Upload
    public String startUpload(String bucket, String key) {
        try(S3Client s3Client = S3Client.builder().region(region).build()){
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(CreateMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return response.uploadId();
        }
    }

    // Add upload part
    public void addPart(BlockingQueue<CompletedPart> queue, byte[] chunk, String bucket, String key, int partNumber, String uploadId) {
        try(S3Client s3Client = S3Client.builder().region(region).build()){
            UploadPartResponse response = s3Client.uploadPart(UploadPartRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .partNumber(partNumber)
                    .uploadId(uploadId)
                    .contentLength((long) chunk.length)
                    .build(), RequestBody.fromInputStream(new ByteArrayInputStream(chunk), chunk.length));

            System.out.println("Finished Part: " + partNumber + ", ETag: " + response.eTag());
            queue.put(CompletedPart.builder().partNumber(partNumber).eTag(response.eTag()).build());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // End Multipart Upload
    public CompleteMultipartUploadResponse endUpload(String bucket, String key, String uploadId, List<CompletedPart> finishedParts) {
        try(S3Client s3Client = S3Client.builder().region(region).build()){
            return s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .multipartUpload(CompletedMultipartUpload.builder().parts(finishedParts).build())
                    .uploadId(uploadId)
                    .build());
        }
    }
}
