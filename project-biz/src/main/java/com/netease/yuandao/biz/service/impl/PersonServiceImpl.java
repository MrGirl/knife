
package com.netease.yuandao.biz.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.netease.yuandao.biz.domain.Person;
import com.netease.yuandao.biz.service.PersonService;
import com.netease.yuandao.biz.mapper.PersonMapper;

/**
 * @author hzzhangyuandao
 * @since 2016-01-07
 */
@Service
public class PersonServiceImpl implements PersonService {
	@Autowired
	private PersonMapper mapper;

	@Override
	public int create(Person person) {
		int num = mapper.create(person);
		return num == 0 ? num : person.getId();
	}

	@Override
	public int delete(int id) {
		return mapper.delete(id);
	}

	@Override
	public int update(Person person) {
		return mapper.update(person);
	}

	@Override
	public Person retrieve(int id) {
		return (Person) mapper.retrieve(id);
	}
}