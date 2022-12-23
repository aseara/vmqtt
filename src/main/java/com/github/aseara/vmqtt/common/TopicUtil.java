package com.github.aseara.vmqtt.common;

public class TopicUtil {

    /**
     * trim topic name. This implementation is not consistent with the standard for the sake of simplicity.
     * @param topic topic name
     * @return trimmed topic name
     */
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
        if (sub) {
            trimTopic = trimTopic.substring(s, e);
        }
        return trimTopic.replaceAll("//", "/");
    }

    public static boolean checkMessageTopic(String topic) {
        return topic.indexOf('#') == -1 && topic.indexOf('+') == -1;
    }

    public static String[] splitTopic(String topic) {
        return topic.split("/");
    }

}
