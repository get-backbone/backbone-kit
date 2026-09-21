package io.backbonehq.kit.health.impl.infrastructure;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Reusable health check for S3 bucket accessibility.
 * <p>
 * This abstract base class verifies that specified S3 buckets are accessible.
 * If any bucket is inaccessible, the health check fails. Supports checking multiple buckets.
 * <p>
 * Services should create instances using {@code @Produces} methods that return anonymous class instances.
 * The {@code @Readiness} annotation must be on the {@code @Produces} method, not on this abstract class.
 * <p>
 * Example:
 * <pre>
 * {@code
 * @Produces
 *
 * @Readiness
 * @ApplicationScoped
 *                    public HealthCheck s3HealthCheck(S3Client s3Client) {
 *                    return new S3HealthCheck(s3Client, List.of("my-bucket", "backup-bucket")) {};
 *                    }
 *                    }
 *                    </pre>
 */
@Readiness
public abstract class S3HealthCheck implements HealthCheck
{
    private final List<String> bucketNames = new ArrayList<>();

    private final S3Client s3Client;

    public S3HealthCheck(final S3Client s3Client, final List<String> bucketNames)
    {
        this.s3Client = s3Client;
        this.bucketNames.addAll(bucketNames);
    }

    @Override
    public HealthCheckResponse call()
    {
        try
        {
            // Check all buckets are accessible
            for (final String bucketName : bucketNames)
            {
                this.s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            }

            return HealthCheckResponse.named("S3")
                .up()
                .withData("buckets", String.join(", ", bucketNames))
                .build();
        }
        catch (final NoSuchBucketException e)
        {
            return HealthCheckResponse.named("S3")
                .down()
                .withData("error", "Bucket not found: " + e.getMessage())
                .build();
        }
        catch (final S3Exception e)
        {
            return HealthCheckResponse.named("S3")
                .down()
                .withData("error", e.awsErrorDetails().errorMessage())
                .build();
        }
        catch (final Exception e)
        {
            return HealthCheckResponse.named("S3")
                .down()
                .withData("error", e.getMessage())
                .build();
        }
    }
}
