package com.hcjc666.tcpserver.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcjc666.tcpserver.TcpserverApplication;
import com.hcjc666.tcpserver.cache.DtuInfoCache;
import com.hcjc666.tcpserver.entity.DtuInfo;
import com.hcjc666.tcpserver.entity.EquipmentInfo;
import com.hcjc666.tcpserver.netty.BootNettyChannel;
import com.hcjc666.tcpserver.netty.BootNettyChannelCache;
import com.hcjc666.tcpserver.service.DtuInfoService;
import com.hcjc666.tcpserver.service.EquipmentInfoService;
import com.hcjc666.tcpserver.util.LogUtils;
import com.hcjc666.tcpserver.util.StringUtils;

import io.netty.buffer.Unpooled;

@RestController
public class BootNettyController {

    @Autowired
    private EquipmentInfoService equipmentInfoService;
    @Autowired
    private DtuInfoService dtuInfoService;

    @GetMapping(value = "/")
    public String index() {

        List<EquipmentInfo> list = equipmentInfoService.getList();
        return "netty is running on " + TcpserverApplication.tcpServer.port + ",list.size:" + list.size();
    }

    /**
     * 刷新缓存数据
     * 
     * @return
     */
    @GetMapping(value = "/ref")
    public String ref() {
        List<DtuInfo> list = dtuInfoService.getList();
        for (DtuInfo dtuInfo : list) {
            DtuInfoCache.add(dtuInfo.getImei(), dtuInfo);
        }
        return "refresh   success! dtu size: " + DtuInfoCache.size();
    }

    @GetMapping("/logtest")
    public String helloworld() throws Exception {
        Logger log = LogUtils.getExceptionLogger();
        Logger log1 = LogUtils.getNettyLogger();
        Logger log2 = LogUtils.getPlatformLogger();
        log.info("getExceptionLogger===日志测试");
        log1.info("getNettyLogger===日志测试");
        log2.info("getPlatformLogger===日志测试");
        return "helloworld";
    }

    @GetMapping("/tcp/clientList")
    public List<Map<String, String>> clientList() {
        List<Map<String, String>> list = new ArrayList<>();
        for (Map.Entry<String, BootNettyChannel> entry : BootNettyChannelCache.channelMapCache.entrySet()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("code", entry.getKey());
            // map.put("code", entry.getValue().getCode());
            map.put("report_last_data", entry.getValue().getReportLastData());
            list.add(map);
        }
        return list;
    }

    @PostMapping("/tcp/downDataToAllClient")
    public String downDataToAllClient(@RequestParam(name = "content", required = true) String content) {
        for (Map.Entry<String, BootNettyChannel> entry : BootNettyChannelCache.channelMapCache.entrySet()) {
            BootNettyChannel bootNettyChannel = entry.getValue();
            if (bootNettyChannel != null && bootNettyChannel.getChannel().isOpen()) {
                bootNettyChannel.getChannel()
                        .writeAndFlush(Unpooled.buffer().writeBytes(StringUtils.getHexBytes(content)));
                // netty的编码已经指定，因此可以不需要再次确认编码
                // bootNettyChannel.getChannel().writeAndFlush(Unpooled.buffer().writeBytes(content.getBytes(CharsetUtil.UTF_8)));
            }
        }
        return "ok";
    }

    @PostMapping("/tcp/downDataToClient")
    public String downDataToClient(@RequestParam(name = "dtuImei", required = true) String dtuImei,
            @RequestParam(name = "content", required = true) String content) {
        BootNettyChannel bootNettyChannel = BootNettyChannelCache.get(dtuImei);
        if (bootNettyChannel != null && bootNettyChannel.getChannel().isOpen()) {
            bootNettyChannel.getChannel().writeAndFlush(Unpooled.buffer().writeBytes(StringUtils.getHexBytes(content)));
            // netty的编码已经指定，因此可以不需要再次确认编码
            // bootNettyChannel.getChannel().writeAndFlush(Unpooled.buffer().writeBytes(content.getBytes(CharsetUtil.UTF_8)));
            return "success";
        }
        return "fail";
    }

}