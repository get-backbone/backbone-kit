package io.backbonehq.kit.health.impl.infrastructure;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite for health check implementations.
 * <p>
 * Groups all health check test classes together and pre-initializes the HealthCheckResponse provider
 * via a static initializer. This ensures the ServiceLoader lookup happens once when the suite class
 * is first loaded, before any test classes run, avoiding ServiceLoader overhead in each test class.
 * <p>
 * The static initializer pre-initializes all health check names that will be used in tests:
 * "Cognito", "DynamoDB", "PostgreSQL", and "S3".
 */
@Suite
@SelectClasses({CognitoHealthCheckTest.class, DynamoDbHealthCheckTest.class, PostgresHealthCheckTest.class, S3HealthCheckTest.class
})
public final class HealthCheckTestSuite
{
    static
    {
        // Static initializer runs when suite class is first loaded, before any tests
        // Pre-initialize all health check names that will be used in tests
        // This avoids ServiceLoader overhead in each test class
        HealthCheckResponse.named("Cognito").up().build();
        HealthCheckResponse.named("DynamoDB").up().build();
        HealthCheckResponse.named("PostgreSQL").up().build();
        HealthCheckResponse.named("S3").up().build();
    }
}
