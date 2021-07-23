package io.github.kwakiutlcs.wagon;

import java.io.File;

import org.apache.maven.wagon.AbstractWagon;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.resource.Resource;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Extends {@link AbstractWagon} with AWS S3 read and write operations.
 */
public class S3Wagon extends AbstractWagon {

    private S3Client client;
    
    private S3ClientFactory factory;
    
    /**
     * Constructor for {@link S3Wagon}.
     * Allows to inject arbitary instances of {@link S3Client}.
     * @param factory functional interface that generates {@link S3Client} instances.
     */
    S3Wagon(S3ClientFactory factory) {
        this.factory = factory;
    }
    
    /**
     * Default constructor for {@link S3Wagon}.
     */
    public S3Wagon() {
        this.factory = () -> S3Client.builder().build();
    }

    @Override
    public void get(String resourceName, File destination)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        var s3File = getS3File(resourceName);
        
        getTransfer(new Resource(resourceName), destination, s3File.asInputStream());
    }

    @Override
    public boolean getIfNewer(String resourceName, File destination, long timestamp)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        var s3File = getS3File(resourceName);
        
        if (s3File.response().lastModified().getEpochSecond() >= timestamp) {
            getTransfer(new Resource(resourceName), destination, s3File.asInputStream());

            return true;
        }

        return false;
    }
    
    @Override
    public void put(File source, String destination)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        var request = PutObjectRequest.builder()
                                      .bucket(repository.getHost())
                                      .key(getFullKeyPath(destination))
                                      .build();
       
        client.putObject(request, source.toPath());
    }

    @Override
    protected void openConnectionInternal() throws ConnectionException, AuthenticationException {
        this.client = factory.getClient();
    }

    @Override
    protected void closeConnection() throws ConnectionException {
        this.client.close();
    }
    
    private ResponseBytes<GetObjectResponse> getS3File(String resourceName) {
        var request = GetObjectRequest.builder()
                                      .bucket(repository.getHost())
                                      .key(getFullKeyPath(resourceName))
                                      .build();

        return client.getObjectAsBytes(request);
    }

    private String getFullKeyPath(String resourceName) {
        return repository.getBasedir().substring(1)+"/"+resourceName;
    }
}
