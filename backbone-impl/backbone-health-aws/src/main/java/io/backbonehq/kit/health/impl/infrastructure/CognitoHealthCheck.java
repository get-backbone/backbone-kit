package io.backbonehq.kit.health.impl.infrastructure;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolRequest;

/**
 * Reusable health check for AWS Cognito User Pool accessibility.
 * <p>
 * This abstract base class verifies that a Cognito User Pool is accessible by calling
 * {@code describeUserPool}. If the user pool is inaccessible, the health check fails.
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
 *                    public HealthCheck cognitoHealthCheck(CognitoIdentityProviderClient cognitoClient) {
 *                    return new CognitoHealthCheck(cognitoClient, "us-east-1_ABC123", "MyUserPool") {};
 *                    }
 *                    }
 *                    </pre>
 */
@Readiness
public abstract class CognitoHealthCheck implements HealthCheck
{
    private final CognitoIdentityProviderClient cognitoClient;

    private final String userPoolId;

    private final String userPoolName;

    public CognitoHealthCheck(final CognitoIdentityProviderClient cognitoClient, final String userPoolId, final String userPoolName)
    {
        this.cognitoClient = cognitoClient;
        this.userPoolId = userPoolId;
        this.userPoolName = userPoolName;
    }

    @Override
    public HealthCheckResponse call()
    {
        try
        {
            // Verify user pool is accessible
            // The health check verifies accessibility by successfully calling describeUserPool
            cognitoClient.describeUserPool(DescribeUserPoolRequest.builder()
                .userPoolId(userPoolId)
                .build());

            return HealthCheckResponse.named("Cognito")
                .up()
                .withData("userPoolId", userPoolId)
                .withData("userPoolName", userPoolName)
                .build();
        }
        catch (final CognitoIdentityProviderException e)
        {
            return HealthCheckResponse.named("Cognito")
                .down()
                .withData("userPoolId", userPoolId)
                .withData("userPoolName", userPoolName)
                .withData("error", e.awsErrorDetails().errorMessage())
                .build();
        }
        catch (final Exception e)
        {
            return HealthCheckResponse.named("Cognito")
                .down()
                .withData("userPoolId", userPoolId)
                .withData("userPoolName", userPoolName)
                .withData("error", e.getMessage())
                .build();
        }
    }
}
