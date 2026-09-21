package io.backbonehq.kit.examples.health;

import io.backbonehq.kit.health.impl.infrastructure.CognitoHealthCheck;
import io.backbonehq.kit.health.impl.infrastructure.DynamoDbHealthCheck;
import io.backbonehq.kit.health.impl.infrastructure.PostgresHealthCheck;
import io.backbonehq.kit.health.impl.infrastructure.S3HealthCheck;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.Readiness;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;

/**
 * Example demonstrating how to create health check producers
 * using the backbone-health-aws health check base classes.
 * 
 * <p>This example shows how to:
 * <ul>
 *   <li>Create {@code @Produces} methods for health checks</li>
 *   <li>Use the {@code @Readiness} annotation to mark readiness checks</li>
 *   <li>Instantiate health check base classes with anonymous class syntax</li>
 *   <li>Configure health checks for multiple AWS services</li>
 * </ul>
 * 
 * <p>Once registered, health checks are automatically available at:
 * <ul>
 *   <li>{@code /q/health/ready} - Readiness checks (includes all {@code @Readiness} checks)</li>
 *   <li>{@code /q/health/live} - Liveness checks</li>
 * </ul>
 */
@ApplicationScoped
public class HealthCheckProducersExample
{
    /**
     * PostgreSQL database health check producer.
     * 
     * <p>This health check verifies:
     * <ul>
     *   <li>Database connectivity</li>
     *   <li>Accessibility of specified tables</li>
     * </ul>
     * 
     * @param entityManager The JPA EntityManager (injected by Quarkus)
     * @return A PostgreSQL health check instance
     */
    @Produces
    @Readiness
    @ApplicationScoped
    public HealthCheck databaseHealthCheck(EntityManager entityManager)
    {
        return new PostgresHealthCheck(
            entityManager,
            "mydb",  // Database name
            List.of("users", "orders", "products")  // Tables to check
        ) {};
    }

    /**
     * DynamoDB health check producer.
     * 
     * <p>This health check verifies that specified DynamoDB tables are accessible.
     * 
     * @param dynamoDbClient The DynamoDB client (injected by Quarkus)
     * @return A DynamoDB health check instance
     */
    @Produces
    @Readiness
    @ApplicationScoped
    public HealthCheck dynamoDbHealthCheck(DynamoDbClient dynamoDbClient)
    {
        return new DynamoDbHealthCheck(
            dynamoDbClient,
            List.of("Users", "Orders", "Products")  // Tables to check
        ) {};
    }

    /**
     * S3 health check producer.
     * 
     * <p>This health check verifies that specified S3 buckets are accessible.
     * 
     * @param s3Client The S3 client (injected by Quarkus)
     * @return An S3 health check instance
     */
    @Produces
    @Readiness
    @ApplicationScoped
    public HealthCheck s3HealthCheck(S3Client s3Client)
    {
        return new S3HealthCheck(
            s3Client,
            List.of("my-bucket", "backup-bucket", "uploads")  // Buckets to check
        ) {};
    }

    /**
     * AWS Cognito health check producer.
     * 
     * <p>This health check verifies that a Cognito User Pool is accessible.
     * 
     * @param cognitoClient The Cognito client (injected by Quarkus)
     * @return A Cognito health check instance
     */
    @Produces
    @Readiness
    @ApplicationScoped
    public HealthCheck cognitoHealthCheck(CognitoIdentityProviderClient cognitoClient)
    {
        return new CognitoHealthCheck(
            cognitoClient,
            "us-east-1_ABC123",  // User Pool ID
            "MyUserPool"  // User Pool name (for display purposes)
        ) {};
    }
}
