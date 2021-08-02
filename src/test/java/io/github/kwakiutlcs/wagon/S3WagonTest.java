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
    
    private S3ClientSpy client = S3ClientSpy.of(TEST_BUCKET, KEY_PATH+"/"+RESOURCE);
    
    private S3Wagon wagon = new S3Wagon(() -> client);
    
    @BeforeEach
    void init() throws ConnectionException, AuthenticationException {
        connect(wagon);
    }
    
    @Test
    void writeResourceToFileWhenGettingItFromS3() throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        var file = new File("src/test/resources/destiny");
        
        wagon.get(RESOURCE, file);
        
        assertEquals(8, file.length());

        // cleanup
        file.delete();
    }
    
    @Test
    void doNotWriteResourceToFileWhenGettingIfKeyDoesNotExist() throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException, ConnectionException, AuthenticationException {
        wagon = connect(new S3Wagon(() -> S3ClientSpy.noKey()));
        
        var file = new File("src/test/resources/destiny");
        
        wagon.get(RESOURCE, file);
        
        assertEquals(0, file.length());
    }
    
    @ParameterizedTest
    @ValueSource(longs = {0L, 1000L})
    void writeResourceToFileIfIsNewerThanGivenDate(long timestamp) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        var file = new File("src/test/resources/destiny");
        
        var result = wagon.getIfNewer(RESOURCE, file, timestamp);
        
        assertTrue(result);
        assertEquals(8, file.length());

        // cleanup
        file.delete();
    }
    
    @ParameterizedTest
    @ValueSource(longs = {1001L, 3000L})
    void doNotWriteResourceToFileIfIsOlderThanGivenDate(long timestamp) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        var file = new File("src/test/resources/destiny");
        
        var result = wagon.getIfNewer(RESOURCE, file, timestamp);
        
        assertFalse(result);
        assertEquals(0L, file.length());
    }
    
    @Test
    void doNotWriteResourceToFileWhenGettingNewerFileIfKeyDoesNotExist() throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException, ConnectionException, AuthenticationException {
        wagon = connect(new S3Wagon(() -> S3ClientSpy.noKey()));
        
        var file = new File("src/test/resources/destiny");
        
        wagon.getIfNewer(RESOURCE, file, 0L);
        
        assertEquals(0, file.length());
    }
    
    @Test
    void uploadFileToS3Client() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        var file = new File("src/test/resources/origin");
        
        wagon.put(file, "resource");
    
        assertEquals(file, client.getUpload("path/resource"));
    }
    
    @Test
    void uploadDirectoryToS3Client() throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        var file = new File("src/test/resources/directory");
        var fileA = new File("src/test/resources/directory/a");
        var fileB = new File("src/test/resources/directory/other/b");
        
        wagon.putDirectory(file, "resource");
    
        assertEquals(fileA, client.getUpload("path/resource/a"));
        assertEquals(fileB, client.getUpload("path/resource/other/b"));
    }
    
    @Test
    void throwsSDKExceptionWhenErrorCopyingDirectory() {
        assertThrows(TransferFailedException.class, () -> wagon.putDirectory(new File("inexistent"), "resource"));
    }
    
    @Test
    void shouldImplementDirectoryCopy() {
        assertTrue(wagon.supportsDirectoryCopy());
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
    
    private S3Wagon connect(S3Wagon wagon) throws ConnectionException, AuthenticationException {
        wagon.connect(new Repository("id", "s3://"+TEST_BUCKET+"/"+KEY_PATH));
        wagon.openConnectionInternal();
        
        return wagon;
    }
}
