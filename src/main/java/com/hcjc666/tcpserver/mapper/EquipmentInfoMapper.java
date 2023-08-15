package com.hcjc666.tcpserver.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.hcjc666.tcpserver.entity.EquipmentInfo;

@Repository
public interface EquipmentInfoMapper {
   public List<EquipmentInfo> getList();

   public EquipmentInfo query(EquipmentInfo equipmentInfo);
}
