package us.categorize.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import us.categorize.model.Tag;

public class SQLTagRepository implements TagRepository {

	private Map<String, Tag> label2Tag = new ConcurrentHashMap<>();//rudimentary caching
	private Connection connection;
	public SQLTagRepository(Connection conn){
		this.connection = conn;
	}
	
	@Override
	public Tag tagFor(String label) throws Exception {
		// TODO Auto-generated method stub
		if(label2Tag.containsKey(label)){
			return label2Tag.get(label);
		}
		//yep, hand tooled SQL
		Tag tag = lookupTag(label);
		if(tag==null){
			PreparedStatement insertStatement = connection.prepareStatement("insert into tags(tag) values(?)");
			insertStatement.setString(1, label);
			insertStatement.executeUpdate();//this is synchronous, right?
			//#TODO error checking for concurrent writes here
			tag = lookupTag(label);
		}
		return tag;
	}

	private Tag lookupTag(String label) throws SQLException {
		Tag tag = null;
		String findTag = "select * from tags where tag='"+label+"'";
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(findTag);
		if(rs.next()){
			tag = new Tag();
			tag.setId(rs.getLong("id"));
			tag.setTag(rs.getString("tag"));
			label2Tag.put(tag.getTag(), tag);
		}
		return tag;
	}
	
	

}
