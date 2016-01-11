package com.netease.yuandao.biz.mapper;

import com.netease.yuandao.biz.domain.Person;

/**
 * @author hzzhangyuandao
 * @since 2016-01-07
 */
public interface PersonMapper {
	public int create(Person person);

	public int delete(int id);

	public int update(Person person);

	public Person retrieve(int id);
}