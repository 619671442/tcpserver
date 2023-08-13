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
import com.hcjc666.tcpserver.entity.EquipmentInfo;
import com.hcjc666.tcpserver.netty.BootNettyChannel;
import com.hcjc666.tcpserver.netty.BootNettyChannelCache;
import com.hcjc666.tcpserver.service.EquipmentInfoService;
import com.hcjc666.tcpserver.util.LogUtils;

import io.netty.buffer.Unpooled;

@RestController
public class BootNettyController {

    @Autowired
    private EquipmentInfoService equipmentInfoService;

    @GetMapping(value = "/")
    public String index() {

        List<EquipmentInfo> list = equipmentInfoService.getList();
        return "netty is running on " + TcpserverApplication.tcpServer.port + ",list.size:" + list.size();
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
            map.put("report_last_data", entry.getValue().getReport_last_data());
            list.add(map);
        }
        return list;
    }

    @PostMapping("/tcp/downDataToAllClient")
    public String downDataToAllClient(@RequestParam(name = "content", required = true) String content) {
        for (Map.Entry<String, BootNettyChannel> entry : BootNettyChannelCache.channelMapCache.entrySet()) {
            BootNettyChannel bootNettyChannel = entry.getValue();
            if (bootNettyChannel != null && bootNettyChannel.getChannel().isOpen()) {
                bootNettyChannel.getChannel().writeAndFlush(Unpooled.buffer().writeBytes(content.getBytes()));
                // netty的编码已经指定，因此可以不需要再次确认编码
                // bootNettyChannel.getChannel().writeAndFlush(Unpooled.buffer().writeBytes(content.getBytes(CharsetUtil.UTF_8)));
            }
        }
        return "ok";
    }

    @PostMapping("/tcp/downDataToClient")
    public String downDataToClient(@RequestParam(name = "code", required = true) String code,
            @RequestParam(name = "content", required = true) String content) {
        BootNettyChannel bootNettyChannel = BootNettyChannelCache.get(code);
        if (bootNettyChannel != null && bootNettyChannel.getChannel().isOpen()) {
            bootNettyChannel.getChannel().writeAndFlush(Unpooled.buffer().writeBytes(content.getBytes()));
            // netty的编码已经指定，因此可以不需要再次确认编码
            // bootNettyChannel.getChannel().writeAndFlush(Unpooled.buffer().writeBytes(content.getBytes(CharsetUtil.UTF_8)));
            return "success";
        }
        return "fail";
    }

}