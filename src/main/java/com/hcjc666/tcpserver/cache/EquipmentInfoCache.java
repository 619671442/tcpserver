package com.hcjc666.tcpserver.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hcjc666.tcpserver.entity.EquipmentInfo;
/**
 * 未使用
 */
public class EquipmentInfoCache {
    public static volatile Map<String, EquipmentInfo> equipmentInfoCache = new ConcurrentHashMap<String, EquipmentInfo>();

    public static int size() {
        return equipmentInfoCache.size();
    }

    public static void add(String fid, EquipmentInfo temp) {
        equipmentInfoCache.put(fid, temp);
    }

    public static EquipmentInfo get(String fid) {
        return equipmentInfoCache.get(fid);
    }

    /**
     * 通过dtu设备号和地址取设备
     * 
     * @param registered
     * @return
     */
    public static EquipmentInfo getByiImeiAndAddr(String fid, String modbusAdrr) {
        Set<Entry<String, EquipmentInfo>> entries = equipmentInfoCache.entrySet();
        Iterator<Entry<String, EquipmentInfo>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry<String, EquipmentInfo> entry = iterator.next();
            EquipmentInfo temp = entry.getValue();
            if (temp.getDtuImei().equals(fid) && temp.getModbusAddr().equals(modbusAdrr)) {
                return temp;
            }
        }
        return null;
    }

    public static void remove(String fid) {
        equipmentInfoCache.remove(fid);
    }

    public static void clear() {
        equipmentInfoCache.clear();
    }

    public static void save(String fid, EquipmentInfo temp) {
        if (equipmentInfoCache.get(fid) == null) {
            add(fid, temp);
        }
    }
}
