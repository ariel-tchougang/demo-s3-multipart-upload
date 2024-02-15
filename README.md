# demo-s3-multipart-upload
> S3 Multipart Upload Demo

## Context

* Amazon simple storage service (S3) is an object storage service offered by AWS where you can store objects up to 5TiB (5 TebiBytes) in size
* The maximum size that can be uploaded to S3 with a single putObject api call is 5 GiB (5 GibiBytes)
* AWS recommends to use multipart upload once you have object over 100 MiB in size
* To perform multipart upload, the following steps are needed:
  * Split the object in chunks: minimum chunk size is 5 MiB, and maximum number of chunks is 10000
  * Use S3 startUpload API call to initiate the upload process and retrieve an upload ID
  * Use S3 uploadPart API call to upload each of the parts to the upload ID and retrieve their corresponding ETag
  * Use S3 completeMultipartUpload API call with the upload ID and the finished parts list sorted by part number

![Alt text](/images/s3-multipart-upload.png?raw=true "S3 upload diagram")

## Technologies used:
* Java 17
* Maven
* AWS SDK for Java (artifact s3 - version 2.24.1)
* Apache Commons CLI (version 1.6.0)
* AWS CLI v2

## Required system environment variables for execution:
* AWS_REGION: so that the proper s3 endpoint could be applied
* If you have a configured AWS CLI profile and want to use it:
  * AWS_PROFILE
* If you don't have a configured AWS CLI profile, but rather access keys:
  * AWS_ACCESS_KEY_ID
  * AWS_SECRET_KEY
  * AWS_SESSION_TOKEN: only if you're using IAM role based credentials

## Required S3 API actions to grant to your AWS_PROFILE or AWS_ACCESS_KEY_ID
* s3:CreateMultipartUpload
* s3:UploadPart
* s3:CompleteMultipartUpload
* s3:PutObject

## Execution options
* -f or --file: path to file to be chunked and uploaded (mandatory)
* -k or --key: Key for destination object (if not provided, file path will be used instead)
* -b or --bucket: Destination bucket (must reside in AWS_REGION)
* -cs or --chunk_size: chunk size in MebiBytes (MiB) > 5 MiB
* -p or --processes: Number of upload processes to run simultaneously (default value is 10)
* -m or --multipart_only: to be set to false if you also want to trigger a simple putObject for comparison (default value is true)

## Building the application

```bash
mvn clean package
```

## Run the application
> Don't forget to first set the required system environment variables!!!

### From the project root folder

```bash
java -jar ./target/s3-multipart-upload-1.0-SNAPSHOT.jar -f <PATH/TO/FILE_TO_UPLOAD> -k <UPLOAD_FILE_KEY> -b <UPLOAD_BUCKET> -cs <CHUNK_SIZE> -p <NB_PARALLEL_PROCESS> -m <MULTI_PART_ONLY>
```

### From anywhere
```bash
java -jar <PATH/TO/JAR_FILE> -f <PATH/TO/FILE_TO_UPLOAD> -k <UPLOAD_FILE_KEY> -b <UPLOAD_BUCKET> -cs <CHUNK_SIZE> -p <NB_PARALLEL_PROCESS> -m <MULTI_PART_ONLY>
```

### With provided helper scripts

> Powershell
```bash
powershell.exe -File "PATH/TO/runMultipartUpload.ps1" -aws_region "REGION" -jar_file_location "PATH/TO/JAR_FILE" -upload_file_location "PATH/TO/FILE_TO_UPLOAD" -upload_file_key "UPLOAD_FILE_KEY" -upload_bucket "UPLOAD_BUCKET" -chunk_size "CHUNK_SIZE" -processes "NB_PARALLEL_PROCESS" -multipart_only "MULTI_PART_ONLY"
```

> Linux shell
```bash
PATH/TO/runMultipartUpload.sh <AWS_REGION> <PATH/TO/JAR_FILE> <PATH/TO/FILE_TO_UPLOAD> <UPLOAD_FILE_KEY> <UPLOAD_BUCKET> <CHUNK_SIZE> <NB_PARALLEL_PROCESS> <MULTI_PART_ONLY>
```

> Windows command prompt
```bash
PATH/TO/runMultipartUpload.cmd <AWS_REGION> <PATH/TO/JAR_FILE> <PATH/TO/FILE_TO_UPLOAD> <UPLOAD_FILE_KEY> <UPLOAD_BUCKET> <CHUNK_SIZE> <NB_PARALLEL_PROCESS> <MULTI_PART_ONLY>
```
