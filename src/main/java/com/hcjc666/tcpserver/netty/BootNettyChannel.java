package com.hcjc666.tcpserver.netty;

import io.netty.channel.Channel;

public class BootNettyChannel {

	// 连接客户端唯一的code
	private String code;

	// 客户端最新发送的消息内容
	private String report_last_data;

	private transient volatile Channel channel;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getReport_last_data() {
		return report_last_data;
	}

	public void setReport_last_data(String report_last_data) {
		this.report_last_data = report_last_data;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

}
