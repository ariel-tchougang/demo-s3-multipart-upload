#!/bin/bash

# runMultipartUpload.sh
# PATH/TO/runMultipartUpload.sh <AWS_REGION> <PATH/TO/JAR_FILE> <PATH/TO/FILE_TO_UPLOAD> <UPLOAD_FILE_KEY> <UPLOAD_BUCKET> <CHUNK_SIZE> <NB_PARALLEL_PROCESS> <MULTI_PART_ONLY>

# Assign command line arguments to variables
aws_region=$1
jar_file_location=$2
upload_file_location=$3
upload_file_key=$4
upload_bucket=$5
chunk_size=$6
processes=$7
multipart_only=$8

# Check if required arguments are provided
if [ -z "$aws_region" ] || [ -z "$jar_file_location" ] || [ -z "$upload_file_location" ] || [ -z "$upload_file_key" ] || [ -z "$upload_bucket" ] || [ -z "$chunk_size" ]; then
    echo "Error: aws_region, jar_file_location, upload_file_location, upload_file_key, upload_bucket, and chunk_size arguments are all required."
    exit 1
fi

echo "Info: aws_region = $aws_region"
echo "Info: jar_file_location = $jar_file_location"
echo "Info: upload_file_location = $upload_file_location"
echo "Info: upload_file_key = $upload_file_key"
echo "Info: upload_bucket = $upload_bucket"
echo "Info: chunk_size = $chunk_size"

# Check optional parameters
if [ -z "$processes" ]; then
    processes="10"
fi

if [ -z "$multipart_only" ]; then
    multipart_only="true"
fi

echo "Info: processes = $processes"
echo "multipart_only = $multipart_only"

# Set environment variables
export AWS_REGION="$aws_region"

# Run the Java application
java -jar "$jar_file_location" -f "$upload_file_location" -k "$upload_file_key" -b "$upload_bucket" -cs "$chunk_size" -p "$processes" -m "$multipart_only"

# Unset environment variables
unset AWS_REGION
