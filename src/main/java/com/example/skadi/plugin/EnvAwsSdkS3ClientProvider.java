package com.example.skadi.plugin;

import com.dkay229.skadi.spi.S3ClientProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class EnvAwsSdkS3ClientProvider implements S3ClientProvider {

    @Override
    public String id() {
        return "example-default-aws-env";
    }

    @Override
    public S3Client createClient() {
        String region = System.getenv().getOrDefault("AWS_REGION",
                System.getenv().getOrDefault("AWS_DEFAULT_REGION", "us-east-1"));

        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(region))
                .build();
    }
}
