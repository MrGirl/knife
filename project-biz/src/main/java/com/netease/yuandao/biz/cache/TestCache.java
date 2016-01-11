/**
 * 
 */
package com.netease.yuandao.biz.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.ibatis.cache.Cache;
import org.apache.log4j.Logger;

/**
 * @author hzzhangyuandao
 * @since 2016-01-11
 * a Test cache for mybatis.
 */
public class TestCache implements Cache {

	private static Logger logger = Logger.getLogger(TestCache.class);
	private String id;
	
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    
    private Map<Object,Object> cacheMap = new HashMap<Object,Object>();


    public TestCache(){
    }
    
    
    public TestCache(final String id){
    	this.id = id;
    	logger.debug("TestCache ini,id:"+id);
    }
	@Override
	public String getId() {
		return id;
	}

	@Override
	public void putObject(Object key, Object value) {
		cacheMap.put(key, value);
	}


	@Override
	public Object getObject(Object key) {
		return this.cacheMap.get(key);
	}


	@Override
	public Object removeObject(Object key) {
		return this.cacheMap.remove(key);
	}


	@Override
	public void clear() {
		this.cacheMap.clear();
	}


	@Override
	public int getSize() {
		return this.cacheMap.size();
	}


	@Override
	public ReadWriteLock getReadWriteLock() {
		return readWriteLock;
	}

}
