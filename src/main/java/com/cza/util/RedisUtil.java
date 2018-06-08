package com.cza.util;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cza.config.RedisConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component
@Scope("singleton")
public class RedisUtil {
	private Logger logger = Logger.getLogger(RedisUtil.class);
	@Autowired
	private RedisConfig redisConfig;
	private JedisPool jedisPool = null;  
    /** 
     * 初始化Redis连接池 
     */  
	
	private void initPool() {
		JedisPoolConfig config = new JedisPoolConfig();  
        config.setMaxActive(redisConfig.getMaxActive());  
        config.setMaxIdle(redisConfig.getMaxIdle());  
        config.setMaxWait(redisConfig.getMaxWait());  
        config.setTestOnBorrow(redisConfig.getTestOnBorrow());  
        jedisPool = new JedisPool(config, redisConfig.getHost(), redisConfig.getPort(),redisConfig.getTimeout(),redisConfig.getPassword());  
	}
     /** 
      * 获取Jedis实例 
      * @return 
      */  
     public Jedis getJedis() {  
         try {  
             if (jedisPool == null) {  
            	 synchronized (RedisUtil.class) {
            		 if(jedisPool==null) {
            			 initPool();
            		 }
				}
             }
             if(jedisPool==null) {
            	 return null;
             }else {
            	 Jedis resource = jedisPool.getResource();  
            	 return resource;
             }
         } catch (Exception e) {  
        	 logger.error("getJedis has erro:",e);
         }  
         return null;
     }  
            
     /** 
      * 释放jedis资源 
      * @param jedis 
      */  
      public void returnResource(final Jedis jedis) {  
          if (jedis != null) {  
               jedisPool.returnResource(jedis);  
          }  
      }  
}
