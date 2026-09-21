package io.backbonehq.kit.health.impl.infrastructure;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Reusable health check for PostgreSQL database connectivity and table accessibility.
 * <p>
 * This abstract base class verifies database connectivity and checks that specified tables are accessible.
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
 *                    public HealthCheck databaseHealthCheck(EntityManager entityManager) {
 *                    return new PostgresHealthCheck(entityManager, "mydb", List.of("users", "orders")) {};
 *                    }
 *                    }
 *                    </pre>
 */
@Readiness
public abstract class PostgresHealthCheck implements HealthCheck
{
    private final List<String> tableNames = new ArrayList<>();

    private final EntityManager entityManager;

    private final String databaseName;

    protected PostgresHealthCheck(final EntityManager entityManager, final String databaseName, final List<String> tableNames)
    {
        this.entityManager = entityManager;
        this.databaseName = databaseName;
        this.tableNames.addAll(tableNames);
    }

    @Override
    public HealthCheckResponse call()
    {
        try
        {
            // First verify basic database connectivity
            this.entityManager.createNativeQuery("SELECT 1").getSingleResult();

            // Check all specified tables are accessible
            for (final String tableName : tableNames)
            {
                // Use a simple query to verify table exists and is accessible
                // Use getResultList() instead of getSingleResult() to handle empty tables gracefully
                // If table doesn't exist, this will throw an exception (which we catch)
                // If table exists but is empty, getResultList() returns empty list (no exception)
                // Use double quotes to properly escape table names (PostgreSQL identifier quoting)
                this.entityManager.createNativeQuery("SELECT 1 FROM \"" + tableName + "\" LIMIT 1").getResultList();
            }

            final var responseBuilder = HealthCheckResponse.named("PostgreSQL")
                .up()
                .withData("database", databaseName);

            if (!tableNames.isEmpty())
            {
                responseBuilder.withData("tables", String.join(", ", tableNames));
            }

            return responseBuilder.build();
        }
        catch (final Exception e)
        {
            final var responseBuilder = HealthCheckResponse.named("PostgreSQL")
                .down()
                .withData("database", databaseName);

            if (CollectionUtils.isNotEmpty(tableNames))
            {
                responseBuilder.withData("tables", String.join(", ", tableNames));
            }

            return responseBuilder.withData("error", e.getMessage()).build();
        }
    }
}
