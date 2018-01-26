package com.lance.net.server.redis;
/**
 * author dyc
 */
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


@Service
public class RedisClient {

	@Autowired
	private JedisPool jedisPool;
	
	@Value("${my.redis-first-key}")
	private String firstKey;	
	@Value("${my.redis-first-expire-key}")
    private String onlyExpireKey;
	@Value("${my.connect-timeout}")
	private Integer timeOut;
		

	//+1
	public Long setsysOnlyExpireKeyAddLong(String key, String field, Long value) {
		Jedis jedis = null;
		Long hincrBy =(long)0;
		try {
			jedis = jedisPool.getResource();
			key=onlyExpireKey+":"+key; 
			
			jedis.watch(key); 
			hincrBy = jedis.hincrBy(key, field, value);
			jedis.expire(key, timeOut*2+10);
		} catch (Exception e) {
			System.out.printf("保存缓存，原因:%s" + e.getMessage(), e);
		} finally {
			
			jedis.unwatch();
			jedis.close();
		}
		
		return hincrBy;

	}
	
	public void setOnlyExpireKey(String key, String field, String value) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			key=onlyExpireKey+":"+key;           
			jedis.hset(key, field, value); 
			jedis.expire(key, timeOut*2+10);
		} catch (Exception e) {
			System.out.printf("保存缓存，原因:%s" + e.getMessage(), e);
		} finally {
			jedis.close();
		}

	}
	public String getOnlyExpireKey(String key, String field) {
		Jedis jedis = null;
		String hget =null;
		try {
			jedis = jedisPool.getResource();
			key=onlyExpireKey+":"+key;						
			
			if(jedis.exists(key)){
				hget= jedis.hget(key, field);
			}
			
		} catch (Exception e) {
			System.out.printf("保存缓存，原因:%s" + e.getMessage(), e);
		} finally {
			jedis.close();
		}

		return hget;
	}

	public void removeOnlyExpireKey(String key, String field) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			key=onlyExpireKey+":"+key;
			if(jedis.exists(key)){
				jedis.hdel(key.getBytes(), field.getBytes());
			}
			
		} catch (Exception e) {
			System.out.printf("保存缓存，原因:%s" + e.getMessage(), e);
		} finally {
			jedis.close();
		}

	}
	
	public void setHash(String key, String field, String value, Integer timeOut) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			/*
			 * if (timeOut > 0) { jedis.hsetnx(keyArr, fieldArr, bts);
			 * jedis.expire(keyArr, timeOut); } else { jedis.hsetnx(keyArr,
			 * fieldArr, bts); }
			 */
			key=firstKey+":"+key;
			if (timeOut > 0) {
				jedis.hset(key, field, value);
				jedis.expire(key, timeOut);
			} else {
				jedis.hset(key, field, value);
			}
		} catch (Exception e) {
			System.out.printf("保存缓存，原因:%s" + e.getMessage(), e);
		} finally {
			jedis.close();
		}

	}
	
	public String getInHashObjArr(String key, String field) {
		String bys = null;

		Jedis jedis = null;
		try {
			
			jedis = jedisPool.getResource();
			key=firstKey+":"+key;
			
			if (jedis.exists(key)) {
				bys = jedis.hget(key, field);
			}
		} catch (Exception e) {
			System.out.printf("获取缓存，原因:%s" + e.getMessage(), e);
		} finally {
			jedis.close();
		}

		return bys;
	}
	
	public List<String> getInHashMap(String key, String...  fields) {
		List<String> bys = null;

		Jedis jedis = null;
		try {
			
			jedis = jedisPool.getResource();
			
			key=firstKey+":"+key;
			if (jedis.exists(key)) {
				
				bys = jedis.hmget(key, fields);
				
			}
		} catch (Exception e) {
			System.out.printf("获取缓存，原因:%s" + e.getMessage(), e);
		} finally {
			jedis.close();
		}

		return bys;
	}
	
	

	public void removeInHashObj(String key,String field){
		
		Jedis jedis = null;
		try {
			jedis =jedisPool.getResource();
						
			key=firstKey+":"+key;
			if (jedis.exists(key)) {
				jedis.hdel(key.getBytes(), field.getBytes());
			}
		} catch (Exception e) {
			System.out.printf("删除缓存，原因:%s" + e.getMessage(), e);
		} finally {
			jedis.close();
		}
	}
	
	
	

}
