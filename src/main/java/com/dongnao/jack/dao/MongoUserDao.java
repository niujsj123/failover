package com.dongnao.jack.dao;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceOutput;

@Service
public class MongoUserDao implements IMongoUserDao {

	@Autowired
	@Qualifier("mongoTemplate1")
	MongoTemplate mt;

	public String insert(String param) {
		for (int i = 0; i < 10; i++) {

			JSONObject jo = new JSONObject();
			jo.put("name", "jack" + i);
			jo.put("age", 19);
			mt.insert(jo, "collect");
		}

		for (int i = 0; i < 10; i++) {

			JSONObject jo = new JSONObject();
			jo.put("name", "sam" + i);
			jo.put("age", 12);
			mt.insert(jo, "collect");
		}

		for (int i = 0; i < 10; i++) {

			JSONObject jo = new JSONObject();
			jo.put("name", "senvon" + i);
			jo.put("age", 43);
			mt.insert(jo, "collect");
		}

		return "OK";
	}

	public String sum(String param) {
		DBCollection col = mt.getCollection("collect");

		String mapStr = "function(){if(this.age==12) emit(this.age,this.name)}";

		String reduceStr = "function(key,values){var count=0;values.forEach(function(){count+=1;});var result={names:values,sum:count};return result;}";

		MapReduceOutput output = col.mapReduce(mapStr, reduceStr, "result",
				null);

		DBCollection outcol = output.getOutputCollection();

		DBCursor dbc = outcol.find();

		StringBuffer sb = new StringBuffer();

		while (dbc.hasNext()) {
			DBObject dbo = dbc.next();
			sb.append(dbo.toString());
			System.out.println(dbo.toString());
		}

		return sb.toString();
	}

}
