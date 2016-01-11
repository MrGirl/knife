package com.netease.yuandao.biz.service;

import com.netease.yuandao.biz.domain.Person;

/**
 * @author hzzhangyuandao
 * @since 2016-01-07
 */
public interface PersonService {
	public int create(Person person);

	public int delete(int id);

	public int update(Person person);

	public Person retrieve(int id);
}