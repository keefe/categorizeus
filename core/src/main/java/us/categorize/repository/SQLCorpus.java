package us.categorize.repository;

import us.categorize.model.*;
import us.categorize.api.*;
import java.util.*;
import java.sql.*;

public class SQLCorpus implements Corpus{
    
    
    private Connection connection;
    
    public SQLCorpus(Connection connection){
        this.connection = connection;    
    }
    
    public boolean create(Message message){
     	String insert = "insert into messages(body,title,posted_by,link,img_width, img_height, thumb_width, thumb_height, thumb_link) values (?,?,?,?,?,?,?,?,?)";
		try {
			PreparedStatement stmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, message.getBody());
			stmt.setString(2, message.getTitle());
			stmt.setLong(3, message.getPostedBy().getId());
			stmt.setString(4, message.getLink());
			stmt.setInt(5, message.getImgWidth());
			stmt.setInt(6, message.getImgHeight());
			stmt.setInt(7, message.getThumbWidth());
			stmt.setInt(8, message.getThumbHeight());
			stmt.setString(9, message.getThumbLink());
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();
			long key = rs.getLong(1);
			message.setId(key);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;   
    }
    
    private void mapMessageRow(Message message, ResultSet rs) throws SQLException {
		message.setBody(rs.getString("body"));
		message.setTitle(rs.getString("title"));
		User user = new User();
		user.setId(rs.getLong("posted_by"));
		message.setPostedBy(user);
		message.setId(rs.getLong("id"));
		message.setLink(rs.getString("link"));
		message.setImgWidth(rs.getInt("img_width"));
		message.setImgHeight(rs.getInt("img_height"));
		message.setThumbWidth(rs.getInt("thumb_width"));
		message.setThumbHeight(rs.getInt("thumb_height"));
		message.setThumbLink(rs.getString("thumb_link"));
	}

    public boolean create(Tag tag){
        return readOrCreate(tag);
    }
    
    public boolean read(Message message){
        try{
            	String findMessage = "select * from messages where id=?";
    		PreparedStatement stmt = connection.prepareStatement(findMessage);
    		stmt.setLong(1, message.getId());
    		ResultSet rs = stmt.executeQuery();
    		if(rs.next()){
    		    mapMessageRow(message, rs);
    		}else{
    		    return false;
    		}
    		//continuing to be extremely inefficient, let's do ANOTHER query
    		//curious if doing another query vs doing a join above would lead to more efficient results
    		String findTags = "select * from message_tags, tags where message_tags.tag_id=tags.id AND message_id = ?";
    		System.out.println("Tags found as  " + findTags);
    		PreparedStatement findTagsStmt = connection.prepareStatement(findTags);
    		findTagsStmt.setLong(1, message.getId());
    		rs = findTagsStmt.executeQuery();
    		while(rs.next()){
    			Tag tag = new Tag(rs.getLong("id"), rs.getString("tag"));
    			System.out.println(tag.getTag());
    			message.getTags().add(tag);
    		}
    		return true;
        }catch(SQLException sqe){
            sqe.printStackTrace();
        }
        return false;
    }
    
    public boolean tagMessage(Message message, List<Tag> tags){
		String tagStatement = "insert into message_tags(message_id, tag_id) values (?,?)";
		try {
			for(Tag tag: tags){
				PreparedStatement stmt = connection.prepareStatement(tagStatement);
				stmt.setLong(1, message.getId());
				stmt.setLong(2, tag.getId());
				stmt.executeUpdate();
			}	
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block, this is particularly important because of unique constraint violations
			e.printStackTrace();
		}

		return false;            
    }

    public boolean read(Tag tag){
        try{
    		String findTag = "select * from tags where tag=?";
    		PreparedStatement stmt = connection.prepareStatement(findTag);
    		stmt.setString(1, tag.getTag());
    		ResultSet rs = stmt.executeQuery();
    		if(rs.next()){
    			tag.setId(rs.getLong("id"));
    			return true;
    		}
        }catch(SQLException sqe){
            sqe.printStackTrace();
        }
        return false;
    }
    public boolean readOrCreate(Tag tag){
        if(!read(tag)){
            try{
         		PreparedStatement insertStatement = connection.prepareStatement("insert into tags(tag) values(?)", Statement.RETURN_GENERATED_KEYS);
    			insertStatement.setString(1, tag.getTag());
    			insertStatement.executeUpdate();//this is synchronous, right?
    			ResultSet rs = insertStatement.getGeneratedKeys();
    			rs.next();
    			long key = rs.getLong(1);
    			tag.setId(key);
    		    return true;
            }catch(SQLException sqe){
                sqe.printStackTrace();
            }
        }
        return false;
        
    }

    public List<Message> tagSearch(TagSearchRequest request){
        return null;
    }
    public ThreadResponse findThread(ThreadRequest request){
        return null;
    }

    
    public boolean read(User user){
        return false;//TODO need to think about user validation setup
    }
    public boolean create(User user){
        return false;
    }

}
