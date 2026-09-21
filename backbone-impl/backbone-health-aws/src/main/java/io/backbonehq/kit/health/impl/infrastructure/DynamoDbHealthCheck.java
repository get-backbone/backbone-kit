package io.backbonehq.kit.health.impl.infrastructure;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

/**
 * Reusable health check for DynamoDB table accessibility.
 * <p>
 * This abstract base class verifies that specified DynamoDB tables are accessible.
 * If any table is inaccessible, the health check fails. Supports checking multiple tables.
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
 *                    public HealthCheck dynamoDbHealthCheck(DynamoDbClient dynamoDbClient) {
 *                    return new DynamoDbHealthCheck(dynamoDbClient, List.of("Users", "Orders")) {};
 *                    }
 *                    }
 *                    </pre>
 */
@Readiness
public abstract class DynamoDbHealthCheck implements HealthCheck
{
    private final List<String> tableNames = new ArrayList<>();

    private final DynamoDbClient dynamoDbClient;

    public DynamoDbHealthCheck(final DynamoDbClient dynamoDbClient, final List<String> tableNames)
    {
        this.dynamoDbClient = dynamoDbClient;
        this.tableNames.addAll(tableNames);
    }

    @Override
    public HealthCheckResponse call()
    {
        try
        {
            // Check all tables are accessible
            for (final String tableName : tableNames)
            {
                dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build());
            }

            return HealthCheckResponse.named("DynamoDB")
                .up()
                .withData("tables", String.join(", ", tableNames))
                .build();
        }
        catch (final DynamoDbException e)
        {
            final var responseBuilder = HealthCheckResponse.named("DynamoDB")
                .down();

            if (!tableNames.isEmpty())
            {
                responseBuilder.withData("tables", String.join(", ", tableNames));
            }

            return responseBuilder.withData("error", e.awsErrorDetails().errorMessage()).build();
        }
        catch (final Exception e)
        {
            final var responseBuilder = HealthCheckResponse.named("DynamoDB")
                .down();

            if (!tableNames.isEmpty())
            {
                responseBuilder.withData("tables", String.join(", ", tableNames));
            }

            return responseBuilder.withData("error", e.getMessage()).build();
        }
    }
}
