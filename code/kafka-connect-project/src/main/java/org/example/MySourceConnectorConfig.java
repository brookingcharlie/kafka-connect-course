package org.example;

import com.github.jcustenborder.kafka.connect.utils.config.ConfigKeyBuilder;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigException;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class MySourceConnectorConfig extends AbstractConfig {
    public static final String TOPIC_CONFIG = "topic";
    private static final String TOPIC_DOC = "Topic to write to";

    public static final String OWNER_CONFIG = "github.owner";
    private static final String OWNER_DOC = "Owner of the repository you'd like to follow";

    public static final String REPO_CONFIG = "github.repo";
    private static final String REPO_DOC = "Repository you'd like to follow";

    public static final String SINCE_CONFIG = "since.timestamp";
    private static final String SINCE_DOC = "Only issues updated at or after this time are returned.\n" +
            "This is a timestamp in ISO 8601 format: YYY-MM-DDTHH:MM:SSZ.\n" +
            "Defaults to a year from first launch.";

    public static final String BATCH_SIZE_CONFIG = "batch.size";
    private static final String BATCH_SIZE_DOC = "Number of data points to retrieve at a time.\n" +
            "Defaults to 100 (max value)";

    public static final String AUTH_USERNAME_CONFIG = "auth.username";
    private static final String AUTH_USERNAME_DOC = "Optional username to authenticate calls";

    public static final String AUTH_PASSWORD_CONFIG = "auth.password";
    private static final String AUTH_PASSWORD_DOC = "Optional password to authenticate calls";

    private static class BatchSizeValidator implements ConfigDef.Validator {
        @Override
        public void ensureValid(String name, Object value) {
            Integer batchSize = (Integer) value;
            if (batchSize < 1 || batchSize > 100) {
                throw new ConfigException(name, value, "Batch Size must be an integer between 1 and 100");
            }
        }
    }

    private static class TimestampValidator implements ConfigDef.Validator {
        @Override
        public void ensureValid(String name, Object value) {
            String timestamp = (String) value;
            try {
                Instant.parse(timestamp);
            } catch (DateTimeParseException e) {
                throw new ConfigException(name, value, "Timestamp must be in ISO-8601 format");
            }
        }
    }

    public MySourceConnectorConfig(Map<?, ?> originals) {
        super(config(), originals);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(
                        ConfigKeyBuilder.of(TOPIC_CONFIG, Type.STRING)
                                .importance(Importance.HIGH)
                                .documentation(TOPIC_DOC)
                                .build()
                )
                .define(
                        ConfigKeyBuilder.of(OWNER_CONFIG, Type.STRING)
                                .importance(Importance.HIGH)
                                .documentation(OWNER_DOC)
                                .build()
                )
                .define(
                        ConfigKeyBuilder.of(REPO_CONFIG, Type.STRING)
                                .importance(Importance.HIGH)
                                .documentation(REPO_DOC)
                                .build()
                )
                .define(
                        ConfigKeyBuilder.of(BATCH_SIZE_CONFIG, Type.INT)
                                .defaultValue(100)
                                .validator(new BatchSizeValidator())
                                .importance(Importance.LOW)
                                .documentation(BATCH_SIZE_DOC)
                                .build()
                )
                .define(
                        ConfigKeyBuilder.of(SINCE_CONFIG, Type.STRING)
                                .defaultValue(ZonedDateTime.now().minusYears(1).toInstant().toString())
                                .validator(new TimestampValidator())
                                .importance(Importance.HIGH)
                                .documentation(SINCE_DOC)
                                .build()
                )
                .define(
                        ConfigKeyBuilder.of(AUTH_USERNAME_CONFIG, Type.STRING)
                                .defaultValue("")
                                .importance(Importance.HIGH)
                                .documentation(AUTH_USERNAME_DOC)
                                .build()
                )
                .define(
                        ConfigKeyBuilder.of(AUTH_PASSWORD_CONFIG, Type.PASSWORD)
                                .defaultValue("")
                                .importance(Importance.HIGH)
                                .documentation(AUTH_PASSWORD_DOC)
                                .build()
                );
    }

    public String getTopic() {
        return this.getString(TOPIC_CONFIG);
    }

    public String getOwner() {
        return this.getString(OWNER_CONFIG);
    }

    public String getRepo() {
        return this.getString(REPO_CONFIG);
    }

    public Integer getBatchSize() {
        return this.getInt(BATCH_SIZE_CONFIG);
    }

    public Instant getSince() {
        return Instant.parse(this.getString(SINCE_CONFIG));
    }

    public String getAuthUsername() {
        return this.getString(AUTH_USERNAME_CONFIG);
    }

    public String getAuthPassword() {
        return this.getPassword(AUTH_PASSWORD_CONFIG).toString();
    }
}
