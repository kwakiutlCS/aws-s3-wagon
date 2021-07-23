package io.github.kwakiutlcs.wagon;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class S3ClientSpy implements S3Client {
    
    private final String bucket;
    
    private final String key;
    
    private File upload;
    
    private boolean closed = false;
    
    S3ClientSpy(String bucket, String key) {
        this.bucket = bucket;
        this.key = key;
    }

    @Override
    public String serviceName() {
        return null;
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public ResponseBytes<GetObjectResponse> getObjectAsBytes(GetObjectRequest request) {
        if (closed
            || !bucket.equals(request.bucket())
            || !key.equals(request.key())) {
            throw new IllegalArgumentException();
        }

        var response = GetObjectResponse.builder().lastModified(Instant.ofEpochSecond(1000)).build();
        return ResponseBytes.fromByteArray(response, "response".getBytes());
    }
    
    @Override
    public PutObjectResponse putObject(PutObjectRequest request, Path path) {
        if (closed
            || !bucket.equals(request.bucket())
            || !key.equals(request.key())) {
            throw new IllegalArgumentException();
        }
        
        upload = path.toFile();
        return null;
    }
    
    File getUpload() {
        return upload;
    }
    
    boolean isClosed() {
        return closed;
    }
}
