package us.categorize.model;

import java.util.LinkedList;
import java.util.List;

public class MessageThread {
	private List<Message> thread = new LinkedList<>();
	private ThreadCriteria searchCriteria;
	private List<MessageRelation> relations;
	
	public List<Message> getThread() {
		return thread;
	}
	public void setThread(List<Message> thread) {
		this.thread = thread;
	}
}
