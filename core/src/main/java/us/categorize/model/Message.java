package us.categorize.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message {
	private Map<Tag, List<Message>> relatedMessages = new HashMap<>();
	private String title;
	private String body;
	private User postedBy;
	private long id;
	
	public String toString(){
		return id+":"+title+":"+body+":"+postedBy;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Map<String, List<Message>> getRelatedMessages() {
		return relatedMessages;
	}
	public void setRelatedMessages(Map<String, List<Message>> relatedMessages) {
		this.relatedMessages = relatedMessages;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public User getPostedBy() {
		return postedBy;
	}
	public void setPostedBy(User postedBy) {
		this.postedBy = postedBy;
	}
}
