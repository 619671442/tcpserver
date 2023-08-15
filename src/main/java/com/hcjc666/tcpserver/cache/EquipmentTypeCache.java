package com.hcjc666.tcpserver.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hcjc666.tcpserver.entity.EquipmentType;
/**
 * 未使用
 */
public class EquipmentTypeCache {
    public static volatile Map<String, EquipmentType> equipmentTypeCache = new ConcurrentHashMap<String, EquipmentType>();

    public static int size() {
        return equipmentTypeCache.size();
    }

    public static void add(String fid, EquipmentType temp) {
        equipmentTypeCache.put(fid, temp);
    }

    public static EquipmentType get(String fid) {
        return equipmentTypeCache.get(fid);
    }

    /**
     * 通过类型名称获取类型信息
     * @param type
     * @return
     */
    public static EquipmentType getByType(String type) {
        Set<Entry<String, EquipmentType>> entries = equipmentTypeCache.entrySet();
        Iterator<Entry<String, EquipmentType>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry<String, EquipmentType> entry = iterator.next();
            EquipmentType temp = entry.getValue();
            if (temp.getEquipmentType().equals(type)) {
                return temp;
            }
        }
        return null;
    }

    public static void remove(String fid) {
        equipmentTypeCache.remove(fid);
    }

    public static void clear() {
        equipmentTypeCache.clear();
    }

    public static void save(String fid, EquipmentType temp) {
        if (equipmentTypeCache.get(fid) == null) {
            add(fid, temp);
        }
    }
}
