package com.lance.net.server.common;

import io.netty.util.AttributeKey;

public class ChatConstants {
    public static final AttributeKey<String> CHANNEL_TOKEN_KEY = AttributeKey.valueOf("netty_channel_token");  
    //播放器和websocket默认token的固定值
    public static final String CHANNEL_TOKEN_KEY_STR="netty.channel.token";
    //随机问题
    public static String[] info=new String[]{"ip","videoType","internetType"}; 
	
}
