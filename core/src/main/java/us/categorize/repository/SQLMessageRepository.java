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

import us.categorize.communication.query.ThreadCriteria;
import us.categorize.model.Message;
import us.categorize.model.MessageRelation;
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
		//continuing to be extremely inefficient, let's do ANOTHER query
		//curious if doing another query vs doing a join above would lead to more efficient results
		String findTags = "select * from message_tags, tags where message_tags.tag_id=tags.id AND message_id = ?";
		System.out.println("Tags found as  " + findTags);
		PreparedStatement findTagsStmt = connection.prepareStatement(findTags);
		findTagsStmt.setLong(1, id);
		rs = findTagsStmt.executeQuery();
		while(rs.next()){
			Tag tag = new Tag(rs.getLong("id"), rs.getString("tag"));
			System.out.println(tag.getTag());
			message.getTags().add(tag);
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
		message.setImgWidth(rs.getInt("img_width"));
		message.setImgHeight(rs.getInt("img_height"));
		message.setThumbWidth(rs.getInt("thumb_width"));
		message.setThumbHeight(rs.getInt("thumb_height"));
		message.setThumbLink(rs.getString("thumb_link"));
		return message;
	}

	@Override
	public boolean addMessage(Message message){
		String insert = "insert into messages(body,title,posted_by,link,img_width, img_height, thumb_width, thumb_height, thumb_link) values (?,?,?,?,?,?,?,?,?)";
		try {
			PreparedStatement stmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, message.getBody());
			stmt.setString(2, message.getTitle());
			stmt.setLong(3, message.getPostedBy().getUserId());
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
	
	public boolean updateMessage(Message message){
		String update = "update messages set(body, title, posted_by, link, img_width, img_height, thumb_width, thumb_height, thumb_link) = (?,?,?,?,?,?,?,?,?) where id = ?";
		try{
			PreparedStatement stmt = connection.prepareStatement(update);//fold this into the insert?
			stmt.setString(1, message.getBody());
			stmt.setString(2, message.getTitle());
			stmt.setLong(3, message.getPostedBy().getUserId());
			stmt.setString(4, message.getLink());
			stmt.setInt(5, message.getImgWidth());
			stmt.setInt(6, message.getImgWidth());
			stmt.setInt(7, message.getThumbWidth());
			stmt.setInt(8, message.getThumbHeight());
			stmt.setString(9, message.getThumbLink());
			stmt.setLong(10, message.getId());
			stmt.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	@Override
	public List<Message> findMessages(Tag[] tags) {
		return findMessages(tags, null, null, false);
	}
	
	private String tagSubquery(Tag tag){
		String sql = "exists (select 1 from message_tags where messages.id = message_id and tag_id="+tag.getId();
		sql = sql+")";
		return sql;
	}

	public List<Message> findMessages(Tag[] tags, Integer startId, Integer limit, boolean reverse) {//TODO think about callback form or streaming, something not in RAM
		String idOp = "<";
		String sortOp = "DESC";
		if(reverse){
			idOp = ">";
			sortOp = "ASC";
		}
		String sql = "";
		if(tags.length==0){
			sql = "SELECT messages.id* from messages";
			if(startId !=null){
				sql+=" where id"+idOp+startId;
			}
		}else{
			sql = "SELECT messages.* from messages, message_tags where message_tags.message_id = messages.id AND tag_id = "+tags[0].getId();
			for(int i=1; i<tags.length;i++){
				sql = sql + " AND " + tagSubquery(tags[i]);
			}
		}
		if(startId!=null){
			sql+=" AND id"+idOp+startId;//TODO more sophisticated query for different ordering etc
		}

		sql+=" order by id ";
		sql+=sortOp;
		if(limit!=null){
			sql+=" LIMIT " + limit;
		}
		System.out.println(sql);
		LinkedList<Message> messages = new LinkedList<Message>(); 
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			LinkedList<Long> messageIds = new LinkedList<Long>();//TODO check if we should be using HashSet<Long> here, ugh ordering issue
			while(rs.next()){
				if(reverse){
					messageIds.addFirst(rs.getLong("id"));
				}else{
					messageIds.add(rs.getLong("id"));
				}
			}
			for(long msgId : messageIds){
				messages.add(getMessage(msgId));
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
		System.out.println(criteria.toString());
		List<Message> rootMessages = findMessages(criteria.getSearchTags(), criteria.getStartingId(), criteria.getMaxResults(), criteria.isReverse());
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
				thread.getRelatedMessages().add(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Map<Long, Message> id2Message = new HashMap<>();//just more inefficient by the line, this is where maintaining 1:1 is good
		for(Message message: thread.getThread()){
			id2Message.put(message.getId(), message);
		}
		for(Message message: thread.getRelatedMessages()){
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
		if(currentLevel.size()==0) return;
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

	@Override
	public boolean relate(MessageRelation relation) {
		String relationStatement = "insert into message_relations(message_source_id, tag_id, message_sink_id) values (?,?,?)";
		try {
			PreparedStatement stmt = connection.prepareStatement(relationStatement);
			stmt.setLong(1, relation.getSource().getId());
			stmt.setLong(2, relation.getRelation().getId());
			stmt.setLong(3, relation.getSink().getId());
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block, this is particularly important because of unique constraint violations
			e.printStackTrace();
		}
		return false;
	}

	
	

}
