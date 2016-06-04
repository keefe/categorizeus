package us.categorize.model;

import java.util.LinkedList;
import java.util.List;

public class MessageThread {
	private Message root;
	private List<Message> thread = new LinkedList<>();
	public Message getRoot() {
		return root;
	}
	public void setRoot(Message root) {
		this.root = root;
	}
	public List<Message> getThread() {
		return thread;
	}
	public void setThread(List<Message> thread) {
		this.thread = thread;
	}
}
