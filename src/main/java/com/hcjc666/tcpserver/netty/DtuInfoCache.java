package com.hcjc666.tcpserver.netty;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hcjc666.tcpserver.entity.DtuInfo;

public class DtuInfoCache {
    public static volatile Map<String, DtuInfo> dtuinfoMapCache = new ConcurrentHashMap<String, DtuInfo>();

    public static int size() {
        return dtuinfoMapCache.size();
    }

    public static void add(String imei, DtuInfo channel) {
        dtuinfoMapCache.put(imei, channel);
    }

    public static DtuInfo get(String imei) {
        return dtuinfoMapCache.get(imei);
    }

    public static DtuInfo getByRegistered(String registered) {
        Set<Entry<String, DtuInfo>> entries = dtuinfoMapCache.entrySet();
        Iterator<Entry<String, DtuInfo>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry<String, DtuInfo> entry = iterator.next();
            DtuInfo temp = entry.getValue();
            if (temp.getRegistered().equals(registered)) {
                return temp;
            }
        }
        return null;
    }

    public static void remove(String imei) {
        dtuinfoMapCache.remove(imei);
    }

    public static void clear() {
        dtuinfoMapCache.clear();
    }

    public static void save(String imei, DtuInfo channel) {
        if (dtuinfoMapCache.get(imei) == null) {
            add(imei, channel);
        }
    }
}
