# runMultipartUpload.ps1 file
# powershell.exe -File "PATH/TO/runMultipartUpload.ps1" -aws_region "REGION" -jar_file_location "PATH/TO/JAR_FILE" -upload_file_location "PATH/TO/FILE_TO_UPLOAD" -upload_file_key "UPLOAD_FILE_KEY" -upload_bucket "UPLOAD_BUCKET" -chunk_size "CHUNK_SIZE" -processes "NB_PARALLEL_PROCESS" -multipart_only "MULTI_PART_ONLY"

param(
    [string]$aws_region,
    [string]$jar_file_location,
    [string]$upload_file_location,
    [string]$upload_file_key,
    [string]$upload_bucket,
    [string]$chunk_size,
    [string]$processes,
    [string]$multipart_only
)

# Check if required arguments are provided
if (-not $aws_region -or -not $jar_file_location -or -not $upload_file_location -or -not $upload_file_key -or -not $upload_bucket -or -not $chunk_size) {
    Write-Host "Error: $aws_region, $jar_file_location, $upload_file_location, $upload_file_key, $upload_bucket, and $chunk_size arguments are all required."
    exit 1
}

Write-Host "Info: aws_region = $aws_region"
Write-Host "Info: jar_file_location = $jar_file_location"
Write-Host "Info: upload_file_location = $upload_file_location"
Write-Host "Info: upload_file_location = $upload_file_location"
Write-Host "Info: upload_file_key = $upload_file_key"
Write-Host "Info: upload_bucket = $upload_bucket"
Write-Host "Info: chunk_size = $chunk_size"

# Check optional parameters
if (-not $processes) {
    $processes = "10"
}

if (-not $multipart_only) {
    $multipart_only = "true"
}

Write-Host "processes = $processes"
Write-Host "multipart_only = $multipart_only"

# Set environment variables
$env:AWS_REGION = $aws_region

# Run the Java application
java -jar $jar_file_location -f $upload_file_location -k $upload_file_key -b $upload_bucket -cs $chunk_size -p $processes -m $multipart_only

# Unset environment variables
Remove-Item Env:\AWS_REGION
