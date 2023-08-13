package com.hcjc666.tcpserver.entity;

import lombok.Data;

@Data
public class EquipmentInfo {
    private long fid;

    private String equipmentName;

    private String equipmentCode;

    private String equipmentType;

    private String modbusAddr;

}