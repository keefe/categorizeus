package us.categorize.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import us.categorize.model.Message;
import us.categorize.model.MessageThread;
import us.categorize.model.Tag;

public class SQLMessageRepository implements MessageRepository {

	private Connection connection;
	private UserRepository userRepository;
	
	public SQLMessageRepository(Connection connection, UserRepository userRepository){
		this.connection = connection;
		this.userRepository = userRepository;
	}
	
	@Override
	public Message getMessage(long id) throws Exception {//#TODO establish 1:1 object relationship
		String findMessage = "select * from messages where id=?";
		PreparedStatement stmt = connection.prepareStatement(findMessage);
		stmt.setLong(1, id);
		ResultSet rs = stmt.executeQuery();
		Message message = null;
		if(rs.next()){
			message = mapMessageRow(rs);
		}
		return message;
	}

	private Message mapMessageRow(ResultSet rs) throws SQLException, Exception {
		Message message;
		message = new Message();
		message.setBody(rs.getString("body"));
		message.setTitle(rs.getString("title"));
		message.setPostedBy(userRepository.find(rs.getLong("posted_by")));
		message.setId(rs.getLong("id"));
		return message;
	}

	@Override
	public boolean addMessage(Message message){
		String insert = "insert into messages(body,title,posted_by) values (?,?,?)";
		try {
			PreparedStatement stmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, message.getBody());
			stmt.setString(2, message.getTitle());
			stmt.setLong(3, message.getPostedBy().getUserId());
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

	@Override
	public boolean postMessage(String body) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MessageThread getThread(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Message> findMessages(List<Tag> tags) {
		
		String tagClause = "";
		for(Tag tag : tags){
			if("".equals(tagClause)) tagClause = tagClause + " or ";
			tagClause = tagClause + "tag_id = " + tag.getId();
		}
		String sql = "SELECT messages.* from messages, message_tags where message_tags.message_id = messages.id AND "+tagClause;
		System.out.println(sql);
		List<Message> messages = new LinkedList<Message>(); 
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				messages.add(mapMessageRow(rs));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return messages;		
		
	}

	@Override
	public List<Message> findThreads(List<Tag> tags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean tag(Message message, Tag[] tags) {
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

}
