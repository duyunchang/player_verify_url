package com.lance.net.server;

import java.net.InetSocketAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.lance.net.server.common.ChatServer;

import io.netty.channel.ChannelFuture;

@SpringBootApplication
public class NettyWebSocketServerApplication implements CommandLineRunner{
	@Autowired
	private ChatServer chatServer;

    public static void main(String[] args) {
        SpringApplication.run(NettyWebSocketServerApplication.class, args);
    }
   
    @Bean
    public ChatServer chatServer() {
    	return new ChatServer();
    }

    @Value("${websocket.ip}")
   	private String ip;
    @Value("${websocket.port}")
	private Integer port;
	@Value("${my.connect-timeout}")
    private Integer timeout;
    
	@Override
	public void run(String... args) throws Exception {		
		InetSocketAddress address = new InetSocketAddress(ip, port);
		ChannelFuture future = chatServer.start(address,timeout);
		
		System.out.println("**********************************************************************");
		System.out.println("***********websocket start ip "+ip+" port "+port+"*********************");
		System.out.println("**********************************************************************");
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				chatServer.destroy();
			}
		});
		
		future.channel().closeFuture().syncUninterruptibly();
	
		
	}
}