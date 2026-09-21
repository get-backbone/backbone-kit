package io.backbonehq.kit.metrics.impl.infrastructure;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class MetricsHostNameTest
{
    @Test
    void localHostName_returnsNonBlankValue()
    {
        assertFalse(MetricsHostName.localHostName().isBlank());
    }
}
