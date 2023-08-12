package com.hcjc666.tcpserver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import com.hcjc666.tcpserver.netty.BootNettyServer;


@SpringBootApplication
@EnableAsync
public class TcpserverApplication implements CommandLineRunner {
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
		new BootNettyServer().bind(6655);
		

	}
}
