package com.dongnao.jack.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ApplicationContextUtil implements ApplicationContextAware {

	public static ApplicationContext applicationContext;
	
	public void setApplicationContext(ApplicationContext agr)
			throws BeansException {
		applicationContext = agr;

	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	
}
