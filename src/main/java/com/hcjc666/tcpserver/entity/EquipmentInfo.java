package com.hcjc666.tcpserver.entity;

import lombok.Data;

@Data
public class EquipmentInfo {

    private String fid;

    private String equipmentName;

    private String dtuImei;

    private String equipmentCode;

    private String equipmentType;

    private String modbusAddr;

    private String location;

    private String remark;

}