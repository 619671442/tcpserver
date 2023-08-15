package com.hcjc666.tcpserver.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.hcjc666.tcpserver.entity.EquipmentType;

@Repository
public interface EquipmentTypeMapper {
   public List<EquipmentType> getList();

   public EquipmentType query(EquipmentType equipmentType);
}
