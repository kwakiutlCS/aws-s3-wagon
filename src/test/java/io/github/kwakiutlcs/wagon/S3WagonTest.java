package io.github.kwakiutlcs.wagon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import software.amazon.awssdk.core.exception.SdkClientException;

class S3WagonTest {

    static String TEST_BUCKET = "test-bucket";
    static String KEY_PATH = "path";
    static String RESOURCE = "resource";
    
    private S3ClientSpy client = new S3ClientSpy(TEST_BUCKET, KEY_PATH+"/"+RESOURCE);
    
    private S3Wagon wagon = new S3Wagon(() -> client);
    
    @BeforeEach
    void init() throws ConnectionException, AuthenticationException {
        wagon.connect(new Repository("id", "s3://"+TEST_BUCKET+"/"+KEY_PATH));
        wagon.openConnectionInternal();
    }
    
    @Test
    void writeResourceToFileWhenGettingItFromS3() throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        var file = new File("src/test/resources/destiny");
        file.delete();
        
        wagon.get(RESOURCE, file);
        
        assertEquals(8, file.length());
    }
    
    @ParameterizedTest
    @ValueSource(longs = {0L, 1000L})
    void writeResourceToFileIfIsNewerThanGivenDate(long timestamp) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        var file = new File("src/test/resources/destiny");
        file.delete();
        
        var result = wagon.getIfNewer(RESOURCE, file, timestamp);
        
        assertTrue(result);
        assertEquals(8, file.length());
    }
    
    @ParameterizedTest
    @ValueSource(longs = {1001L, 3000L})
    void doNotWriteResourceToFileIfIsOlderThanGivenDate(long timestamp) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        var file = new File("src/test/resources/destiny");
        file.delete();
        
        var result = wagon.getIfNewer(RESOURCE, file, timestamp);
        
        assertFalse(result);
        assertEquals(0L, file.length());
    }
    
    @Test
    void uploadFileToS3Client() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        var file = new File("src/test/resources/origin");
        
        wagon.put(file, "resource");
    
        assertEquals(file, client.getUpload());
    }
    
    @Test
    void defaultConstructorShouldCreateADefaultS3Client() throws ConnectionException, AuthenticationException {
        var wagon = new S3Wagon();
        
        // should fail locally, because no AWS_REGION is defined
        assertThrows(SdkClientException.class, () -> wagon.openConnectionInternal());
    }
    
    @Test
    void closingWagonShouldCloseTheS3Client() throws ConnectionException {
        wagon.closeConnection();
       
        assertTrue(client.isClosed());
    }
}
