package com.hcjc666.tcpserver.util;

/**
 * 本地日志枚举
 * 
 * @author Administrator
 *
 */
public enum LogEnum {

    NETTY("netty"),

    PLATFORM("platform"),

    EXCEPTION("exception"),

    ;

    private String category;

    LogEnum(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}