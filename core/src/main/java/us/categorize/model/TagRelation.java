package us.categorize.model;

public class TagRelation {
	private Tag tag;
	private double rating;//TODO what am I going towards here, storing feature vectors?
	private Message message;
	public Tag getTag() {
		return tag;
	}
	public void setTag(Tag tag) {
		this.tag = tag;
	}
	public double getRating() {
		return rating;
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
	}
	
}
