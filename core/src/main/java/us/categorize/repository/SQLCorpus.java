package us.categorize.repository;

import us.categorize.model.*;
import us.categorize.api.*;
import us.categorize.communication.query.ThreadCriteria;

import java.util.*;
import java.sql.*;
import java.io.*;


public class SQLCorpus implements Corpus{
    
    private Connection connection;
    private String initializeSQLBase = "sql/basic/";
    
    public SQLCorpus(Connection connection){
        this.connection = connection;    
    }
    
    public boolean create(Message message){
     	String insert = "insert into messages(body,title,posted_by,link,img_width, img_height, thumb_width, thumb_height, thumb_link) values (?,?,?,?,?,?,?,?,?)";
		try {
			PreparedStatement stmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
			mapMessageUpdate(message, stmt);
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
    public boolean update(Message message){
		String update = "update messages set(body, title, posted_by, link, img_width, img_height, thumb_width, thumb_height, thumb_link) = (?,?,?,?,?,?,?,?,?) where id = ?";
		try {
			PreparedStatement stmt = connection.prepareStatement(update);
			mapMessageUpdate(message, stmt);
			stmt.setLong(10, message.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;   
    }
    
    
    private void mapMessageUpdate(Message message, PreparedStatement stmt) throws SQLException
    {
    	stmt.setString(1, message.getBody());
		stmt.setString(2, message.getTitle());
		stmt.setLong(3, message.getPostedBy().getId());
		stmt.setString(4, message.getLink());
		stmt.setInt(5, message.getImgWidth());
		stmt.setInt(6, message.getImgHeight());
		stmt.setInt(7, message.getThumbWidth());
		stmt.setInt(8, message.getThumbHeight());
		stmt.setString(9, message.getThumbLink());

    }
    
    public boolean create(Message message, Long repliesToId){
    	if(!create(message)) return false;
    	Tag repliesTo = new Tag("repliesTo");
    	read(repliesTo);
    	if(repliesToId!=null)
    		return relate(message.getId(), repliesTo, repliesToId);
    	return true;
    }
	public boolean relate(long from, Tag relation, long to) {
		String relationStatement = "insert into message_relations(message_source_id, tag_id, message_sink_id) values (?,?,?)";
		try {
			PreparedStatement stmt = connection.prepareStatement(relationStatement);
			stmt.setLong(1, from);
			stmt.setLong(2, relation.getId());
			stmt.setLong(3, to);
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block, this is particularly important because of unique constraint violations
			e.printStackTrace();
		}
		return false;
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
    
    public Tag tagFor(String tag){
    	Tag tagObj = new Tag(tag);
    	readOrCreate(tagObj);
    	return tagObj;
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
    		PreparedStatement findTagsStmt = connection.prepareStatement(findTags);
    		findTagsStmt.setLong(1, message.getId());
    		rs = findTagsStmt.executeQuery();
    		while(rs.next()){
    			Tag tag = new Tag(rs.getLong("id"), rs.getString("tag"));
    			message.getTags().add(tag);
    		}
    		if(!read(message.getPostedBy())){
    			System.out.println("Message Poster Not Found in Database " + message.getPostedBy());
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
        Tag tags[] = request.getTags().toArray(new Tag[]{});
        List<Message> results = findMessages(tags, request.getLastKnownMessage(), request.getMaximumResults(), !request.getFindAfter());
        return results;
    }
    private String tagSubquery(Tag tag){
		String sql = "exists (select 1 from message_tags where messages.id = message_id and tag_id="+tag.getId();
		sql = sql+")";
		return sql;
	}

    private List<Message> findMessages(Tag[] tags, Message lastKnownMessage, Integer limit, boolean reverse) {//TODO think about callback form or streaming, something not in RAM
		String idOp = "<";
		String sortOp = "DESC";
		if(reverse){
			idOp = ">";
			sortOp = "ASC";
		}
		String sql = "";
		if(tags.length==0){
			sql = "SELECT messages.id* from messages";
			if(lastKnownMessage !=null){
				sql+=" where id"+idOp+lastKnownMessage.getId();
			}
		}else{
			sql = "SELECT messages.* from messages, message_tags where message_tags.message_id = messages.id AND tag_id = "+tags[0].getId();
			for(int i=1; i<tags.length;i++){
				sql = sql + " AND " + tagSubquery(tags[i]);
			}
		}
		if(lastKnownMessage!=null){
			sql+=" AND id"+idOp+lastKnownMessage.getId();//TODO more sophisticated query for different ordering etc
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
			    Message msg = new Message();
			    msg.setId(msgId);
			    read(msg);
				messages.add(msg);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return messages;		
		
	}


    public ThreadResponse findThread(ThreadRequest request){
    	
    	ThreadResponse response = new ThreadResponse();
    	response.setBaseMessage(request.getBaseMessage());
    	if(!read(request.getBaseMessage())){
    		response.setBaseMessage(null);
    		return response;
    	}
    	if(!read(request.getTransitivePredicate())){
    		return response;//the relationship doesn't exist, so it can't have any theads
    	}
    	
    	Map<Long, Message> messageIdentity = new HashMap<Long, Message>();
    	messageIdentity.put(request.getBaseMessage().getId(), request.getBaseMessage());
    	loadTransitiveThread(request, response, messageIdentity, null, 0);
		 
		return response;
	}
	
	
	private Message findOrCreate(Map<Long, Message> messageIdentity, Long id){
		Message msg = messageIdentity.get(id);
		if(msg==null){
			msg = new Message(id);
			if(!read(msg)){
				System.out.println("Found relationship to a message we don't know about?");
			}
			messageIdentity.put(id, msg);
		}
		return msg;
	}
	private void loadTransitiveThread(ThreadRequest request, ThreadResponse response, Map<Long, Message> messageIdentity, List<Long> currentLevel, int level) {
		if(level==0){
			currentLevel = new LinkedList<Long>();
			currentLevel.add(request.getBaseMessage().getId());
		}
		if(currentLevel.size()==0 || level > request.getMaxDepth()) return;
		String sql = "SELECT message_relations.* from message_relations where";
		sql = sql+" tag_id ="+request.getTransitivePredicate().getId();
		
		String searchWhat = " AND message_sink_id ";//if we're searching the source, we're fixing the sink
		if(request.getSearchSink()){//if we're searching the sink, we're fixing the source
			searchWhat = " AND message_source_id ";
		}
		
		sql = sql + searchWhat + buildOrList(currentLevel);
		currentLevel = new LinkedList<>();//dupes in here?
		System.out.println("Finding Related with \n" + sql);
		try {
			Statement stmt = connection.createStatement();
			ResultSet matching = stmt.executeQuery(sql);
			while(matching.next()){
				long source = matching.getLong("message_source_id");
				long sink = matching.getLong("message_sink_id");
				long tag = matching.getLong("tag_id");
				//relations.put(source, sink);//TODO what is the relationshi betwee this map and the source,sink table? Think more of this.
				long relatedId = source;
				long relatingId = sink;
				if(request.getSearchSink()){
					relatedId = sink;
					relatingId = source;
				}
				System.out.println("Considering " + relatingId + " , " + relatedId);
				Message relatingMessage = findOrCreate(messageIdentity, relatingId);
				Message relatedMessage = findOrCreate(messageIdentity, relatedId);
				response.getMessageRelationships().put(relatedMessage, relatingMessage);
				currentLevel.add(relatedId);
				response.getRelated().add(relatedMessage);
			}
		    	loadTransitiveThread(request, response, messageIdentity, currentLevel, level+1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	
    
    public boolean read(User user){
		String findUser = "select * from users where id=?";
		try{
			PreparedStatement stmt = connection.prepareStatement(findUser);
			stmt.setLong(1, user.getId());
			ResultSet rs = stmt.executeQuery();
			if(rs!=null && rs.next()){
				user.setId(rs.getLong("id"));
				user.setEmail(rs.getString("email"));
				user.setPasshash(rs.getString("passhash"));
				user.setUserName(rs.getString("username"));
				return true;
			}
		}catch(SQLException sqe){
			sqe.printStackTrace();
		}
        return false;//TODO need to think about user validation setup
    }
    public boolean create(User user){
        return false;
    }
    
    public boolean resetCorpus(){
    	try{
		Statement stmt = connection.createStatement();
    		execClasspathFile(initializeSQLBase + "clear.sql", stmt);
    		execClasspathFile(initializeSQLBase + "tables.sql", stmt);
    		execClasspathFile(initializeSQLBase + "indices.sql", stmt);
    		execClasspathFile(initializeSQLBase + "seed.sql", stmt);
		stmt.close();
		return true;
    	}catch(IOException ioe){
    		ioe.printStackTrace();
    	}catch(SQLException sqe){
		sqe.printStackTrace();
	}
    	return false;
    }
	private void execClasspathFile(String fname, Statement stmt) throws IOException, SQLException{
		System.out.println("Executing " + fname);
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(fname).getFile());
		Scanner scanner = new Scanner(file);
		String currentStatement = "";
		while(scanner.hasNextLine()){
			String line = scanner.nextLine();
			currentStatement = currentStatement + line.trim();
			if(line.contains(";")){
				currentStatement = currentStatement.trim();
				System.out.println(currentStatement);
				stmt.execute(currentStatement);
				currentStatement = "";
			}
		}
		scanner.close();
		
	}   
    
	
	
	//TODO this stuff is being phased out, we're going to merge it in here to be able to kill extraneous files
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
				Message message =new Message(newId);
				read(message);
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
				Message message = new Message(msgId);
				read(message);
				messages.add(message);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return messages;		
		
	}

}
