package com.github.aseara.vmqtt.topic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@Slf4j
public class TopicTest {

    @Test
    public void testTopicLevels() {
        String[] topics = new String[] {"", "test", "/", "/test", "/test/", "//test", "test//",
                "test1/test2", "test1//test2"};
        Arrays.stream(topics).forEach(topic -> {
            String[] levels = topic.split("/");
            log.info("levels of [{}]: levels {}, {}", topic, levels.length, levels);
        });

    }

}
