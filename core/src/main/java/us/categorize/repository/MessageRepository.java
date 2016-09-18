package us.categorize.repository;

import java.util.List;

import us.categorize.model.Message;
import us.categorize.model.MessageThread;
import us.categorize.model.Tag;
import us.categorize.model.ThreadCriteria;

//#TODO pagination, using continuation token style
//#TODO add support for saving, deleting messages, even if not exposed at the web layer
public interface MessageRepository {
	Message getMessage(long id) throws Exception;
	boolean addMessage(Message message);//are messages immutable?
	boolean updateMessage(Message message);
	List<Message> findMessages(Tag[] tags);
	MessageThread loadThread(ThreadCriteria criteria);
	boolean tag(Message message, Tag[] tags);
	boolean tag(long messageIds[], Tag[] tags);
}
