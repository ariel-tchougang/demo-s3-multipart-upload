package com.tutos.ariel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {

        String region = System.getenv("AWS_REGION");

        if (region == null) {
            System.err.println("AWS_REGION environment variable is not set.");
            System.exit(1);
        }

        final S3MultiPartUploader multiPartUploader = new S3MultiPartUploader(region);

        CommandLineArgs commandLineArgs = parseCommandLineArgs(args);

        if (commandLineArgs == null) {
            System.out.println("Invalid command line arguments");
            return;
        }

        if (commandLineArgs.chunkSize() <= 5) {
            System.out.println("Chunk size must be greater than 5");
            return;
        }

        String file = commandLineArgs.file();
        String key = commandLineArgs.key();
        String bucket = commandLineArgs.bucket();
        int chunkSize = commandLineArgs.chunkSize() * 1024 * 1024;
        int processes = commandLineArgs.processes();

        if (key == null || key.isEmpty()) {
            key = file;
        }

        handleMultipartUpload(multiPartUploader, file, bucket, key, processes, chunkSize);

        if (!commandLineArgs.multipartOnly()) {
            handleSimpleUpload(region, file, bucket, key);
        }
    }

    private static void handleSimpleUpload(String region, String file, String bucket, String key) {
        long startTime;
        long endTime;
        long executionTime;

        System.out.println("Starting simple putObject");
        startTime = System.currentTimeMillis();
        String uploadKey = "putObject-" + key;
        System.out.printf("Uploading file %s into bucket %s with key %s", file, bucket, uploadKey);
        new S3Uploader(region).process(file, bucket, uploadKey);
        endTime = System.currentTimeMillis();
        executionTime = (endTime - startTime) / 1000;
        System.out.println("Execution time: " + executionTime + " seconds");
        System.out.println("Upload completed");
    }

    private static void handleMultipartUpload(S3MultiPartUploader multiPartUploader, String file, String bucket, String key, int processes, int chunkSize) {
        long startTime = System.currentTimeMillis();
        String multipartKey =  "multipart-" + key;
        String uploadId = multiPartUploader.startUpload(bucket, multipartKey);
        System.out.println("Starting upload: " + uploadId);
        System.out.printf("Uploading file %s into bucket %s with key %s", file, bucket, multipartKey);

        try (FileInputStream fileUpload = new FileInputStream(file)) {
            BlockingQueue<CompletedPart> queue = new LinkedBlockingQueue<>();
            ExecutorService executor = Executors.newFixedThreadPool(processes);

            List<CompletedPart> completedParts = new ArrayList<>();
            byte[] chunk = new byte[chunkSize];
            int partNumber = 1;

            int bytesRead;
            while ((bytesRead = fileUpload.read(chunk)) > 0) {
                byte[] finalChunk = new byte[bytesRead];
                System.arraycopy(chunk, 0, finalChunk, 0, bytesRead);
                FilePartUploader uploader = new FilePartUploader(multiPartUploader, queue, finalChunk, bucket, multipartKey, partNumber, uploadId);
                executor.execute(uploader);
                partNumber++;
            }

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            while (!queue.isEmpty()) {
                completedParts.add(queue.take());
            }

            completedParts.sort((o1, o2) -> Integer.compare(o1.partNumber(), o2.partNumber()));
            CompleteMultipartUploadResponse response = multiPartUploader.endUpload(bucket, multipartKey, uploadId, completedParts);
            System.out.println(response);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long executionTime = (endTime - startTime) / 1000;
        System.out.println("Execution time: " + executionTime + " seconds");
        System.out.println("Multipart Upload completed");
    }

    private static CommandLineArgs parseCommandLineArgs(String[] args) {
        Options options = new Options();
        options.addOption("f", "file", true, "File to be chunked and uploaded");
        options.addOption("k", "key", true, "Key for destination object");
        options.addOption("b", "bucket", true, "Destination bucket");
        options.addOption("cs", "chunk_size", true, "Chunk size in MB, must be > 5MiB");
        options.addOption("p", "processes", true, "Number of upload processes to run simultaneously");
        options.addOption("m", "multipart_only", true, "Performs simple putObject as well for comparison if value is false");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            return null;
        }

        return new CommandLineArgs(
                cmd.getOptionValue("file"),
                cmd.getOptionValue("key"),
                cmd.getOptionValue("bucket"),
                Integer.parseInt(cmd.getOptionValue("chunk_size")),
                Integer.parseInt(cmd.getOptionValue("processes", "10")),
                Boolean.parseBoolean(cmd.getOptionValue("multipart_only", "true"))
        );
    }

    private record CommandLineArgs(
            String file,
            String key,
            String bucket,
            int chunkSize,
            int processes,
            boolean multipartOnly
    ) { }
}

