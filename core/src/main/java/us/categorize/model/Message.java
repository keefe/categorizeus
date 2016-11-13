package us.categorize.model;

public class Message {
	private String title;
	private String body;
	private User postedBy;
	private long id;
	private String link, thumbLink;//TODO what belongs in this class, what belongs in a relationship?
	public String getThumbLink() {
		return thumbLink;
	}

	public void setThumbLink(String thumbLink) {
		this.thumbLink = thumbLink;
	}

	private int imgWidth, imgHeight, thumbWidth, thumbHeight;

	
	public int getImgWidth() {
		return imgWidth;
	}

	public void setImgWidth(int imgWidth) {
		this.imgWidth = imgWidth;
	}

	public int getImgHeight() {
		return imgHeight;
	}

	public void setImgHeight(int imgHeight) {
		this.imgHeight = imgHeight;
	}

	public int getThumbWidth() {
		return thumbWidth;
	}

	public void setThumbWidth(int thumbWidth) {
		this.thumbWidth = thumbWidth;
	}

	public int getThumbHeight() {
		return thumbHeight;
	}

	public void setThumbHeight(int thumbHeight) {
		this.thumbHeight = thumbHeight;
	}

	public String toString(){
		return id+":"+title+":"+body+":"+postedBy;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
}
