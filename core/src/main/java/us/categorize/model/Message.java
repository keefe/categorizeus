package us.categorize.model;
import java.util.*;

public class Message extends Identifiable{
	private String title;
	private String body;
	private User postedBy;
	private String link, thumbLink;//TODO what belongs in this class, what belongs in a relationship?
	private List<Tag> tags;
	private int imgWidth, imgHeight, thumbWidth, thumbHeight;

	public String getThumbLink() {
		return thumbLink;
	}
	public List<Tag> getTags(){
		if(tags==null){
			tags = new LinkedList<Tag>();
		}
		return tags;
	}

	public void setThumbLink(String thumbLink) {
		this.thumbLink = thumbLink;
	}


	
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
		return getId()+":"+title+":"+body+":"+postedBy;
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
