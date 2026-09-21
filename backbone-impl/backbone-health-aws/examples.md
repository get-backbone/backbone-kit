# Backbone Health Checks

Reusable health check base classes for MicroProfile Health, providing production-ready health checks for common infrastructure components.

## Overview

This module provides abstract base classes for implementing health checks in Quarkus/MicroProfile applications.  
The health checks verify connectivity and accessibility of:

- **PostgreSQL** - Database connectivity and table accessibility
- **DynamoDB** - Table accessibility
- **S3** - Bucket accessibility
- **AWS Cognito** - User Pool accessibility

## Features

- ✅ **Production-Ready** - Battle-tested in production environments
- ✅ **Generic** - No domain-specific logic, works for any application
- ✅ **Multiple Resources** - Supports checking multiple tables/buckets
- ✅ **Comprehensive Error Handling** - Detailed error messages in health responses
- ✅ **Zero Dependencies** - Uses provided scope for all dependencies

## Usage

### PostgreSQL Health Check

```java
@Produces
@Readiness
@ApplicationScoped
public HealthCheck databaseHealthCheck(EntityManager entityManager) {
    return new PostgresHealthCheck(entityManager, "mydb", List.of("users", "orders")) {};
}
```

### DynamoDB Health Check

```java
@Produces
@Readiness
@ApplicationScoped
public HealthCheck dynamoDbHealthCheck(DynamoDbClient dynamoDbClient) {
    return new DynamoDbHealthCheck(dynamoDbClient, List.of("Users", "Orders")) {};
}
```

### S3 Health Check

```java
@Produces
@Readiness
@ApplicationScoped
public HealthCheck s3HealthCheck(S3Client s3Client) {
    return new S3HealthCheck(s3Client, List.of("my-bucket", "backup-bucket")) {};
}
```

### Cognito Health Check

```java
@Produces
@Readiness
@ApplicationScoped
public HealthCheck cognitoHealthCheck(CognitoIdentityProviderClient cognitoClient) {
    return new CognitoHealthCheck(cognitoClient, "us-east-1_ABC123", "MyUserPool") {};
}
```

## Dependencies

This module uses **provided scope** for all dependencies. Your application must include:

- `org.eclipse.microprofile.health:microprofile-health-api` (or Quarkus SmallRye Health)
- `software.amazon.awssdk:dynamodb` (for DynamoDB health check)
- `software.amazon.awssdk:s3` (for S3 health check)
- `software.amazon.awssdk:cognitoidentityprovider` (for Cognito health check)
- `jakarta.persistence:jakarta.persistence-api` (for PostgreSQL health check)

## Maven Dependency

```xml
<dependency>
    <groupId>io.backbonehq</groupId>
    <artifactId>backbone-health</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Health Check Endpoints

Once registered, health checks are automatically available at:

- `/q/health/ready` - Readiness checks (includes all `@Readiness` checks)
- `/q/health/live` - Liveness checks
- `/q/health/started` - Startup checks

## Example Response

```json
{
  "status": "UP",
  "checks": [
    {
      "name": "PostgreSQL",
      "status": "UP",
      "data": {
        "database": "mydb",
        "tables": "users, orders"
      }
    }
  ]
}
```

## License

[Add your license here]
