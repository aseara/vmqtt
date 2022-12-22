package com.github.aseara.vmqtt.common;

public class TopicUtil {

    public static String trimTopic(String topic) {
        String trimTopic = topic.trim();
        int s = 0;
        int e = trimTopic.length();
        boolean sub = false;
        if (trimTopic.startsWith("/")) {
            sub = true;
            s = 1;
        }
        if (trimTopic.endsWith("/")) {
            sub = true;
            e = e - 1;
        }
        return sub ? trimTopic.substring(s, e) : trimTopic;
    }

    public static boolean checkMessageTopic(String topic) {
        return topic.indexOf('#') == -1 && topic.indexOf('+') == -1;
    }

    public static String[] splitTopic(String topic) {
        return topic.split("/");
    }

}
