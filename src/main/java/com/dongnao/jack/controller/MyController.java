package com.dongnao.jack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dongnao.jack.dao.IMongoUserDao;

@Controller
@RequestMapping("/my")
public class MyController {

	@Autowired
	IMongoUserDao dao;

	@Autowired
	@Qualifier("mongoTemplate1")
	MongoTemplate mt;

	@RequestMapping("/insert")
	public @ResponseBody String insert() {
		return dao.insert(null);
	}

	@RequestMapping("/sum")
	public @ResponseBody String sum() {

		return dao.sum(null);
	}
}
