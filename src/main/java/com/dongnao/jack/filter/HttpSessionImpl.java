package com.dongnao.jack.filter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.dongnao.jack.service.ApplicationContextUtil;
import com.mongodb.util.JSON;

import redis.clients.jedis.JedisCluster;

public class HttpSessionImpl implements HttpSession {

	private HttpServletRequest request;
    
    private HttpServletResponse response;
    
    private ThreadLocal<String> local = new ThreadLocal<String>();
    
    private ThreadLocal<Map<String,String>> maplocal = new ThreadLocal<>();
    
    private JedisCluster jedisCluster = (JedisCluster) ApplicationContextUtil
			.getApplicationContext().getBean("jedisCluster");
    
	public HttpSessionImpl(HttpServletRequest request, HttpServletResponse response) {
		this.request= request;
		this.response = response;
	}

	@Override
	public long getCreationTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		String sessionId = getSessionId();
		
		List<String> lists = jedisCluster.hmget(sessionId, name);
		return lists.get(0);
	}

	private String getSessionId() {
		Cookie[] cookies = request.getCookies();
		for (Cookie c:cookies){
			if (c.getName().equals("sessionId")){
				return c.getValue();
			}
		}
		return null;
	}

	@Override
	public Object getValue(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getValueNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(String name, Object value) {
		
		String sessionId = getSessionId();
		if (null ==sessionId){
			if (local.get() != null){
				sessionId = local.get();
			} else {
				sessionId = UUID.randomUUID().toString();
				local.set(sessionId);
			}
		}
		if (maplocal.get()!=null){
			maplocal.get().put(name,JSON.serialize(value));
		} else {
			Map<String,String> map = new HashMap<>();
			map.put(name, JSON.serialize(value));
			maplocal.set(map);
		}
		jedisCluster.del(sessionId);
		jedisCluster.hmset(sessionId, maplocal.get());
		setCookie(sessionId);
	}

	private void setCookie(String sessionId) {
		Cookie c = new Cookie("sessionId", sessionId);
		c.setPath("/");
		response.addCookie(c);
	}

	@Override
	public void putValue(String name, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAttribute(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeValue(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isNew() {
		// TODO Auto-generated method stub
		return false;
	}

}
