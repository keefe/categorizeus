package us.categorize.communication.creation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import us.categorize.model.Message;

public class MessageAssertion {
	private Message message;
	
	//for now this is implying 1:1 relationship, in the future this may change but for simplicity leave it
	private Map<String, String> relationships = new HashMap<String, String>();
	private List<String> tags = new LinkedList<String>();
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
	}

	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public Map<String, String> getRelationships() {
		return relationships;
	}
	public void setRelationships(Map<String, String> relationships) {
		this.relationships = relationships;
	}
	
	
}
