package com.hcjc666.tcpserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import com.hcjc666.tcpserver.config.NettyConfig;
import com.hcjc666.tcpserver.netty.BootNettyServer;


@SpringBootApplication
@MapperScan("com.hcjc666.tcpserver.mapper")
@EnableAsync
public class TcpserverApplication implements CommandLineRunner {

	@Autowired
	private NettyConfig nettyConfig;

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


		//循环发送查询命令到各个通道
		
	}
}
