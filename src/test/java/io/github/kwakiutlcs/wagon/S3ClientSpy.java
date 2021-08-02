package io.github.kwakiutlcs.wagon;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class S3ClientSpy implements S3Client {
    
    private final String bucket;
    
    private final String key;
    
    private Map<String, File> uploads = new HashMap<>();
    
    private boolean closed = false;
    
    private boolean hasKey = true;
    
    private S3ClientSpy(String bucket, String key) {
        this.bucket = bucket;
        this.key = key;
    }
    
    static S3ClientSpy of(String bucket, String key) {
        return new S3ClientSpy(bucket, key);
    }
    
    static S3ClientSpy noKey() {
        var client = new S3ClientSpy("bucket", "key");
        client.hasKey = false;
        
        return client;
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
    public ResponseInputStream<GetObjectResponse> getObject(GetObjectRequest request) {
        if (!hasKey) {
            throw NoSuchKeyException.builder().build();
        }
        
        if (closed
            || !bucket.equals(request.bucket())
            || !request.key().startsWith(key)) {
            throw new IllegalArgumentException();
        }
        
        var is = AbortableInputStream.create(new ByteArrayInputStream("response".getBytes()));
        var response = GetObjectResponse.builder().lastModified(Instant.ofEpochSecond(1000)).build();
        return new ResponseInputStream<>(response, is);
    }
    
    @Override
    public PutObjectResponse putObject(PutObjectRequest request, Path path) {
        if (closed
            || !bucket.equals(request.bucket())
            || !request.key().startsWith(key)) {
            throw new IllegalArgumentException();
        }
        
        uploads.put(request.key(), path.toFile());
        return null;
    }
    
    File getUpload(String key) {
        return uploads.get(key);
    }
    
    boolean isClosed() {
        return closed;
    }
}
