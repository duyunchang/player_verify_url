package com.lance.net.server.module;

public class Constants {

	/**
	 * MESSAGE返回结果
	 */
	public static final String MSG_SUCCESS = "success";// 成功
	public static final String MSG_FAIL = "fail";// 失败

	/**
	 * CODE返回结果
	 */
	public static final int CODE_SUCCESS = 0;// 成功
	public static final int CODE_FAIL = 1;// 失败

	
	/**
	 * websocket状态信息
	 */
	public static final String CONNECTED_YES = "1";// 已经连接
	public static final String CONNECTED_NO = "2";// 未连接
	public static final int CONNECTED_TIMEOUT = 60*60*2;// 2小时
	
	/**
	 * jerdis key
	 */
	public static final String VERIFICATIONCODE= "verificationCode";// 验证码
	public static final String PROBLEM= "problem";// 优先级高
	public static final String ANSWER = "answer";// 问题答案
	public static final String CURRFAILCOUNT="currFailCount"; //当前失败次数
	public static final String ISCONNECTED="isConnected";//是否连接
	public static final String CUEEENTONLINENUMBER="CurrentOnlineNumber";//当前websocket总登录次数
	public static final String CUEEENTONLOGINCOUNT="CurrentLoginCount";//当前单个uid登录次数
	public static final int CURRENTMAXLOGINCONNECT=5;//每个uid最大连接数
	public static final int MAXCURRFAILCOUNT=3;//每个播放器最大失败次数
	/**
	 * url key参数
	 */
	public static final String TOKEN= "token";
	public static final String UID = "uid";	
	public static final String TIME = "time";
	public static final String WSSIGN= "wsSign";
	public static final String PLAYTOKEN = "playToken";
	public static final String SIGN = "sign";
	public static final String QUESTION = "question";
	public static final String INFO = "info";
	

}
