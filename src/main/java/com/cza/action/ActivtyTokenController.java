package com.cza.action;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.cza.util.RedPacketConstant;
import com.cza.util.RedisUtil;
import com.cza.vo.ActivityToken;
import com.cza.vo.ActivityVo;

import redis.clients.jedis.Jedis;
@RestController
public class ActivtyTokenController {
	private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private RedisUtil redisUil;
    
    private Integer retry=2;
    
    @RequestMapping("/queryActivtyList")
    public List<String> queryActivtyList() {
    	Jedis jedis=null;
    	try {
    		jedis=redisUil.getJedis();
			List<String> results=new ArrayList<>();
			String activityList=jedis.get(RedPacketConstant.RED_PACKET_ACTIVITY_LIST_KEY);
			if(activityList!=null) {
				List<ActivityVo>activityVos=JSONArray.parseArray(activityList, ActivityVo.class);
				if(activityVos!=null&&activityVos.size()>0){
					for(ActivityVo vo:activityVos) {
						results.add(vo.getName());
					}
				}
			}
			return results;
		} catch (Exception e) {
			logger.error("queryActivtyList has error:",e);
		}finally {
			redisUil.returnResource(jedis);
		}
    	logger.info("return activty is empty!");
    	return null;
    }
    
    @RequestMapping("/queryToken")
    public String queryToken(@RequestParam(value="activitys") List<String> activtys) {
    	Jedis jedis=null;
    	try {
    		jedis=redisUil.getJedis();
			String activityList=jedis.get(RedPacketConstant.RED_PACKET_ACTIVITY_LIST_KEY);
			redisUil.returnResource(jedis);
			ActivityVo maxWeightActivity=null;
			if(activityList!=null) {
				List<ActivityVo>activityVos=JSONArray.parseArray(activityList, ActivityVo.class);
				if(activityVos!=null&&activityVos.size()>0){
					for(String activityName:activtys) {
						for(ActivityVo vo:activityVos) {
							if(vo.getName().equals(activityName)) {
								if(maxWeightActivity==null) {
									maxWeightActivity=vo;
								}else if(maxWeightActivity.getWeight()<vo.getWeight()) {
									maxWeightActivity=vo;
								}
							}
						}
					}
				}
			}
			logger.info("get maxWeight activity:{}",maxWeightActivity);
			return reduceStock(maxWeightActivity.getName());
		} catch (Exception e) {
			logger.error("queryToken has error:",e);
		}finally {
			redisUil.returnResource(jedis);
		}
    	return null;
    }
    
    private String reduceStock(String activityName) {
    	String key=RedPacketConstant.RED_PACKET_ACTIVITY_TOKEN_PREFIX_KEY+activityName;
    	String lockKey=activityName+".lock";
    	String value="1";
    	Jedis jedis=null;
    	try {
    		jedis=redisUil.getJedis();
    		while(retry-->0) {
    			Long issucess=jedis.setnx(lockKey, value);
        		if(1==issucess) {
        			jedis.expire(lockKey, 10);
        			String tokenList=jedis.get(key);
        			if(tokenList!=null) {
        				ActivityToken maxWeightToken=null;
        				List<ActivityToken>tokenVos=JSONArray.parseArray(tokenList, ActivityToken.class);
        				if(tokenVos!=null&&tokenVos.size()>0){
        					for(ActivityToken vo:tokenVos) {
        						if(vo.getStock()>0) {
        							if(maxWeightToken==null) {
        								maxWeightToken=vo;
        							}else if(maxWeightToken.getWeight()<vo.getWeight()) {
        								maxWeightToken=vo;
        							}
        						}
        					}
        				}
        				if(maxWeightToken!=null) {
            				maxWeightToken.setStock(maxWeightToken.getStock()-1);
            				jedis.set(key, JSONArray.toJSONString(tokenVos));
            				logger.info("return token:{}",maxWeightToken.getToken());
            				return maxWeightToken.getToken();
            			}
        			}
        		}
    		}
    	} catch (Exception e) {
			logger.error("queryToken has error:",e);
		}finally {
			if(jedis!=null) {
				jedis.del(lockKey);
			}
			redisUil.returnResource(jedis);
		}
    	logger.info("return token is empty!");
    	return null;
    }
}
