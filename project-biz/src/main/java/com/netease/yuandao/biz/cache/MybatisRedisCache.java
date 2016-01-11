/**
 * 
 */
package com.netease.yuandao.biz.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
 
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.ibatis.cache.Cache;
import org.apache.log4j.Logger;
import org.springframework.util.ResourceUtils;

import com.netease.yuandao.biz.cache.utils.SerializeUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
 

/**
 * @author ZhangYuandao
 * @version 2015-09-21
 * mysql redis cache  to the query engine.cache the updated record, once the cud is invoked ,the local record cache is 
 * updated , hence the redis cache is a cache supporting incremental modification.
 */
public class MybatisRedisCache implements Cache {
     
    private static final Logger logger = Logger.getLogger(MybatisRedisCache.class);
     
    /** The ReadWriteLock. */
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
 
    private JedisSentinelPool jedisPool;
    private static final int DB_INDEX = 1;
    private final String COMMON_CACHE_KEY = "mybatis-redis-cache:";
    private static final String UTF_8 = "utf-8";
  
    private String id;
    
    private Properties properties;
    
    
    //default mysql redis cache is off.
    private static boolean isMysqlRedisCacheOn = false; 
    
    
    
    {
        properties = getProp();
        
		if (properties == null) {
			logger.error("redis.properties not found.");
		} else {

			JedisPoolConfig config = new JedisPoolConfig();

			/*
			 * judge whether the redis is on and mysql.redis.cache is on
			 */

			String isRedisOn = properties.getProperty("redisStatus","off");
			String mysqlRedisCache = properties.getProperty("mysql.cache.redis","on");
			
			if( isRedisOn.equals("on")&&mysqlRedisCache.equals("on")){
				isMysqlRedisCacheOn = true;
			}
			
			if(isMysqlRedisCacheOn){
			
				config.setMaxTotal(500);
				config.setTestOnBorrow(false);
				config.setMaxIdle(5);
				config.setTestOnBorrow(true);
	
				String sentinelAddressList = properties.getProperty("redis.sentinel.address");
				String addressList[] = sentinelAddressList.split(",");
				Set<String> aset = new HashSet<String>();
				for (String s : addressList) {
					aset.add(s);
				}
				
				String redisMaster = properties.getProperty("redis.sentinel.master");
				String password = properties.getProperty("redis.sentinel.password");
				jedisPool = new JedisSentinelPool(redisMaster, aset, config, 3000, password);
			}
		}
    }
     
    
    /**
     * 按照一定规则标识key
     */
    private String getKey(Object key) {
        StringBuilder accum = new StringBuilder();
        accum.append(COMMON_CACHE_KEY);
        accum.append(this.id).append(":");
        accum.append(DigestUtils.md5Hex(String.valueOf(key)));
        return accum.toString();
    }
  
    /**
     * redis key规则前缀
     */
    private String getKeys() {
        return COMMON_CACHE_KEY + this.id + ":*";
    }
   
    /**
     * 加载项目redis连接属性文件
     */
    private Properties getProp(){
        if(properties == null || properties.isEmpty()){
            String propName = "classpath:redis.properties";
            properties = new Properties();
            InputStream is = null;
            BufferedReader bf = null;
            try {
//                is= this.getClass().getResourceAsStream(propName);//将地址加在到文件输入流中
//            	is = MybatisRedisCache.class.getClassLoader().getSystemResourceAsStream(propName);
            	File resource = ResourceUtils.getFile(propName);
            	is = new FileInputStream(resource);
                bf = new BufferedReader(new InputStreamReader(is,"UTF-8"));//转为字符流，设置编码为UTF-8防止出现乱码
                properties.load(bf);//properties对象加载文件输入流
                
            } catch (UnsupportedEncodingException e) {
                logger.error(propName + "编码格式转换失败，不支持指定编码。" +  e);
            } catch (FileNotFoundException e) {
                logger.error(propName + "属性文件,不存在。" +  e);
            } catch (IOException e) {
                logger.error(propName + "属性文件,读取失败。" +  e);
            } catch (Exception e) {
                logger.error(propName + "属性文件,读取失败。" +  e);
            } finally {
                try {//文件流关闭
                    if(bf != null){
                        bf.close();
                    }
                    if(is != null ){
                        is.close();
                    }
                } catch (IOException e) {
                    logger.error("关闭文件流失败。" +  e);
                }
            }
        }
        return properties;
    }
 
    public MybatisRedisCache() {
    }
 
    public MybatisRedisCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("必须传入ID");
        }
        logger.debug("MybatisRedisCache:id=" + id);
        this.id = id;
    }
 
    @Override
    public String getId() {
        return this.id;
    }
 
    @Override
    public int getSize() {
    	
    	if(!isMysqlRedisCacheOn) return 0;
    	
        Jedis jedis = null;
        int result = 0;
        boolean borrowOrOprSuccess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.select(DB_INDEX);
            Set<byte[]> keys = jedis.keys(getKeys().getBytes(UTF_8));
            if (null != keys && !keys.isEmpty()) {
                result = keys.size();
            }
            logger.debug(this.id+"---->>>>总缓存数:" + result);
        } catch (Exception e) {
            borrowOrOprSuccess = false;
            if (jedis != null)
                jedisPool.returnBrokenResource(jedis);
        } finally {
            if (borrowOrOprSuccess)
                jedisPool.returnResource(jedis);
        }
        return result;
 
    }
 
    @SuppressWarnings("rawtypes")
	@Override
    public void putObject(Object key, Object value) {
    	
    	if(!isMysqlRedisCacheOn) return;
    	
        Jedis jedis = null;
        boolean borrowOrOprSuccess = true;
        try {
            jedis = jedisPool.getResource();
            
            if(jedis == null)
            	return;
            
            jedis.select(DB_INDEX);
             
            byte[] keys = getKey(key).getBytes(UTF_8);
            //if value is collecton ,and size is 0 , do not put it in redis
            if(value instanceof Collection && ((Collection) value).size() == 0){
            	
            }else{
            
            jedis.set(keys, SerializeUtil.serialize(value));
            logger.debug("添加缓存--------"+this.id);
            logger.debug("add redis value:"+value);
            }
            //getSize();
        } catch (Exception e) {
        	logger.error("mysql redis cache error!", e);
            borrowOrOprSuccess = false;
            if (jedis != null)
                jedisPool.returnBrokenResource(jedis);
        } finally {
            if (borrowOrOprSuccess)
                jedisPool.returnResource(jedis);
        }
 
    }
 
    @SuppressWarnings("rawtypes")
	@Override
    public Object getObject(Object key) {
    	if(!isMysqlRedisCacheOn)
    		return null;
    	
        Jedis jedis = null;
        Object value = null;
        boolean borrowOrOprSuccess = true;
        try {
            jedis = jedisPool.getResource();
            
            if(jedis == null)
            	return null;
            
            jedis.select(DB_INDEX);
            value = SerializeUtil.unserialize(jedis.get(getKey(key).getBytes(UTF_8)));
            
            
            //当value为集合类时,且大小为空时,返回空。
            if(value instanceof List){
            	if(((List) value).size() == 0)
            		value = null;
            }
            logger.debug("从缓存中获取-----"+this.id);
            logger.debug("get redis value:"+value);
            //getSize();
        } catch (Exception e) {
        	logger.warn("mybatis redis cache error!", e);
            borrowOrOprSuccess = false;
            if (jedis != null)
                jedisPool.returnBrokenResource(jedis);
            
            //if exception occured such as net interrupt or port close , set value to null.
            //this is to see we force the mybatis to query db all the time as it does not hit the cache.
            value = null;

        } finally {
            if (borrowOrOprSuccess)
                jedisPool.returnResource(jedis);
        }
        return value;
    }
 
    @Override
    public Object removeObject(Object key) {
    	if(!isMysqlRedisCacheOn)
    		return null;
    	
        Jedis jedis = null;
        Object value = null;
        boolean borrowOrOprSuccess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.select(DB_INDEX);
            value = jedis.del(getKey(key).getBytes(UTF_8));
            logger.debug("LRU算法从缓存中移除-----"+this.id);
            //getSize();
        } catch (Exception e) {
            borrowOrOprSuccess = false;
            if (jedis != null)
                jedisPool.returnBrokenResource(jedis);
        } finally {
            if (borrowOrOprSuccess)
                jedisPool.returnResource(jedis);
        }
        return value;
    }
 
    @Override
    public void clear() {
    	
    	if(!isMysqlRedisCacheOn)
    		return ;
    	
        Jedis jedis = null;
        boolean borrowOrOprSuccess = true;
        try {
            jedis = jedisPool.getResource();
            
            if(jedis == null)
            	return;
            
            jedis.select(DB_INDEX);
            Set<byte[]> keys = jedis.keys(getKeys().getBytes(UTF_8));
            logger.debug("出现CUD操作，清空对应Mapper缓存======>"+keys.size());
            for (byte[] key : keys) {
                jedis.del(key);
            }
            //下面是网上流传的方法，极大的降低系统性能，没起到加入缓存应有的作用，这是不可取的。
            //jedis.flushDB();
            //jedis.flushAll();
        } catch (Exception e) {
        	logger.error("mysql redis cache clear failed", e);
            borrowOrOprSuccess = false;
            if (jedis != null)
                jedisPool.returnBrokenResource(jedis);
        } finally {
            if (borrowOrOprSuccess)
                jedisPool.returnResource(jedis);
        }
    }
 
    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }
    
    public void shutdown(){
    	if(this.jedisPool != null){
    		jedisPool.close();
    		jedisPool.destroy();
    	}
    }
     
    public static void main(String args[]){
    
    	MybatisRedisCache redisCache = new MybatisRedisCache(1+"");
    	redisCache.putObject("a", "a");
    	Object obj = redisCache.getObject("a");
    	System.err.println(obj);
    	
    	redisCache.shutdown();
    }
}
