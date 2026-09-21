package io.backbonehq.kit.health.impl.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostgresHealthCheckTest
{
    private final EntityManager entityManager = mock(EntityManager.class);

    private final Query query = mock(Query.class);

    @BeforeEach
    void setUp()
    {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    }

    @Test
    void call_WithSuccessfulConnection_ReturnsUp()
    {
        when(query.getSingleResult()).thenReturn(1);
        when(query.getResultList()).thenReturn(List.of(1));

        final PostgresHealthCheck healthCheck = new PostgresHealthCheck(entityManager, "mydb", List.of("users"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertEquals("PostgreSQL", response.getName());
        assertSame(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("mydb", response.getData().orElseThrow().get("database"));
        assertEquals("users", response.getData().orElseThrow().get("tables"));
    }

    @Test
    void call_WithMultipleTables_ChecksAllTables()
    {
        when(query.getSingleResult()).thenReturn(1);
        when(query.getResultList()).thenReturn(List.of(1));

        final PostgresHealthCheck healthCheck = new PostgresHealthCheck(entityManager, "mydb", List.of("users", "orders"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("users, orders", response.getData().orElseThrow().get("tables"));
        verify(entityManager).createNativeQuery("SELECT 1 FROM \"users\" LIMIT 1");
        verify(entityManager).createNativeQuery("SELECT 1 FROM \"orders\" LIMIT 1");
    }

    @Test
    void call_WithNoTables_OnlyChecksConnection()
    {
        when(query.getSingleResult()).thenReturn(1);

        final PostgresHealthCheck healthCheck = new PostgresHealthCheck(entityManager, "mydb", List.of())
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("mydb", response.getData().orElseThrow().get("database"));
        assertFalse(response.getData().orElseThrow().containsKey("tables"));
        verify(entityManager, never()).createNativeQuery("SELECT 1 FROM");
    }

    @Test
    void call_WithConnectionFailure_ReturnsDown()
    {
        when(query.getSingleResult()).thenThrow(new RuntimeException("Connection failed"));

        final PostgresHealthCheck healthCheck = new PostgresHealthCheck(entityManager, "mydb", List.of("users"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertEquals("PostgreSQL", response.getName());
        assertSame(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertEquals("mydb", response.getData().orElseThrow().get("database"));
        assertTrue(response.getData().orElseThrow().containsKey("error"));
    }

    @Test
    void call_WithTableAccessFailure_ReturnsDown()
    {
        when(query.getSingleResult()).thenReturn(1);
        when(query.getResultList()).thenThrow(new RuntimeException("Table does not exist"));

        final PostgresHealthCheck healthCheck = new PostgresHealthCheck(entityManager, "mydb", List.of("users"))
        {
        };

        final HealthCheckResponse response = healthCheck.call();

        assertSame(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertTrue(response.getData().orElseThrow().containsKey("error"));
    }
}
