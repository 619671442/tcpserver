package com.hcjc666.tcpserver.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hcjc666.tcpserver.entity.EquipmentInfo;
import com.hcjc666.tcpserver.mapper.EquipmentInfoMapper;

@Service
public class EquipmentInfoService {
    @Autowired
    private EquipmentInfoMapper equipmentInfoMapper;

    public List<EquipmentInfo> getList() {
        return equipmentInfoMapper.getList();
    }
    public EquipmentInfo query(EquipmentInfo equipmentInfo) {
        return equipmentInfoMapper.query(equipmentInfo);
    }
}