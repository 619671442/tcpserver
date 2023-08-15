package com.hcjc666.tcpserver.entity;

import java.sql.Date;

import lombok.Data;

@Data
public class DtuInfo {
    private long fid;

    private String imei;

    private String dtuName;

    private String factory;

    private String location;

    private String remark;

    private String lastData;

    private Date lastDataTime;

    private String heartBeats;

    
}