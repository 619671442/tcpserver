package com.hcjc666.tcpserver.netty;

import java.io.IOException;
import java.net.InetSocketAddress;

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
     * 从客户端收到新的数据时，这个方法会在收到消息时被调用
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

            // 这里我将通道id作为code来使用，实际是需要msg里来摘取的客户端数据里的唯一值的
            // 如果没有则创建 如果有，更新data值
            BootNettyChannel b = BootNettyChannelCache.get("channelId:" + channelId);
            if (b == null) {
                BootNettyChannel bootNettyChannel = new BootNettyChannel();
                bootNettyChannel.setChannel(ctx.channel());
                bootNettyChannel.setCode("channelId:" + channelId);
                bootNettyChannel.setReport_last_data(data);
                BootNettyChannelCache.save("channelId:" + channelId, bootNettyChannel);
            } else {
                b.setReport_last_data(data);
            }
            ctx.writeAndFlush(Unpooled.buffer().writeBytes(StringUtils.getHexBytes(data)));//原样返回
            // netty的编码已经指定，因此可以不需要再次确认编码
            // ctx.writeAndFlush(Unpooled.buffer().writeBytes(channelId.getBytes(CharsetUtil.UTF_8)));
        } catch (Exception e) {
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
     * 当出现 Throwable 对象才会被调用，即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws IOException {
        LogUtils.getNettyLogger().info("--exceptionCaught,channelId:" + ctx.channel().id().toString());
        cause.printStackTrace();
        BootNettyChannel bootNettyChannel = BootNettyChannelCache.get("channelId:" + ctx.channel().id().toString());
        if (bootNettyChannel != null) {
            BootNettyChannelCache.remove("channelId:" + ctx.channel().id().toString());
        }
        ctx.close();// 抛出异常，断开与客户端的连接
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
        // 此处不能使用ctx.close()，否则客户端始终无法与服务端建立连接
        LogUtils.getNettyLogger()
                .info("--channelActive,channelId:" + ctx.channel().id().toString() + clientIp + ctx.name());
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
        if (bootNettyChannel != null) {
            BootNettyChannelCache.remove("channelId:" + ctx.channel().id().toString());
        }
        ctx.close(); // 断开连接时，必须关闭，否则造成资源浪费，并发量很大情况下可能造成宕机
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
                .info("--userEventTriggered,channelId:" + ctx.channel().id().toString() + "," + clientIp);
    }

}