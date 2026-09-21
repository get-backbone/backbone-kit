package io.backbonehq.kit.http.aws;

/**
 * AWS SigV4 service signing names used by Backbone HTTP exporters.
 */
public enum AwsSigningServiceName
{
    /** Amazon Managed Prometheus remote write. */
    APS("aps"),

    /** AWS X-Ray OTLP trace ingestion. */
    XRAY("xray");

    private final String signingName;

    AwsSigningServiceName(final String signingName)
    {
        this.signingName = signingName;
    }

    /**
     * Returns the AWS SigV4 signing service name.
     */
    public String signingName()
    {
        return signingName;
    }
}
