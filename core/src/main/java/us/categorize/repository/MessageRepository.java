package us.categorize.repository;

import java.util.List;

import us.categorize.model.Message;
import us.categorize.model.MessageThread;
import us.categorize.model.Tag;

//#TODO pagination, using continuation token style
public interface MessageRepository {
	Message getMessage(long id) throws Exception;
	boolean addMessage(Message message);//are messages immutable?
	boolean postMessage(String body);//#TODO figure this out, for the post stuff
	MessageThread getThread(String id);
	List<Message> findMessages(Tag[] tags);
	List<Message> findThreads(List<Tag> tags);
	boolean tag(Message message, Tag[] tags);
	boolean tag(long messageIds[], Tag[] tags);
	
}
