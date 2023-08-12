package com.hcjc666.tcpserver.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcjc666.tcpserver.netty.BootNettyChannel;
import com.hcjc666.tcpserver.netty.BootNettyChannelCache;

import io.netty.buffer.Unpooled;

@RestController
public class BootNettyController {

    @GetMapping(value = "/")
    public String index() {
        return "netty springBoot tcp demo";
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