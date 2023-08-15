package com.hcjc666.tcpserver.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hcjc666.tcpserver.entity.EquipmentType;
import com.hcjc666.tcpserver.mapper.EquipmentTypeMapper;

@Service
public class EquipmentTypeService {
    @Autowired
    private EquipmentTypeMapper equipmentTypeMapper;

    public List<EquipmentType> getList() {
        return equipmentTypeMapper.getList();
    }

    public EquipmentType query(EquipmentType equipmentType) {
        return equipmentTypeMapper.query(equipmentType);
    }
}