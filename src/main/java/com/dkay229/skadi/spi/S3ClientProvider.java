package com.dkay229.skadi.spi;

import software.amazon.awssdk.services.s3.S3Client;

public interface S3ClientProvider {
    String id();
    S3Client createClient();
}
