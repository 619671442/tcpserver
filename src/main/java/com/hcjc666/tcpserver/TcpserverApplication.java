package com.hcjc666.tcpserver;

import java.util.List;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import com.hcjc666.tcpserver.config.NettyConfig;
import com.hcjc666.tcpserver.entity.DtuInfo;
import com.hcjc666.tcpserver.netty.BootNettyServer;
import com.hcjc666.tcpserver.netty.DtuInfoCache;
import com.hcjc666.tcpserver.service.DtuInfoService;

@SpringBootApplication
@MapperScan("com.hcjc666.tcpserver.mapper")
@EnableAsync
public class TcpserverApplication implements CommandLineRunner {

	@Autowired
	private NettyConfig nettyConfig;
	@Autowired
	private DtuInfoService dtuInfoService;
	public static BootNettyServer tcpServer;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(TcpserverApplication.class);
		app.run(args);
		System.out.println("Hello World!");
	}

	@Async
	@Override
	public void run(String... args) throws Exception {
		/**
		 * 使用异步注解方式启动netty服务端服务
		 */
		tcpServer = new BootNettyServer();
		tcpServer.bind(nettyConfig.getPort());

		// 初始化dtu缓存
		List<DtuInfo> list = dtuInfoService.getList();
		for (DtuInfo dtuInfo : list) {
			DtuInfoCache.add(dtuInfo.getImei(), dtuInfo);
		}
		System.out.println("dtuinfo read   success! dtucache size: " + DtuInfoCache.size());
	}
}
