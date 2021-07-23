package io.github.kwakiutlcs.wagon;

import software.amazon.awssdk.services.s3.S3Client;

/**
 * Produces instances of {@link S3Client}.
 */
@FunctionalInterface
interface S3ClientFactory {

    /**
     * Get a {@link S3Client} instance.
     * @return {@link S3Client}
     */
    S3Client getClient();
}
