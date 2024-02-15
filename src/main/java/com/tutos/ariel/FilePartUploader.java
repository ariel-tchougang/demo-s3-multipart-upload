package com.tutos.ariel;

import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.concurrent.BlockingQueue;

public class FilePartUploader implements Runnable {

    private final S3MultiPartUploader multiPartUploader;
    private final BlockingQueue<CompletedPart> queue;
    private final byte[] chunk;
    private final String bucket;
    private final String key;
    private final int partNumber;
    private final String uploadId;

    public FilePartUploader(
            S3MultiPartUploader multiPartUploader,
            BlockingQueue<CompletedPart> queue,
            byte[] chunk,
            String bucket,
            String key,
            int partNumber,
            String uploadId) {
        this.multiPartUploader = multiPartUploader;
        this.queue = queue;
        this.chunk = chunk;
        this.bucket = bucket;
        this.key = key;
        this.partNumber = partNumber;
        this.uploadId = uploadId;
    }

    @Override
    public void run() {
        multiPartUploader.addPart(queue, chunk, bucket, key, partNumber, uploadId);
    }
}
