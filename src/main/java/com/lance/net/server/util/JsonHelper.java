package com.lance.net.server.util;

import java.util.List;
import java.util.Map;

import net.sf.json.xml.XMLSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JsonHelper {
	private static  Logger logger = LogManager.getLogger();
	
	public static String toJsonStr(Object obj) {
		try{
			return JSON.toJSONString(obj, SerializerFeature.WriteDateUseDateFormat);
		}catch(Exception e){
			logger.error(obj+" toJsonStr fail"+e.getMessage());
			return null;
		}
	}

	public static <T> T toJSONObject(String jsonStr,Class<T> type){
		try{
			return JSON.parseObject(jsonStr,type);
		}catch(Exception e){
			logger.error(jsonStr +","+type+" toJSONObject fail"+e.getMessage());
			return null;
		}
		
	}
	
	public static  <T> List<T> toBeanList(String jsonStr,Class<T> type){
		try{
			return JSON.parseArray(jsonStr, type);
		}catch(Exception e){
			logger.error(jsonStr +","+type+" toObjectList fail"+e.getMessage());
			return null;
		}
	}
	@SuppressWarnings("rawtypes")
	public static  Map beanToMap(Object obj){
		try{
			String jsonStr=toJsonStr(obj);
			return toJSONObject(jsonStr, Map.class);
		}catch(Exception e){
			logger.error(obj+"to map error result:"+e.getMessage());
			return null;
		}
	}


	public static <T> T toGenericBean(String jsonStr,TypeReference<T> tr){
		try {
			return JSON.parseObject(jsonStr, tr);
		} catch (Exception e) {
			logger.error(jsonStr + " to GenericBean fail " + e.getMessage());
			return null;
		}
	}
	
	public static <T> T toJSONObjectSlash(String jsonStr,Class<T> type){
		String reInfo = StringUtils.EMPTY;
		if(StringUtils.isNotEmpty(jsonStr)){
			String temp = jsonStr.replaceAll("\\\\", StringUtils.EMPTY);
			reInfo = temp.substring(1, temp.length()-1);
		}
		return JSON.parseObject(reInfo,type);
	}
	
	public static <T> T xmlToJson(String xmlInfo,Class<T> type){
		net.sf.json.JSON json = new XMLSerializer().read(xmlInfo);
		String temp = json.toString().replaceAll("@", "");
		if(temp.startsWith("[") && temp.endsWith("]")){
			temp = temp.substring(1, temp.length()-1);
		}
		System.out.println(temp);
		return JsonHelper.toJSONObject(temp, type);
	}
	
	
}
