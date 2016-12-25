package us.categorize.communication;

import java.io.InputStream;

import us.categorize.communication.creation.CategoricalAssertion;
import us.categorize.communication.streams.TagStreamReader;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.TagRepository;

public class TagCommunicator {
	private TagRepository tagRepository;
	private MessageRepository messageRepository;
	private TagStreamReader tagStreamReader;
	public TagCommunicator(TagRepository tagRepository, MessageRepository messageRepository) {
		this.tagRepository = tagRepository;
		this.messageRepository = messageRepository;
		tagStreamReader = new TagStreamReader(tagRepository);
	}
	
	
	public void categorizeMessages(InputStream input) throws Exception{
		CategoricalAssertion tagWhat = tagStreamReader.readTagRequest(input);
		if(tagWhat==null){
			throw new Exception("[NotFound] No Valid Tagging Specified");
		}
		
		messageRepository.tag(tagWhat.getMessageIds(), tagWhat.getTags());
	}
}
