package com.example.skadi.plugin;

import com.dkay229.skadi.spi.S3ClientProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.net.URI;
import java.util.ServiceLoader;

public final class S3TopLevelListMain {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: S3TopLevelListMain <bucket-or-s3-uri>");
            System.err.println("Examples:");
            System.err.println("  s3://my-bucket");
            System.err.println("  s3://my-bucket/some/prefix/");
            System.err.println("  my-bucket");
            System.exit(2);
        }

        Target t = parseTarget(args[0]);

        S3ClientProvider provider = ServiceLoader.load(S3ClientProvider.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No S3ClientProvider found via ServiceLoader. " +
                                "Check META-INF/services/com.dkay229.skadi.spi.S3ClientProvider"));

        System.out.println("Provider: " + provider.id());
        System.out.println("Listing top-level for bucket=" + t.bucket + " prefix=" + (t.prefix == null ? "" : t.prefix));

        try (S3Client s3 = provider.createClient()) {
            // delimiter "/" gives “top-level” grouping under the prefix
            ListObjectsV2Request req = ListObjectsV2Request.builder()
                    .bucket(t.bucket)
                    .prefix(t.prefix == null ? "" : t.prefix)
                    .delimiter("/")
                    .maxKeys(1000)
                    .build();

            ListObjectsV2Response resp = s3.listObjectsV2(req);

            if (resp.commonPrefixes() != null && !resp.commonPrefixes().isEmpty()) {
                System.out.println("\nDirectories:");
                for (CommonPrefix cp : resp.commonPrefixes()) {
                    System.out.println("  " + cp.prefix());
                }
            } else {
                System.out.println("\nDirectories:\n  (none)");
            }

            if (resp.contents() != null && !resp.contents().isEmpty()) {
                System.out.println("\nObjects:");
                for (S3Object o : resp.contents()) {
                    // When delimiter is set, AWS still may include the “prefix placeholder object” if it exists.
                    System.out.println("  " + o.key() + "  (" + o.size() + " bytes)");
                }
            } else {
                System.out.println("\nObjects:\n  (none)");
            }

            if (Boolean.TRUE.equals(resp.isTruncated())) {
                System.out.println("\nNOTE: results truncated (more than maxKeys).");
            }
        }
    }

    private static Target parseTarget(String arg) {
        String bucket;
        String prefix = "";

        if (arg.startsWith("s3://")) {
            URI uri = URI.create(arg);
            bucket = uri.getHost();
            if (bucket == null || bucket.isBlank()) {
                throw new IllegalArgumentException("Invalid S3 URI (missing bucket): " + arg);
            }
            String path = uri.getPath(); // includes leading "/"
            if (path != null && path.length() > 1) {
                prefix = path.substring(1); // drop leading "/"
            }
        } else {
            bucket = arg;
            prefix = "";
        }

        // Normalize: keep prefix empty or ending with "/" to avoid weird partial-key matching
        if (!prefix.isEmpty() && !prefix.endsWith("/")) {
            prefix = prefix + "/";
        }

        return new Target(bucket, prefix);
    }

    private record Target(String bucket, String prefix) {}
}
