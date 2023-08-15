package com.hcjc666.tcpserver.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;

import com.hcjc666.tcpserver.entity.DtuInfo;
import com.hcjc666.tcpserver.util.LogUtils;
import com.hcjc666.tcpserver.util.StringUtils;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * I/O数据读写处理类
 */
@ChannelHandler.Sharable
public class BootNettyChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter {

    /**
     * 注册时执行
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        LogUtils.getNettyLogger().info("--channelRegistered,channelId:" + ctx.channel().id().toString());
    }

    /**
     * 离线时执行
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        LogUtils.getNettyLogger().info("--channelUnregistered,channelId:" + ctx.channel().id().toString());
    }

    /**
     * 从客户端收到新的数据时,这个方法会在收到消息时被调用
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg == null) {
                return;
            }
            String data = (String) msg;
            data = data.replaceAll("\r|\n", "");
            String channelId = ctx.channel().id().toString();
            LogUtils.getNettyLogger().info("channelId=" + channelId + "data=" + data);

            BootNettyChannel cacheChannel = BootNettyChannelCache.get("channelId:" + channelId);

            // 判断是否是已经知道的通道返回数据
            if (null != cacheChannel) {
                String imei = cacheChannel.getDtuImei();
                // 获取dtu信息
                DtuInfo dtu = DtuInfoCache.get(imei);
                String heartBeats = dtu.getHeartBeats();
                // 收到的不是数据,是心跳包
                if (data.equals(heartBeats)) {
                    // 是心跳数据,不做处理
                    LogUtils.getNettyLogger()
                            .info("--channelRead,HeartBeats,channelId:" + ctx.channel().id().toString() + ",imei:"
                                    + imei);
                } else {
                    // 是真实的数据
                    cacheChannel.setReportLastData(data);// 更新最后一次收到的数据和时间
                    cacheChannel.setReportLastDataTime(new Date());
                    // 更新数据库

                    // 发送到dtu数据处理服务
                }
            } else {
                // 未知通道返回
                // 判断收到的数据是不是已知道的imei串号
                DtuInfo dtu = DtuInfoCache.get(data);
                if (dtu != null) {// 能获取到dtu信息，说明是dtu串号
                    if (cacheChannel == null) {
                        BootNettyChannel bootNettyChannel = new BootNettyChannel();
                        bootNettyChannel.setChannel(ctx.channel());
                        bootNettyChannel.setDtuImei(dtu.getImei());
                        bootNettyChannel.setChannelId(channelId);
                        bootNettyChannel.setReportLastData(data);
                        bootNettyChannel.setReportLastDataTime(new Date());
                        BootNettyChannelCache.save("channelId:" + channelId, bootNettyChannel);
                        LogUtils.getNettyLogger()
                                .info("--channelRead,channelId:" + ctx.channel().id().toString()
                                        + " is first connect ,cache ,imei: " + dtu);
                    }
                } else {
                    //一個未知设备返回数据，且不是已经知道的imie数据,打印通道信息和数据
                    LogUtils.getNettyLogger()
                            .info("--channelRead,Unknown data,channelId:" + ctx.channel().id().toString()
                                    + ",remoteAddress：" + ctx.channel().remoteAddress() + ",data:" + data);
                }
            }

            ctx.writeAndFlush(Unpooled.buffer().writeBytes(StringUtils.getHexBytes(data)));// 原样返回
            // netty的编码已经指定,因此可以不需要再次确认编码
            // ctx.writeAndFlush(Unpooled.buffer().writeBytes(channelId.getBytes(CharsetUtil.UTF_8)));
        } catch (

        Exception e) {
            LogUtils.getNettyLogger()
                    .info("--channelRead,channelId:" + ctx.channel().id().toString() + "," + e.toString());

        }
    }

    /**
     * 从客户端收到新的数据、读取完成时调用
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws IOException {
        LogUtils.getNettyLogger().info("--channelReadComplete,channelId:" + ctx.channel().id().toString());
        ctx.flush();
    }

    /**
     * 当出现 Throwable 对象才会被调用,即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws IOException {
        LogUtils.getNettyLogger().info("--exceptionCaught,channelId:" + ctx.channel().id().toString());
        cause.printStackTrace();
        BootNettyChannel bootNettyChannel = BootNettyChannelCache.get("channelId:" + ctx.channel().id().toString());
        if (bootNettyChannel != null) {
            BootNettyChannelCache.remove("channelId:" + ctx.channel().id().toString());
        }
        ctx.close();// 抛出异常,断开与客户端的连接
    }

    /**
     * 客户端与服务端第一次建立连接时 执行
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception, IOException {
        super.channelActive(ctx);
        ctx.channel().read();
        InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = inSocket.getAddress().getHostAddress();
        // 此处不能使用ctx.close(),否则客户端始终无法与服务端建立连接
        LogUtils.getNettyLogger()
                .info("--channelActive,channelId:" + ctx.channel().id().toString() + ",clientIp:" + clientIp
                        + ctx.name());
    }

    /**
     * 客户端与服务端 断连时 执行
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception, IOException {
        super.channelInactive(ctx);
        InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = inSocket.getAddress().getHostAddress();
        LogUtils.getNettyLogger().info("--channelInactive,channelId:" + ctx.channel().id().toString() + "," + clientIp);
        BootNettyChannel bootNettyChannel = BootNettyChannelCache.get("channelId:" + ctx.channel().id().toString());

        try {
            if (bootNettyChannel != null) {
                BootNettyChannelCache.remove("channelId:" + ctx.channel().id().toString());
            }
            ctx.close(); // 断开连接时,必须关闭,否则造成资源浪费,并发量很大情况下可能造成宕机
            LogUtils.getNettyLogger()
                    .info("--channelInactive,channel  [ Id:" + ctx.channel().id().toString() + ",clientIp:" + clientIp
                            + ctx.name());
        } catch (Exception e) {
            LogUtils.getNettyLogger()
                    .error("--channelInactive  exception,channelId:" + ctx.channel().id().toString() + ",clientIp:"
                            + clientIp
                            + ctx.name());
        }

    }

    /**
     * 服务端当read超时, 会调用这个方法
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception, IOException {
        super.userEventTriggered(ctx, evt);
        InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = inSocket.getAddress().getHostAddress();

        ctx.close();// 超时时断开连接
        LogUtils.getNettyLogger()
                .info("--userEventTriggered,channelId:" + ctx.channel().id().toString() + ",clientIp:" + clientIp);
    }

}