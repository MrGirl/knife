/**
 * 
 */
package com.netease.yuandao.biz.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhangyuandao
 * @since 2016-01-07
 *
 */
public class Person implements Serializable{


	private static final long serialVersionUID = 1722106666432451773L;

	private Integer id;
	
	private Boolean gender;
	
	private String name;
	
	private String tel;
	
	private Date createTime;
	
	private Date modifyTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Boolean getGender() {
		return gender;
	}

	public void setGender(Boolean gender) {
		this.gender = gender;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	
	
	
}
