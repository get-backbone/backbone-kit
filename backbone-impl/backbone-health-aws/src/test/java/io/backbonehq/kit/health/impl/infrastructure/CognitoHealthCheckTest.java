package io.backbonehq.kit.health.impl.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolResponse;

class CognitoHealthCheckTest
{
    private final CognitoIdentityProviderClient cognitoClient = mock(CognitoIdentityProviderClient.class);

    @Test
    void call_WithSuccessfulUserPoolAccess_ReturnsUp()
    {
        when(cognitoClient.describeUserPool(any(DescribeUserPoolRequest.class)))
            .thenReturn(DescribeUserPoolResponse.builder().build());

        final CognitoHealthCheck healthCheck = new CognitoHealthCheck(cognitoClient, "us-east-1_ABC123", "MyUserPool")
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertEquals("Cognito", response.getName());
        assertSame(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("us-east-1_ABC123", response.getData().orElseThrow().get("userPoolId"));
        assertEquals("MyUserPool", response.getData().orElseThrow().get("userPoolName"));
    }

    @Test
    void call_VerifiesCorrectUserPoolId()
    {
        when(cognitoClient.describeUserPool(any(DescribeUserPoolRequest.class)))
            .thenReturn(DescribeUserPoolResponse.builder().build());

        final CognitoHealthCheck healthCheck = new CognitoHealthCheck(cognitoClient, "us-west-2_XYZ789", "TestPool")
        {
        };

        healthCheck.call();

        verify(cognitoClient).describeUserPool(DescribeUserPoolRequest.builder()
            .userPoolId("us-west-2_XYZ789")
            .build());
    }

    @Test
    void call_WithCognitoException_ReturnsDown()
    {
        final CognitoIdentityProviderException exception = mock(CognitoIdentityProviderException.class);
        final AwsErrorDetails errorDetails = AwsErrorDetails.builder()
            .errorMessage("User pool not found")
            .errorCode("ResourceNotFoundException")
            .build();
        when(exception.awsErrorDetails()).thenReturn(errorDetails);
        when(cognitoClient.describeUserPool(any(DescribeUserPoolRequest.class)))
            .thenThrow(exception);

        final CognitoHealthCheck healthCheck = new CognitoHealthCheck(cognitoClient, "us-east-1_ABC123", "MyUserPool")
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertEquals("us-east-1_ABC123", response.getData().orElseThrow().get("userPoolId"));
        assertEquals("MyUserPool", response.getData().orElseThrow().get("userPoolName"));
        assertTrue(response.getData().orElseThrow().containsKey("error"));
        assertEquals("User pool not found", response.getData().orElseThrow().get("error"));
    }

    @Test
    void call_WithGenericException_ReturnsDown()
    {
        when(cognitoClient.describeUserPool(any(DescribeUserPoolRequest.class)))
            .thenThrow(new RuntimeException("Connection failed"));

        final CognitoHealthCheck healthCheck = new CognitoHealthCheck(cognitoClient, "us-east-1_ABC123", "MyUserPool")
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertTrue(response.getData().orElseThrow().containsKey("error"));
        assertEquals("us-east-1_ABC123", response.getData().orElseThrow().get("userPoolId"));
        assertEquals("MyUserPool", response.getData().orElseThrow().get("userPoolName"));
    }
}
