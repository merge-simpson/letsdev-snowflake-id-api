package me.letsdev.persistence.id.snowflake;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

import static me.letsdev.persistence.id.snowflake.SnowflakeConstants.NETTEE_EPOCH;
import static me.letsdev.persistence.id.snowflake.SnowflakeProperties.PREFIX;

@ConfigurationProperties(PREFIX)
public record SnowflakeProperties(
        Long datacenterId,
        Long workerId,
        Long epoch
) {
    static final String PREFIX = "letsdev.persistence.snowflake";

    public SnowflakeProperties {
        Objects.requireNonNull(datacenterId, PREFIX + ".datacenter-id must not be null.");
        Objects.requireNonNull(workerId, PREFIX + ".worker-id must not be null.");

        if (epoch == null) {
            epoch = NETTEE_EPOCH;
        } else if (epoch < 0) {
            epoch = NETTEE_EPOCH;
        }
    }
}