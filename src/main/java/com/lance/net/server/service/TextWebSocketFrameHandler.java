package com.lance.net.server.service;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lance.net.server.common.ChatConstants;
import com.lance.net.server.module.Constants;
import com.lance.net.server.redis.RedisClient;
import com.lance.net.server.util.UrlMap;
import com.lance.net.server.util.sign.MD5;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame>{
	private Logger loger = LogManager.getLogger();
	private final ChannelGroup group;
	private RedisClient redisClient;
		
	
	public TextWebSocketFrameHandler(ChannelGroup group,RedisClient redisClient) {
		this.group = group;
		this.redisClient=redisClient;
	}
	//握手
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		//loger.info("userEventTriggered_Event====>{}", evt);
		
		if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
			ctx.pipeline().remove(HttpRequestHandler.class);
			
			//加入当前, 上线人员推送前端，显示用户列表中去
			//Channel channel = ctx.channel();
			//计算当前登录人数  测试用
			//group.add(channel);
			//redis设置已连接
			//String phoneNumber = channel.attr(ChatConstants.CHANNEL_TOKEN_KEY).get();	
			//当前在线人数
			//redisClient.setOnlyExpireKey(phoneNumber,  Constants.CUEEENTONLINENUMBER,group.size()+"");
			//设置已经连接
			//redisClient.setOnlyExpireKey(phoneNumber,  Constants.ISCONNECTED,Constants.CONNECTED_YES);
			
		}else {
			super.userEventTriggered(ctx, evt);
		}
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		String receiveInfo = msg.text();	
		loger.info("channelReceive====>{}", receiveInfo);
				
	    validationMessage(ctx, receiveInfo);	
	
	}
		
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		loger.info("Current channel channelInactive");
		//offlines(ctx);
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		loger.info("Current channel handlerRemoved");
		offlines(ctx);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		loger.error("=====> {}", cause.getMessage());
		//offlines(ctx);
	}
	
	//验证message//解析问题 格式String  xxx=xxx&xxx=xxx
	private void validationMessage(ChannelHandlerContext ctx,String receiveInfo) throws Exception{	
		Map<String, String> urlRequest = null;
		try{
			urlRequest = UrlMap.URLRequest(receiveInfo);
			
			String time = urlRequest.get(Constants.TIME);
			if(time==null||time.length()!=17){
				
				return ;
			}
			//获取uid
			String phoneNumber = ctx.channel().attr(ChatConstants.CHANNEL_TOKEN_KEY).get();	
			//sign验证
			//url
			String sign = urlRequest.get(Constants.SIGN);
			//redis
			String SIGN = redisClient.getOnlyExpireKey(phoneNumber+":"+sign, Constants.SIGN);
			//只取第一个接收值		
			if(SIGN!=null){
				//删除sign
				redisClient.removeOnlyExpireKey(phoneNumber+":"+sign, Constants.SIGN);
			}else{
				loger.info("channelReceive-validationMessage=====> {}", "多次返回");
				return ;
			}
			
			//获取redis信息
			String ANSWER=redisClient.getOnlyExpireKey(phoneNumber+":"+SIGN, Constants.ANSWER);		
			//回答问题答案
			String answer = urlRequest.get(Constants.ANSWER);
			
			String timemmills=	time.substring(time.length()-3, time.length());//time.substring(0, time.length()-3);
			
			String md5=MD5.stringToMD5(timemmills+time+ANSWER);
			
			if(md5.equals(answer)){
				loger.info("channelReceive-validationMessage md5====>{}", "问题回答正确");
				redisClient.setOnlyExpireKey(phoneNumber+":"+SIGN, Constants.CURRFAILCOUNT,"0" );
			}else{
				loger.info("channelReceive-validationMessage md5====>{}", "问题回答错误");
			}
				
		}finally{
			urlRequest=null;
		}
	
	}
	
	
	private void offlines(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();

		String phoneNumber = channel.attr(ChatConstants.CHANNEL_TOKEN_KEY).get();
		if(phoneNumber!=null){
			//redis设置关闭连接状态
			redisClient.removeOnlyExpireKey(phoneNumber,  Constants.ISCONNECTED);
			redisClient.removeOnlyExpireKey(phoneNumber,  Constants.CUEEENTONLOGINCOUNT);
		}
		group.remove(channel);
		ctx.close();
	}
}
