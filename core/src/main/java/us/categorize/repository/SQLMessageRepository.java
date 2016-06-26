package us.categorize.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
			message = new Message();
			message.setBody(rs.getString("body"));
			message.setTitle(rs.getString("title"));
			message.setPostedBy(userRepository.find(rs.getLong("posted_by")));
			message.setId(rs.getLong("id"));
		}
		return message;
	}

	@Override
	public boolean addMessage(Message message) {
		
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Message> findThreads(List<Tag> tags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean tag(Message message, List<Tag> tags) {
		// TODO Auto-generated method stub
		return false;
	}

}
