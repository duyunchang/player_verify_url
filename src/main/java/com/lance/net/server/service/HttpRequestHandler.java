package com.lance.net.server.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lance.net.server.common.ChatConstants;
import com.lance.net.server.module.Constants;
import com.lance.net.server.redis.RedisClient;
import com.lance.net.server.util.DateHelper;
import com.lance.net.server.util.sign.MD5;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;


public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private Logger loger = LogManager.getLogger();
	private final String webUri;	
	private RedisClient redisClient;
	
	public HttpRequestHandler(String webUri,RedisClient redisClient) {
		this.webUri = webUri;
		this.redisClient=redisClient;
		
	}

	//拦截url
	@Override
	protected  void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		loger.info("===========> {}, {}", webUri, request.uri());
		
		String uri = StringUtils.substringBefore(request.uri(), "?");//ws
		if(webUri.equalsIgnoreCase(uri)) {//获取webSocket参数
			if(validationLoginBefore(ctx, request)){
				request.setUri(uri);
				ctx.fireChannelRead(request.retain());			
			}
						
		}else {
			loger.info("channelRead0拒绝这个链接> {}","ws请求url格式错误");
			//sendResponseMessage("ws请求url缺少参数", ctx,request);
			ctx.close();		
			return ;
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
	private  boolean validationLoginBefore(ChannelHandlerContext ctx,FullHttpRequest request) throws Exception{	
		QueryStringDecoder query = new QueryStringDecoder(request.uri());
		Map<String, List<String>> map = query.parameters();
		//获取url参数
		List<String> tokens = map.get(Constants.TOKEN);		
		List<String> uids = map.get(Constants.UID);	
		List<String> times = map.get(Constants.TIME);	
			
		try{
			//根据参数保存当前登录对象, 并把该uid加入到channel中
			if(tokens != null && !tokens.isEmpty()&&uids!=null&&!uids.isEmpty()&&times!=null&&!times.isEmpty()) {
				String token = tokens.get(0);
				String uid  = uids.get(0);
				String time=times.get(0);
		
				//token验证
				if(!validationLoginParameter(token, uid, time)){//参数没验证通过 -关闭
					//sendResponseMessage("参数没验证通过 ", ctx,request);
					ctx.close();
					return false;
				};
				
				
				loger.info("validationLoginBefore> {},;uid={}","ws验证通过",uid);
				//设置已经连接
				//redisClient.setOnlyExpireKey(uid,  Constants.ISCONNECTED,Constants.CONNECTED_YES);
				redisClient.setHash(uid, Constants.TIME, time, Constants.CONNECTED_TIMEOUT);
				ctx.channel().attr(ChatConstants.CHANNEL_TOKEN_KEY).getAndSet(uid);
				
			}else{//不符合条件关闭
				loger.info("validationLoginBefore拒绝这个链接> {}","请求url缺少参数");
				//sendResponseMessage("请求url缺少参数", ctx,request);
				ctx.close();		
				return false;
			}
		}finally{
			map=null;
			tokens=null;
			uids=null;
			times=null;
		}
		return true;
	}
	//验证参数
	private boolean validationLoginParameter(String token,String uid,String time) throws Exception{
		//验证时间长度 
		if(time.length()!=17){
			loger.info("validationLoginParameter拒绝这个链接> {}","time长度不为17");
			return false;
		}

		//获取redis信息
		List<String> inHashMap = redisClient.getInHashMap(uid, Constants.WSSIGN,Constants.PLAYTOKEN,Constants.TIME);
		if(inHashMap==null){
			loger.info("validationLoginParameter拒绝这个链接> {}","redis中key:uid不存在");
			return false;
		}
		//请求redis存放时间
		String TIME = inHashMap.get(2);
		if(TIME!=null&&DateHelper.getDateLongByStr(time) <= DateHelper.getDateLongByStr(TIME)){
			loger.info("validationLoginParameter拒绝这个链接> {}","time小于等于上次时间一样");
			return false;
		}
		
		String stringToMD5 = MD5.stringToMD5(ChatConstants.CHANNEL_TOKEN_KEY_STR+inHashMap.get(0)+inHashMap.get(1)+time);
						
		//验证MD5
		if(!token.matches(stringToMD5)){				
			loger.info("validationLoginParameter拒绝这个链接> {}","token未验证通过");
			return false;
		}
		
		//确保同一个uid只有几个登录
		Long number = redisClient.setsysOnlyExpireKeyAddLong(uid,  Constants.CUEEENTONLOGINCOUNT,(long)1);
		if(number>Constants.CURRENTMAXLOGINCONNECT){
			loger.info("validationLoginParameter拒绝这个链接> {}","uid已经开始连接;当前这个uid已经登录次数="+number);
			return false;
		}

		return true;
		
	}
	
//	private void sendResponseMessage(String sendInfo ,ChannelHandlerContext ctx,FullHttpRequest request) throws Exception{
//	if(HttpUtil.is100ContinueExpected(request)) {
//		send100ContinueExpected(ctx);
//	}
//	
//	RandomAccessFile file = new RandomAccessFile("src/main/resources/templates/index.html", "r");
//	
//	HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
//	response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
//	
//	boolean keepAlive = HttpUtil.isKeepAlive(request);
//	
//	if(keepAlive) {
//		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
//		response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
//	}
//	ctx.write(response);
//	//ctx.write(new TextWebSocketFrame(sendInfo));
//	
//	if(ctx.pipeline().get(SslHandler.class) == null) {
//		ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
//	}else {
//		ctx.write(new ChunkedNioFile(file.getChannel()));
//	}
//	
//	ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
//	if(!keepAlive) {
//		
//		future.addListener(ChannelFutureListener.CLOSE);
//	}
//	
//	file.close();
//	
//	//关闭
//	//ctx.close();
//}
//
//private void send100ContinueExpected(ChannelHandlerContext ctx) {
//	FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONFLICT);
//	ctx.writeAndFlush(response);		
//}
	
}
