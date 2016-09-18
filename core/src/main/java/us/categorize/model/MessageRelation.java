package us.categorize.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class MessageRelation {
	public MessageRelation() {
	}
	public MessageRelation(Message source, Tag relation, Message sink) {
		this.source = source;
		this.relation = relation;
		this.sink = sink;
	}
	@JsonIgnoreProperties({"title","body","postedBy","link"})
	private Message source;
	
	@JsonIgnoreProperties({"id"})
	private Tag relation;
	
	@JsonIgnoreProperties({"title","body","postedBy","link"})
	private Message sink;
	
	public Message getSource() {
		return source;
	}
	public void setSource(Message source) {
		this.source = source;
	}
	public Tag getRelation() {
		return relation;
	}
	public void setRelation(Tag relation) {
		this.relation = relation;
	}
	public Message getSink() {
		return sink;
	}
	public void setSink(Message sink) {
		this.sink = sink;
	}
	
	
}
