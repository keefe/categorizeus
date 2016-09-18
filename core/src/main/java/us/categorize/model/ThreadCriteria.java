package us.categorize.model;

public class ThreadCriteria {
	private Tag[] searchTags; //TODO is it a good idea to introduce a tag categorizeus:id for each top level post?
	private Tag[] transitiveTags;
	private int maxResults = 10; //max results for the top level search
	private int maxTransitiveDepth = 3; //number of links to follow, e.g. replies, replies of replies, so forth
	private int maxTransitiveResults = 5; //total number to load for each predicate, so max number of replies to a member
	public Tag[] getSearchTags() {
		return searchTags;
	}
	public void setSearchTags(Tag[] searchTags) {
		this.searchTags = searchTags;
	}
	public Tag[] getTransitiveTags() {
		return transitiveTags;
	}
	public void setTransitiveTags(Tag[] transitiveTags) {
		this.transitiveTags = transitiveTags;
	}
	public int getMaxResults() {
		return maxResults;
	}
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
	public int getMaxTransitiveDepth() {
		return maxTransitiveDepth;
	}
	public void setMaxTransitiveDepth(int maxTransitiveDepth) {
		this.maxTransitiveDepth = maxTransitiveDepth;
	}
	public int getMaxTransitiveResults() {
		return maxTransitiveResults;
	}
	public void setMaxTransitiveResults(int maxTransitiveResults) {
		this.maxTransitiveResults = maxTransitiveResults;
	}
	
	
}
