package com.hcjc666.tcpserver.netty;

import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 通道初始化
 */
@ChannelHandler.Sharable
public class BootNettyChannelInitializer<SocketChannel> extends ChannelInitializer<Channel> {

    public static long READ_TIME_OUT = 60;

    public static long WRITE_TIME_OUT = 60;

    public static long ALL_TIME_OUT = 60;

    @Override
    protected void initChannel(Channel ch) throws Exception {

        ch.pipeline().addLast(new IdleStateHandler(READ_TIME_OUT, WRITE_TIME_OUT, ALL_TIME_OUT, TimeUnit.SECONDS));

        // 如果接受字符串 ，带编码
        //ch.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        //ch.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));


        //如果接受16进制数据
        //ch.pipeline().addLast("encoder", new MyEncoder());//这一块没摸透，未按照预想执行
        ch.pipeline().addLast("decoder", new MyDecoder());

        // // ChannelOutboundHandler，依照逆序执行
        // ch.pipeline().addLast("encoder", new StringEncoder());
        //
        // // 属于ChannelInboundHandler，依照顺序执行
        // ch.pipeline().addLast("decoder", new StringDecoder());

        // 自定义ChannelInboundHandlerAdapter
        ch.pipeline().addLast(new BootNettyChannelInboundHandlerAdapter());

    }

}



