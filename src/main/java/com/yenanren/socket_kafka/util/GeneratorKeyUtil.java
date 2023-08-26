package com.yenanren.socket_kafka.util;

import java.util.HashMap;
import java.util.Map;

public class GeneratorKeyUtil {

    public static String makeKey(String userId, String chatroomId) {
        String conURL = "/topic/chatrooms/" + userId + "_" + chatroomId;
        return conURL;
    }

    public static String makeKey(int userId, int chatroomId) {
        String conURL = "/topic/chatrooms/" + userId + "_" + chatroomId;
        return conURL;
    }

    public static Map<String, String> parseKeyToMap(String conURL) {
        Map<String, String> idMap = new HashMap<>();

        String[] parts = conURL.split("/");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            String[] ids = lastPart.split("_");

            if (ids.length == 2) {
                idMap.put("userId", ids[0]);
                idMap.put("chatroomId", ids[1]);
            }
        }

        return idMap;
    }

}
