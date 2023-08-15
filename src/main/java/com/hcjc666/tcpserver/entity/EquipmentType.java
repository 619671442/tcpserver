package com.hcjc666.tcpserver.entity;

import lombok.Data;

@Data
public class EquipmentType {

    private long fid;

    private String equipmentType;

    private String factory;

    private String protocol;

    private String handleConfig;

    private String remark;

}