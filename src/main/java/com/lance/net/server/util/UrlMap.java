package com.lance.net.server.util;

import java.util.HashMap;
import java.util.Map;

public class UrlMap {
	
	
	/**
	 * 解析出url参数中的键值对 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
	 * 
	 * @param URL
	 *            url地址
	 * @return url请求参数部分
	 */
	public static Map<String, String> URLRequest(String strUrlParam) {
		Map<String, String> mapRequest = new HashMap<String, String>();
		// 每个键值为一组 www.2cto.com
		String[] arrSplit  = strUrlParam.split("[&]");
		String[] arrSplitEqual = null;
		
		for (String strSplit : arrSplit) {			
			arrSplitEqual = strSplit.split("[=]");
			// 解析出键值
			if (arrSplitEqual.length == 2) {
				// 正确解析
				mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

			}
		}
		
		return mapRequest;
	}
	
	public static void main(String[] args) {
		System.out.println(URLRequest("play=xx&ll=xx"));
	}

}
