package com.lance.net.server.common;

import java.util.concurrent.TimeUnit;


import com.lance.net.server.redis.RedisClient;
import com.lance.net.server.service.ChatHeartbeatHandler;
import com.lance.net.server.service.HttpRequestHandler;
import com.lance.net.server.service.TextWebSocketFrameHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

public  class  ChatServerInitializer extends ChannelInitializer<Channel>{
	private final ChannelGroup group;
	private RedisClient redisClient;
    private Integer timeout;
	
	public ChatServerInitializer(ChannelGroup group,RedisClient redisClient,Integer timeout) {
		this.group = group;
		this.redisClient=redisClient;
		this.timeout=timeout;
	}
	
	@Override
	protected  void initChannel(Channel ch) throws Exception {
				
		ChannelPipeline pipeline = ch.pipeline();
	
		//处理日志 生产环境不需要打开
		//pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
		
		//处理心跳
		//pipeline.addLast(new IdleStateHandler(0, 0, 1800, TimeUnit.SECONDS));
		pipeline.addLast(new IdleStateHandler(0, 0, timeout, TimeUnit.SECONDS));
		pipeline.addLast(new ChatHeartbeatHandler(redisClient));
		
		pipeline.addLast(new HttpServerCodec());//请求解码器
		pipeline.addLast(new ChunkedWriteHandler());//支持异步发送大的码流
		pipeline.addLast(new HttpObjectAggregator(64 * 1024));//将多个消息转换成单一的消息对象
		pipeline.addLast(new HttpRequestHandler("/ws",redisClient));
		pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
		pipeline.addLast(new TextWebSocketFrameHandler(group,redisClient));		
	}
}
