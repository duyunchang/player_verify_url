package com.lance.net.server.util;

import org.apache.commons.lang3.RandomUtils;

public class RandomUtil {

	public static Integer RandomU(int max){
		
		return  RandomUtils.nextInt(0,max);
		
	}
	
}
