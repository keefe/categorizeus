package us.categorize.model;

public class ThreadCriteria {
	private Tag[] searchTags; //TODO is it a good idea to introduce a tag categorizeus:id for each top level post?
	private Tag[] transitiveTags;
	private int maxResults; //max results for the top level search
	private int maxTransitiveDepth; //number of links to follow, e.g. replies, replies of replies, so forth
	private int maxTransitiveResults; //total number to load for each predicate, so max number of replies to a member
	
}
