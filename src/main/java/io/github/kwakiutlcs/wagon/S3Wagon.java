package io.github.kwakiutlcs.wagon;

import java.io.File;

import org.apache.maven.wagon.AbstractWagon;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;

/**
 * Extends #{link AbstractWagon} with AWS S3 read and writer operations.
 */
public class S3Wagon extends AbstractWagon {

    @Override
    public void get(String resourceName, File destination)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean getIfNewer(String resourceName, File destination, long timestamp)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void put(File source, String destination)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void openConnectionInternal() throws ConnectionException, AuthenticationException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void closeConnection() throws ConnectionException {
        // TODO Auto-generated method stub
        
    }
}
