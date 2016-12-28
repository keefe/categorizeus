package us.categorize.communication;

import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.communication.query.ThreadCriteria;
import us.categorize.communication.streams.ThreadStreamReader;
import us.categorize.model.MessageThread;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.TagRepository;

public class ThreadCommunicator {
	private TagRepository tagRepository;
	private MessageRepository messageRepository;
	private ThreadStreamReader threadReader;
	public ThreadCommunicator(TagRepository tagRepository, MessageRepository messageRepository) {
		this.tagRepository = tagRepository;
		this.messageRepository = messageRepository;
		threadReader = new ThreadStreamReader(tagRepository, messageRepository);
	}
	
	public void queryStreams(InputStream input, OutputStream output) throws Exception{
		ThreadCriteria criteria = threadReader.readThreadCriteria(input);
		MessageThread thread = messageRepository.loadThread(criteria);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(output, thread);
	}
	
}
