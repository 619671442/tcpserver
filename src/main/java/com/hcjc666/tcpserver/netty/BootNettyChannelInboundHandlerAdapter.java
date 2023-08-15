package com.hcjc666.tcpserver.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import com.hcjc666.tcpserver.cache.DtuInfoCache;
import com.hcjc666.tcpserver.entity.DtuInfo;
import com.hcjc666.tcpserver.entity.EquipmentInfo;
import com.hcjc666.tcpserver.entity.EquipmentType;
import com.hcjc666.tcpserver.service.DtuInfoService;
import com.hcjc666.tcpserver.service.EquipmentInfoService;
import com.hcjc666.tcpserver.service.EquipmentTypeService;
import com.hcjc666.tcpserver.util.GetBeanUtil;
import com.hcjc666.tcpserver.util.LogUtils;
import com.hcjc666.tcpserver.util.StringUtils;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
        LogUtils.getNettyLogger().info("注册时执行,channelId:" + ctx.channel().id().toString());
    }

    /**
     * 离线时执行
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        LogUtils.getNettyLogger().info("离线时执行,channelId:" + ctx.channel().id().toString());
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
            LogUtils.getNettyLogger().info("从客户端收到新的数据时,收到消息时被调用,channelId=" + channelId + ",data=" + data);

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
                            .info("从客户端收到新的数据时,收到消息时被调用,收到的是心跳数据,不做处理,channelId:" + ctx.channel().id().toString()
                                    + ",imei:"
                                    + imei + ",data=" + data);
                } else {
                    Date now = new Date();
                    // 是真实的数据
                    cacheChannel.setReportLastData(data);// 更新最后一次收到的数据和时间
                    cacheChannel.setReportLastDataTime(now);
                    // 更新数据库
                    DtuInfo temp = new DtuInfo();
                    temp.setFid(dtu.getFid());
                    temp.setLastData(data);
                    temp.setLastDataTime(now);
                    GetBeanUtil.getBean(DtuInfoService.class).update(temp);
                    // 发送到dtu数据处理服务
                    postDataToServer(data, dtu, now);
                }
            } else {
                // 未知通道返回
                // 判断收到的数据是不是dtu的注册包
                DtuInfo dtu = DtuInfoCache.getByRegistered(data);
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
                                .info("从客户端收到新的数据时,收到消息时被调用,是已知的dtu设备第一次连接,channelId:" + ctx.channel().id().toString()
                                        + " ,保存通道 ,imei: " + dtu);
                    }
                } else {
                    // 一個未知设备返回数据，且收到的数据不是注册包，不是注册包，就无法判断是那个dtu设备返回的数据，无法知道对于地址位的数据是那个设备,所以只打印通道信息和数据
                    LogUtils.getNettyLogger()
                            .info("从客户端收到新的数据时,收到消息时被调用,是未知的设备第一次连接,踢掉连接,channelId:" + ctx.channel().id().toString()
                                    + ",remoteAddress：" + ctx.channel().remoteAddress() + ",data:" + data);
                    ctx.writeAndFlush(Unpooled.buffer().writeBytes("未知的设备,断开连接".getBytes()));//  
                    // 踢掉连接
                    ctx.channel().close();
                }
            }

            // ctx.writeAndFlush(Unpooled.buffer().writeBytes(StringUtils.getHexBytes(data)));//
            // 原样返回
            // netty的编码已经指定,因此可以不需要再次确认编码
            // ctx.writeAndFlush(Unpooled.buffer().writeBytes(channelId.getBytes(CharsetUtil.UTF_8)));
        } catch (

        Exception e) {
            LogUtils.getNettyLogger()
                    .info("从客户端收到新的数据时,收到消息时被调用,发送异常,channelId:" + ctx.channel().id().toString() + "," + e.toString());

        }
    }

    /**
     * 发送数据到数据处理服务
     * 
     * @param data mmodbus数据
     * @param imei dtu设备串号
     * @param dtu  dtu信息
     * @param now  数据时间
     * @throws JSONException
     */
    private void postDataToServer(String data, DtuInfo dtu, Date now) throws JSONException {
        // 地址位
        String modbusAddr = data.split(" ")[0];// modebus的数据，第一个空格之前是16进制地址位
        //// 先查询设备信息
        EquipmentInfo etemp = new EquipmentInfo();
        Integer intAddr = Integer.parseInt(modbusAddr, 16);
        etemp.setModbusAddr(intAddr.toString());
        etemp.setDtuImei(dtu.getImei());
        EquipmentInfo equipmentInfo = GetBeanUtil.getBean(EquipmentInfoService.class).query(etemp);
        if (null == equipmentInfo) {
            LogUtils.getNettyLogger().info("发送数据到数据处理服务,查询不到设备信息! imei:" + dtu.getImei()
                    + ",modbusAddr(16进制):" + modbusAddr + ",data:" + data);
            return;
        }

        // 设备类型
        String type = equipmentInfo.getEquipmentType();

        // 获取设备类型对应的信息
        EquipmentType ttemp = new EquipmentType();
        ttemp.setEquipmentType(type);

        EquipmentType equipmentType = GetBeanUtil.getBean(EquipmentTypeService.class).query(ttemp);
        if (null == equipmentType) {
            LogUtils.getNettyLogger().info("发送数据到数据处理服务,查询不到设备类型信息! imei:" + dtu.getImei()
                    + ",modbusAddr(16进制):" + modbusAddr + ",data:" + data);
            return;
        }

        String url = equipmentType.getHandleConfig();
        JSONObject json = new JSONObject();
        json.put("data", data);
        json.put("time", now.getTime());
        json.put("dtuImei", dtu.getImei());
        json.put("equipmentName", equipmentInfo.getEquipmentName());
        json.put("modbusAddr", modbusAddr);
        postData(url, String.valueOf(json));
        LogUtils.getNettyLogger().info("发送数据到数据处理服务,success! imei:" + dtu.getImei()
                + ",modbusAddr(16进制):" + ",data:" + data);
    }

    // 数据发送到指定url服务去解析保存
    private void postData(String url, String jsonstr) {
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonstr);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.getNettyLogger().error("数据发送到指定url服务去解析保存,失败! url：" + url
                    + ",jsonstr:" + jsonstr + "," + e.getMessage());
        }
    }

    /**
     * 从客户端收到新的数据、读取完成时调用
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws IOException {
        LogUtils.getNettyLogger().info("从客户端收到新的数据、读取完成时调用,channelId:" + ctx.channel().id().toString());
        ctx.flush();
    }

    /**
     * 当出现 Throwable 对象才会被调用,即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws IOException {
        LogUtils.getNettyLogger().info(
                "当出现 Throwable 对象才会被调用,即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时,channelId:" + ctx.channel().id().toString());
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
                .info("客户端与服务端第一次建立连接时 执行,channelId:" + ctx.channel().id().toString() + ",clientIp:" + clientIp
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
        LogUtils.getNettyLogger().info("客户端与服务端 断连时 执行,channelId:" + ctx.channel().id().toString() + "," + clientIp);
        BootNettyChannel bootNettyChannel = BootNettyChannelCache.get("channelId:" + ctx.channel().id().toString());

        try {
            if (bootNettyChannel != null) {
                BootNettyChannelCache.remove("channelId:" + ctx.channel().id().toString());
            }
            ctx.close(); // 断开连接时,必须关闭,否则造成资源浪费,并发量很大情况下可能造成宕机
            LogUtils.getNettyLogger()
                    .info("客户端与服务端 断连时 执行完毕,channel  [ Id:" + ctx.channel().id().toString() + ",clientIp:" + clientIp
                            + ctx.name());
        } catch (Exception e) {
            LogUtils.getNettyLogger()
                    .error("客户端与服务端 断连时 执行exception,channelId:" + ctx.channel().id().toString() + ",clientIp:"
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
                .info("服务端当read超时, 会调用这个方法,channelId:" + ctx.channel().id().toString() + ",clientIp:" + clientIp);
    }

}