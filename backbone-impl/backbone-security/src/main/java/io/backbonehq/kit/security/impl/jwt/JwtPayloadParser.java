package io.backbonehq.kit.security.impl.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.backbonehq.kit.common.impl.lang.Base64Utils;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

public final class JwtPayloadParser
{
    private static final Logger LOGGER = Logger.getLogger(JwtPayloadParser.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JwtPayloadParser()
    {}

    public static Optional<JsonNode> parsePayload(final String jwtToken)
    {
        try
        {
            if (StringUtils.isNotBlank(jwtToken))
            {
                final String[] parts = jwtToken.split("\\.");
                if (parts.length != 3)
                {
                    return Optional.empty();
                }

                final byte[] decoded = Base64.getUrlDecoder()
                    .decode(Base64Utils.addBase64Padding(parts[1]));

                return Optional.of(
                    MAPPER.readTree(new String(decoded, StandardCharsets.UTF_8))
                );
            }
        }
        catch (final JsonProcessingException | IllegalArgumentException e)
        {
            LOGGER.errorf("unable to process token: %s", jwtToken, e.getMessage());
        }

        return Optional.empty();
    }
}
