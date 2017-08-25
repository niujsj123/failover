package com.dongnao.jack.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

@Controller
@RequestMapping("/test")
public class TestController {

	@Autowired
	@Qualifier("mongoTemplate1")
	MongoTemplate mt;

	// @Autowired
	// JedisCluster jedisCluster;
	//
	// @RequestMapping("/invoke")
	// public @ResponseBody String test() {
	// try {
	// for(int i = 0 ; i < 100 ; i++) {
	// jedisCluster.set("key" + i, "value" + i);
	// }
	// } catch(Exception e) {
	// e.printStackTrace();
	// }
	//
	// return "OK";
	// }

	@RequestMapping("/test")
	public @ResponseBody String test() {
//		for (int i = 0; i < 10000; i++) {
//			JSONObject jo = new JSONObject();
//			jo.put("name", "star"+i);
//			jo.put("age", i);
//			mt.insert(jo, "jackcol");
//		}
		GridFS fs = new GridFS(mt.getDb(),"collect");
		try {
			GridFSInputFile gfs = fs.createFile(new File("C:\\Users\\niujsj\\Downloads\\suxinhua-004.jpg"));
			gfs.put("filename", "sss");
			gfs.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		GridFSDBFile fs1 = fs.findOne("sss");
		InputStream is = fs1.getInputStream();
		
		try {
			FileOutputStream fos = new FileOutputStream("e:\\aa.jpg");
			
			byte[] bytes = new byte[1024];
			while(is.read(bytes)!= -1) {
				fos.write(bytes);
			}
			is.close();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		DBCollection col = mt.getCollection("mycon");
//		String map = "function(){emit(this.age,this.name)}";
//		String reduce = "function(key,values){var count=0;values.forEach(function(){count+=1;});var result={names:values,sum:count};return result;}";
//		MapReduceOutput op = col.mapReduce(map, reduce, "codeMapReduce",null);
//		
//		DBCollection codeCol = mt.getCollection("codeMapReduce");
//		
//		System.out.println(codeCol.find());
//		DBCursor cur = codeCol.find();
//		
//		StringBuffer sb = new StringBuffer();
//		
//		while(cur.hasNext()) {
//			DBObject dbo = cur.next();
//			sb.append(dbo.toString());
//			System.out.println(dbo.toString());
//		}
 catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "OK";
	}

}
