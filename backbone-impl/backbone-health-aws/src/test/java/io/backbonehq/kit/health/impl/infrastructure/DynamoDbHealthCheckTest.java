package io.backbonehq.kit.health.impl.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

class DynamoDbHealthCheckTest
{
    private final DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);

    @Test
    void call_WithSuccessfulTableAccess_ReturnsUp()
    {
        when(dynamoDbClient.describeTable(any(DescribeTableRequest.class)))
            .thenReturn(DescribeTableResponse.builder().build());

        final DynamoDbHealthCheck healthCheck = new DynamoDbHealthCheck(dynamoDbClient, List.of("Users"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertEquals("DynamoDB", response.getName());
        assertSame(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("Users", response.getData().orElseThrow().get("tables"));
    }

    @Test
    void call_WithMultipleTables_ChecksAllTables()
    {
        when(dynamoDbClient.describeTable(any(DescribeTableRequest.class)))
            .thenReturn(DescribeTableResponse.builder().build());

        final DynamoDbHealthCheck healthCheck = new DynamoDbHealthCheck(dynamoDbClient, List.of("Users", "Orders"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("Users, Orders", response.getData().orElseThrow().get("tables"));
        verify(dynamoDbClient).describeTable(DescribeTableRequest.builder().tableName("Users").build());
        verify(dynamoDbClient).describeTable(DescribeTableRequest.builder().tableName("Orders").build());
    }

    @Test
    void call_WithEmptyTableList_ReturnsUpWithEmptyTables()
    {
        final DynamoDbHealthCheck healthCheck = new DynamoDbHealthCheck(dynamoDbClient, List.of())
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("", response.getData().orElseThrow().get("tables"));
    }

    @Test
    void call_WithDynamoDbException_ReturnsDown()
    {
        final DynamoDbException exception = mock(DynamoDbException.class);
        final AwsErrorDetails errorDetails = AwsErrorDetails.builder()
            .errorMessage("Table not found")
            .errorCode("ResourceNotFoundException")
            .build();
        when(exception.awsErrorDetails()).thenReturn(errorDetails);
        when(dynamoDbClient.describeTable(any(DescribeTableRequest.class)))
            .thenThrow(exception);

        final DynamoDbHealthCheck healthCheck = new DynamoDbHealthCheck(dynamoDbClient, List.of("Users"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertTrue(response.getData().orElseThrow().containsKey("error"));
        assertTrue(response.getData().orElseThrow().containsKey("tables"));
        assertEquals("Table not found", response.getData().orElseThrow().get("error"));
    }

    @Test
    void call_WithGenericException_ReturnsDown()
    {
        when(dynamoDbClient.describeTable(any(DescribeTableRequest.class)))
            .thenThrow(new RuntimeException("Connection failed"));

        final DynamoDbHealthCheck healthCheck = new DynamoDbHealthCheck(dynamoDbClient, List.of("Users"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertTrue(response.getData().orElseThrow().containsKey("error"));
    }
}
