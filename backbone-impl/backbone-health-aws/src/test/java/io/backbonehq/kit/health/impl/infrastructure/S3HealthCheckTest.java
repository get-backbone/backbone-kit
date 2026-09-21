package io.backbonehq.kit.health.impl.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

class S3HealthCheckTest
{
    private final S3Client s3Client = mock(S3Client.class);

    @Test
    void call_WithSuccessfulBucketAccess_ReturnsUp()
    {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenReturn(HeadBucketResponse.builder().build());

        final S3HealthCheck healthCheck = new S3HealthCheck(s3Client, List.of("my-bucket"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertEquals("S3", response.getName());
        assertSame(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("my-bucket", response.getData().orElseThrow().get("buckets"));
    }

    @Test
    void call_WithMultipleBuckets_ChecksAllBuckets()
    {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenReturn(HeadBucketResponse.builder().build());

        final S3HealthCheck healthCheck = new S3HealthCheck(s3Client, List.of("bucket1", "bucket2"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("bucket1, bucket2", response.getData().orElseThrow().get("buckets"));
        verify(s3Client).headBucket(HeadBucketRequest.builder().bucket("bucket1").build());
        verify(s3Client).headBucket(HeadBucketRequest.builder().bucket("bucket2").build());
    }

    @Test
    void call_WithNoSuchBucketException_ReturnsDown()
    {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(NoSuchBucketException.builder().message("Bucket not found").build());

        final S3HealthCheck healthCheck = new S3HealthCheck(s3Client, List.of("my-bucket"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertTrue(response.getData().orElseThrow().containsKey("error"));
        assertTrue(response.getData().orElseThrow().get("error").toString().contains("Bucket not found"));
    }

    @Test
    void call_WithS3Exception_ReturnsDown()
    {
        final S3Exception exception = mock(S3Exception.class);
        final AwsErrorDetails errorDetails = AwsErrorDetails.builder()
            .errorMessage("Access denied")
            .errorCode("AccessDenied")
            .build();
        when(exception.awsErrorDetails()).thenReturn(errorDetails);
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(exception);

        final S3HealthCheck healthCheck = new S3HealthCheck(s3Client, List.of("my-bucket"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertTrue(response.getData().orElseThrow().containsKey("error"));
        assertEquals("Access denied", response.getData().orElseThrow().get("error"));
    }

    @Test
    void call_WithGenericException_ReturnsDown()
    {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(new RuntimeException("Connection failed"));

        final S3HealthCheck healthCheck = new S3HealthCheck(s3Client, List.of("my-bucket"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertTrue(response.getData().orElseThrow().containsKey("error"));
    }
}
