package us.categorize.communication.creation;

import us.categorize.model.Tag;

public class CategoricalAssertion {
	private long messageIds[];
	private Tag tags[];
	public long[] getMessageIds() {
		return messageIds;
	}
	public void setMessageIds(long[] messageIds) {
		this.messageIds = messageIds;
	}
	public Tag[] getTags() {
		return tags;
	}
	public void setTags(Tag[] tags) {
		this.tags = tags;
	}
	
	
}
