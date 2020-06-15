package org.example;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigValue;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.example.MySourceConnectorConfig.*;
import static org.junit.jupiter.api.Assertions.*;

class MySourceConnectorConfigTest {
    private final ConfigDef configDef = MySourceConnectorConfig.config();

    public Map<String, String> initialConfig() {
        Map<String, String> baseProps = new HashMap();
        baseProps.put(OWNER_CONFIG, "foo");
        baseProps.put(REPO_CONFIG, "bar");
        baseProps.put(SINCE_CONFIG, "2019-06-15T10:00:00Z");
        baseProps.put(BATCH_SIZE_CONFIG, "100");
        baseProps.put(TOPIC_CONFIG, "github-issues");
        return baseProps;
    }

    @Test
    public void initialConfigIsValid() {
        for (ConfigValue configValue : configDef.validate(initialConfig())) {
            assertTrue(configValue.errorMessages().isEmpty(), configValue.name() + " had error");
        }
    }

    @Test
    void canReadConfig() {
        MySourceConnectorConfig config = new MySourceConnectorConfig(initialConfig());
        assertEquals("foo", config.getOwner());
        assertEquals("bar", config.getRepo());
        assertEquals(Instant.parse("2019-06-15T10:00:00Z"), config.getSince());
        assertEquals(100, config.getBatchSize());
        assertEquals("github-issues", config.getTopic());
        assertEquals("", config.getAuthUsername());
        assertEquals("[hidden]", config.getAuthPassword());
    }

    @Test
    void validatesSince() {
        Map<String, String> config = initialConfig();
        config.put(SINCE_CONFIG, "haha");
        assertFalse(configDef.validateAll(config).get(SINCE_CONFIG).errorMessages().isEmpty());
    }

    @Test
    void validatesNegativeBatchSize() {
        Map<String, String> config = initialConfig();
        config.put(BATCH_SIZE_CONFIG, "-1");
        assertFalse(configDef.validateAll(config).get(BATCH_SIZE_CONFIG).errorMessages().isEmpty());
    }

    @Test
    void validatesTooBigBatchSize() {
        Map<String, String> config = initialConfig();
        config.put(BATCH_SIZE_CONFIG, "101");
        assertFalse(configDef.validateAll(config).get(BATCH_SIZE_CONFIG).errorMessages().isEmpty());
    }
}