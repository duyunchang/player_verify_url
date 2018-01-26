package com.lance.net.server.common;

import java.net.InetSocketAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.lance.net.server.redis.RedisClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;

@Component
public class ChatServer {

	private final ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
	private final EventLoopGroup bossGroup = new NioEventLoopGroup();
	private final EventLoopGroup workGroup = new NioEventLoopGroup();
	private Channel channel;

	@Autowired
	private RedisClient redisClient;
	
	public ChannelFuture start(InetSocketAddress address,Integer timeout) {
	
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChatServerInitializer(channelGroup, redisClient,timeout))
				//.option(ChannelOption.SO_BACKLOG, 2*50*10000)////初始化服务端可连接队列大小
				.option(ChannelOption.SO_BACKLOG, 5000)////初始化服务端可连接队列大小
				.option(ChannelOption.SO_RCVBUF, 128)
				.childOption(ChannelOption.SO_SNDBUF, 128)
				.childOption(ChannelOption.SO_KEEPALIVE, true);

		ChannelFuture future = bootstrap.bind(address).syncUninterruptibly();//addListener(ChannelFutureListener.CLOSE_ON_FAILURE).
		channel = future.channel();
		return future;
	}

	public void destroy() {
		if (channel != null) {
			channel.close();
		}
		
		channelGroup.close();
		workGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}

}
