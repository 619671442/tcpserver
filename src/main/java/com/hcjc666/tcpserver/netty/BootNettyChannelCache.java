package com.hcjc666.tcpserver.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BootNettyChannelCache {

    public static volatile Map<String, BootNettyChannel> channelMapCache = new ConcurrentHashMap<String, BootNettyChannel>();

    public static void add(String code, BootNettyChannel channel) {
        channelMapCache.put(code, channel);
    }

    public static BootNettyChannel get(String code) {
        return channelMapCache.get(code);
    }

    public static void remove(String code) {
        channelMapCache.remove(code);
    }

    public static void save(String code, BootNettyChannel channel) {
        if (channelMapCache.get(code) == null) {
            add(code, channel);
        }
    }

}