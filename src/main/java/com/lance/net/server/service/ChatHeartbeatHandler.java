package com.lance.net.server.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lance.net.server.common.ChatConstants;
import com.lance.net.server.module.Constants;
import com.lance.net.server.redis.RedisClient;
import com.lance.net.server.util.DateHelper;
import com.lance.net.server.util.RandomUtil;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;

public class ChatHeartbeatHandler extends ChannelInboundHandlerAdapter{
	private Logger logger = LogManager.getLogger();
	private RedisClient redisClient;
	
	public ChatHeartbeatHandler() {
		super();
	}

	public ChatHeartbeatHandler(RedisClient redisClient) {
		super();
		this.redisClient = redisClient;
	}
	
	//心跳
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		
		if(evt instanceof IdleStateEvent) {
			//logger.info("====>Heartbeat:{}", "start");		
			//发送消息
			sendMessage(ctx);
		}else {
			super.userEventTriggered(ctx, evt);
		}
	}
	
	private void sendMessage(ChannelHandlerContext ctx) throws Exception{	
		String sign = ctx.hashCode()+"";		
		//开始提问题
		String phoneNumber = ctx.channel().attr(ChatConstants.CHANNEL_TOKEN_KEY).get();
		String ProblemKey=//"ip";
				ChatConstants.info[RandomUtil.RandomU(ChatConstants.info.length)];	
		String sendInfo=null;
		List<String> HashMap=null;
		String[] split =null;
		try{
			//获取验证码值		
			//HashMap=redisClient.getInHashMap(phoneNumber, Constants.VERIFICATIONCODE,Constants.PROBLEM,ProblemKey,Constants.CURRFAILCOUNT);
			HashMap=redisClient.getInHashMap(phoneNumber, Constants.VERIFICATIONCODE,Constants.PROBLEM,ProblemKey);
			String faileCount=redisClient.getOnlyExpireKey(phoneNumber+":"+sign,Constants.CURRFAILCOUNT );			
			//大于3次 关闭链接 
//			if(faileCount!=null&&Integer.parseInt(faileCount)>Constants.MAXCURRFAILCOUNT){
//				logger.info("错误{}次;强制关闭;uid={}的连接",Constants.MAXCURRFAILCOUNT,phoneNumber);
//				ctx.close();
//				return ;
//			}
			
			
			String problemValue =HashMap.get(0);
			//特殊问题优先
			if(problemValue==null){//验证码
				//优先级问题
				problemValue = HashMap.get(1);//redisClient.getInHashObjArr(phoneNumber, Constants.PROBLEM);	
			 
				if(problemValue==null){
					problemValue= HashMap.get(2);//redisClient.getInHashObjArr(phoneNumber, ProblemKey);
					
				}else{
					split = problemValue.split("=");
					if(split.length==2){
						ProblemKey=split[0];
						problemValue=split[1];
						redisClient.removeInHashObj(phoneNumber, Constants.PROBLEM);
					}
				}
				
				if(problemValue==null){
					ProblemKey=ChatConstants.info[RandomUtil.RandomU(ChatConstants.info.length)];
					problemValue= redisClient.getInHashObjArr(phoneNumber, ProblemKey);
					
				}
				
				
			
			}else{//删除
				ProblemKey= Constants.VERIFICATIONCODE;
				redisClient.removeInHashObj(phoneNumber, Constants.VERIFICATIONCODE);
			}
	
			//发送问题
			
			redisClient.setOnlyExpireKey(phoneNumber+":"+sign,Constants.SIGN ,sign );			
			//"answer" 存放问题答案
			redisClient.setOnlyExpireKey(phoneNumber+":"+sign, Constants.ANSWER,problemValue==null?"null":problemValue );	
			//String faileCount = HashMap.get(3);
			redisClient.setOnlyExpireKey(phoneNumber+":"+sign,Constants.CURRFAILCOUNT ,faileCount==null?"1":(Integer.parseInt(faileCount)+1)+"");
						
			//更新连接时间 ;心跳更新已经连接
			redisClient.setOnlyExpireKey(phoneNumber, Constants.ISCONNECTED,Constants.CONNECTED_YES);			
			sendInfo=Constants.TIME+"="+DateHelper.getDateTimeByyyyyMMddHHmmss(new Date())+"&"+Constants.QUESTION+"="+ProblemKey+"&sign="+sign;		
	
			ctx.channel().writeAndFlush(new TextWebSocketFrame(sendInfo));
				
		}finally{
			HashMap=null;
			split=null;
			logger.info("====>Heartbeat:send:{}", sendInfo);
			sendInfo=null;
		}
		
	
		
	}
	
	
	
	public RedisClient getRedisClient() {
		return redisClient;
	}

	public void setRedisClient(RedisClient redisClient) {
		this.redisClient = redisClient;
	}
	
	
}
