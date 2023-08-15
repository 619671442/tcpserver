package com.hcjc666.tcpserver.netty;

import io.netty.channel.Channel;

public class BootNettyChannel {

	// 连接客户端唯一的code
	private String code;

	// 客户端最新发送的消息内容
	private String reportLastData;

	private transient volatile Channel channel;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

 
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getReportLastData() {
		return reportLastData;
	}

	public void setReportLastData(String reportLastData) {
		this.reportLastData = reportLastData;
	}

}
