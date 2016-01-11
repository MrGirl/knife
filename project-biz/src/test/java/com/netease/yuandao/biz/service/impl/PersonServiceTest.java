package com.netease.yuandao.biz.service.impl;


import java.util.Date;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.netease.yuandao.biz.domain.Person;
import com.netease.yuandao.biz.service.PersonService;


@ContextConfiguration("classpath:/context-project-biz-service-test.xml")
public class PersonServiceTest extends AbstractJUnit4SpringContextTests{
	@Resource
	private PersonService personService;
	
	@Test
	public void testFindAll(){

		Person p  = new Person();
		p.setGender(true);
		p.setName("Tom");
		p.setTel("123456789111");
		p.setCreateTime(new Date());
		
		int id = personService.create(p);
		
		p = personService.retrieve(id);
		
		Assert.assertTrue(id == p.getId());
		
		personService.delete(id);
	}
}
