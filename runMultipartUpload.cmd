@echo off
setlocal

REM runMultipartUpload.cmd
REM PATH/TO/runMultipartUpload.cmd AWS_REGION PATH/TO/JAR_FILE PATH/TO/FILE_TO_UPLOAD UPLOAD_FILE_KEY UPLOAD_BUCKET CHUNK_SIZE NB_PARALLEL_PROCESS MULTI_PART_ONLY

set "aws_region=%1"
set "jar_file_location=%2"
set "upload_file_location=%3"
set "upload_file_key=%4"
set "upload_bucket=%5"
set "chunk_size=%6"
set "processes=%7"
set "multipart_only=%8"

REM Check if required arguments are provided
if "%aws_region%"=="" if "%jar_file_location%"=="" if "%upload_file_location%"=="" if "%upload_file_key%"=="" if "%upload_bucket%"=="" if "%chunk_size%"=="" (
    echo Error: aws_profile, jar_file_location, upload_file_location, upload_file_key, upload_bucket, and chunk_size arguments are all required.
    exit /b 1
)

echo Info: aws_region = %aws_region%
echo Info: jar_file_location = %jar_file_location%
echo Info: upload_file_location = %upload_file_location%
echo Info: upload_file_key = %upload_file_key%
echo Info: upload_bucket = %upload_bucket%
echo Info: chunk_size = %chunk_size%

REM Check optional parameters
if "%processes%"=="" set "processes=10"
if "%multipart_only%"=="" set "multipart_only=true"

echo Info: processes = %processes%
echo Info: multipart_only = %multipart_only%

REM Set environment variables
set "AWS_REGION=%aws_region%"

REM Run the Java application
java -jar %jar_file_location% -f %upload_file_location% -k %upload_file_key% -b %upload_bucket% -cs %chunk_size% -p %processes% -m %multipart_only%

REM Unset environment variables
set "AWS_REGION="

endlocal
