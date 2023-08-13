package com.hcjc666.tcpserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "netty")
public class NettyConfig {

    private Integer port;

    private String listenEquipmentType;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getListenEquipmentType() {
        return listenEquipmentType;
    }

    public void setListenEquipmentType(String listenEquipmentType) {
       this.listenEquipmentType = listenEquipmentType;
    }

}
