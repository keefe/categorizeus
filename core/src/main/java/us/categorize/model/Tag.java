package us.categorize.model;

public class Tag extends Identifiable{
	private String tag; 
	public Tag(){
		
	}
	public Tag(String t){
		tag = t;
	}
	public String toString(){
		return "Tag:: " + getId() + " is " + tag;
	}
	public Tag(Long id, String tag){
		super.setId(id);
		this.tag = tag;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	
}
