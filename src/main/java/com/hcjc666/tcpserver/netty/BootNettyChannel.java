package com.hcjc666.tcpserver.netty;

import java.util.Date;

import io.netty.channel.Channel;

public class BootNettyChannel {

	// 通道id
	private String channelId;

	// 连接客户端唯一标识,设备串号
	private String dtuImei;

	// 客户端最新发送的消息内容
	private String reportLastData;

	// 客户端最新发送的消息时间
	private Date reportLastDataTime;

	private transient volatile Channel channel;


	
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

	public String getDtuImei() {
		return dtuImei;
	}

	public void setDtuImei(String dtuImei) {
		this.dtuImei = dtuImei;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public Date getReportLastDataTime() {
		return reportLastDataTime;
	}

	public void setReportLastDataTime(Date reportLastDataTime) {
		this.reportLastDataTime = reportLastDataTime;
	}

	 

}
