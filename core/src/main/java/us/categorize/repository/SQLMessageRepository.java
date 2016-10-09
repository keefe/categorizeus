package us.categorize.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import us.categorize.model.Message;
import us.categorize.model.MessageRelation;
import us.categorize.model.MessageThread;
import us.categorize.model.Tag;
import us.categorize.model.ThreadCriteria;

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
		message.setLink(rs.getString("link"));
		return message;
	}

	@Override
	public boolean addMessage(Message message){
		String insert = "insert into messages(body,title,posted_by,link) values (?,?,?,?)";
		try {
			PreparedStatement stmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, message.getBody());
			stmt.setString(2, message.getTitle());
			stmt.setLong(3, message.getPostedBy().getUserId());
			stmt.setString(4, message.getLink());
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
	
	public boolean updateMessage(Message message){
		String update = "update messages set(body, title, posted_by, link) = (?,?,?,?) where id = ?";
		try{
			PreparedStatement stmt = connection.prepareStatement(update);//fold this into the insert?
			stmt.setString(1, message.getBody());
			stmt.setString(2, message.getTitle());
			stmt.setLong(3, message.getPostedBy().getUserId());
			stmt.setString(4, message.getLink());
			stmt.setLong(5, message.getId());
			stmt.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	@Override
	public List<Message> findMessages(Tag[] tags) {
		return findMessages(tags, null, null);
	}

	public List<Message> findMessages(Tag[] tags, Integer startId, Integer limit) {//TODO think about callback form or streaming, something not in RAM
		
		String tagClause = "";
		for(Tag tag : tags){
			if(!"".equals(tagClause)) tagClause = tagClause + " or ";
			tagClause = tagClause + "tag_id = " + tag.getId();
		}
		String sql = "SELECT messages.* from messages, message_tags where message_tags.message_id = messages.id AND ("+tagClause+")";
		if(startId!=null){
			sql+=" where id<"+startId;//TODO more sophisticated query for different ordering etc
		}
		if(tags.length==0){
			sql = "SELECT messages.* from messages";
			if(startId !=null){
				sql+=" where id<"+startId;
			}
		}
		sql+=" order by id DESC";
		if(limit!=null){
			sql+=" LIMIT " + limit;
		}
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
	public MessageThread loadThread(ThreadCriteria criteria) {
		// TODO research combining these calls
		List<Message> rootMessages = findMessages(criteria.getSearchTags(), criteria.getStartingId(), criteria.getMaxResults());
		Set<Long> seenMessages = new HashSet<>();
		List<Long> messageIds = new LinkedList<>();
		for(Message message: rootMessages){
			seenMessages.add(message.getId());
			messageIds.add(message.getId());
		}
		MessageThread thread = new MessageThread();
		thread.setSearchCriteria(criteria);
		thread.setThread(rootMessages);
		List<Long> newMessageIds = new LinkedList<>();
		List<long[]> relations = new LinkedList<>();
		loadTransitiveThread(thread, messageIds, seenMessages,newMessageIds, relations, 0);
		for(long newId : newMessageIds){//TODO this is obviously very inefficient, but efficiency after correctness
			try {
				Message message = getMessage(newId);
				thread.getThread().add(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Map<Long, Message> id2Message = new HashMap<>();//just more inefficient by the line, this is where maintaining 1:1 is good
		for(Message message: thread.getThread()){
			id2Message.put(message.getId(), message);
		}
		Map<Long, Tag> id2Tag = new HashMap<>();
		for(Tag t : thread.getSearchCriteria().getTransitiveTags()){
			id2Tag.put(t.getId(), t);
		}
		for(long[] rel : relations){
			MessageRelation relation = new MessageRelation(id2Message.get(rel[0]), id2Tag.get(rel[1]),id2Message.get(rel[2]));
			thread.getRelations().add(relation);
		}
		return thread;
	}
	private String buildOrList(List<Long> identifiers){//#TODO this is dying to be merged together into a nice generic function, like identity OrList and make an interface or something
		if(identifiers.size()==0){//yeah yeah copy paste, trying to nail down the basics
			return null;
		}
		String orList = "";//#TODO replace with stringbuilder, this is likely very expensive, also memoizable
		for(Long id : identifiers){
			if(!"".equals(orList))orList =orList+",";
			orList = orList + id;
		}
		return "IN ("+orList+")";
	}
	//TODO it is pretty confusing what source and sink means here and what to query, for things like repliesTo this is the pattern, maybe this is a property on the relationship or something, think about this
	private void loadTransitiveThread(MessageThread thread, List<Long> currentLevel, Set<Long> seenMessages, List<Long> newIds, List<long[]> relations, int level) {
		String sql = "SELECT message_relations.* from message_relations where";
		LinkedList<Long> tagIds = new LinkedList<>();
		for(Tag t : thread.getSearchCriteria().getTransitiveTags()) tagIds.push(t.getId());
		sql = sql+" tag_id "+buildOrList(tagIds);
		sql = sql+" AND message_sink_id " + buildOrList(currentLevel);
		currentLevel = new LinkedList<>();//dupes in here?
		System.out.println("Finding Related with \n" + sql);
		try {
			Statement stmt = connection.createStatement();
			ResultSet matching = stmt.executeQuery(sql);
			while(matching.next()){
				long source = matching.getLong("message_source_id");
				long sink = matching.getLong("message_sink_id");
				long tag = matching.getLong("tag_id");
				currentLevel.add(source);
				if(!seenMessages.contains(source)){
					seenMessages.add(source);
					newIds.add(source);
				}
				boolean found = false;//TODO this is really horrible, but this whole part is getting rewritten
				for(long[] oldRel:relations){
					if(oldRel[0]==source && oldRel[1]==tag&&oldRel[2]==sink){
						found = true;
						break;
					}
				}
				if(!found){
					relations.add(new long[]{source, tag, sink});					
				}
			}
			level++;
			if(level < thread.getSearchCriteria().getMaxTransitiveDepth() && currentLevel.size()>0){
				loadTransitiveThread(thread, currentLevel, seenMessages, newIds, relations, level);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public boolean tag(long messageId, Tag[] tags) {
		String tagStatement = "insert into message_tags(message_id, tag_id) values (?,?)";
		try {
			for(Tag tag: tags){
				PreparedStatement stmt = connection.prepareStatement(tagStatement);
				stmt.setLong(1, messageId);
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
	
	@Override
	public boolean tag(Message message, Tag[] tags) {
		return tag(message.getId(), tags);
	}
	
	public boolean tag(long messageIds[], Tag[] tags){
		boolean allGood = true;
		for(long messageId : messageIds){
			allGood = allGood && tag(messageId, tags);
		}
		return allGood;
	}


}
