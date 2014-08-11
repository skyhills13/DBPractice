import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;


public class MongoConnector {
	
	private DB db;
	private DBCollection coll;
	public void connectDB() throws UnknownHostException {
		
		MongoClient mongoClient = new MongoClient("10.73.45.54");
		db = mongoClient.getDB("blog");
		coll = db.getCollection("posts");
	}
	
	public void writePost(String title, String body){
		//insert
		BasicDBObject sortOption = new BasicDBObject("_id", -1);
		DBCursor cursor = coll.find().sort(sortOption).limit(1);
		int result = 0;
		if (cursor.hasNext()) {
			result = (int) cursor.next().get("id");
			System.out.println(result);
		}
		
		BasicDBObject doc = new BasicDBObject("title", title);
		doc.append("id", ++result).append("body", body).append("last_updated", new Date());
		coll.insert(doc);
	}
	
	public List<DBObject> showPosts() {
		ArrayList<DBObject> result = new ArrayList<DBObject>();
		DBCursor cursor = coll.find();	
		while(cursor.hasNext()) {
			result.add(cursor.next());
		}
		return result;
	}
	
	public void deletePost(int id){
		BasicDBObject findOption = new BasicDBObject("id", id);
		coll.remove(findOption);
	}
	
	public void updatePost(int id, String title, String body){
		BasicDBObject findOption = new BasicDBObject("id", id);
		BasicDBObject updatedDoc = new BasicDBObject("title", title);
		updatedDoc.append("id", id).append("body", body).append("last_updated", new Date());
		coll.find(findOption);
		coll.update(findOption, updatedDoc);
	}
}
