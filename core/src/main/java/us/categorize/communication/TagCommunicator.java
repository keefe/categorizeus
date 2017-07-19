package us.categorize.communication;

import java.io.InputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import us.categorize.communication.creation.CategoricalAssertion;
import us.categorize.repository.*;
import us.categorize.model.*;
import java.util.*;

public class TagCommunicator {
	private Corpus corpus;
	public TagCommunicator(Corpus corpus) {
		this.corpus = corpus;
	}
	public CategoricalAssertion readTagRequest(InputStream input) throws Exception{
		CategoricalAssertion tagStuff = new CategoricalAssertion();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode bodyObj = mapper.readTree(input);
		JsonNode tagNode = bodyObj.get("tags");
		String tags[] = null;		
		if(tagNode.isArray()){
			tags = new String[tagNode.size()];
			int i =0;
			for(JsonNode aTag : tagNode){
				tags[i++] = aTag.asText();
			}
		}
		JsonNode messagesNode = bodyObj.get("messages");
		long messages[] = null;
		if(messagesNode.isArray()){
			messages = new long[messagesNode.size()];
			int i = 0;
			for(JsonNode aMessage : messagesNode){
				messages[i++] = Long.parseLong(aMessage.asText());
			}
		}
		if(messages!=null && tags!=null){
			Tag tagObjs[] = new Tag[tags.length];
			for(int i=0; i<tagObjs.length;i++){
				tagObjs[i] = corpus.tagFor(tags[i]);
			}
			tagStuff.setMessageIds(messages);
			tagStuff.setTags(tagObjs);
		}else{
			tagStuff = null;
		}
		return tagStuff;
	}
	
	public void categorizeMessages(InputStream input) throws Exception{
		CategoricalAssertion tagWhat = readTagRequest(input);
		if(tagWhat==null){
			throw new Exception("[NotFound] No Valid Tagging Specified");
		}
		//TODO temporary, moving to new structure
		LinkedList<Tag> tags = new LinkedList<Tag>();
		for(Tag t : tagWhat.getTags()){
			tags.add(t);
		}
		for(long id : tagWhat.getMessageIds()){
			corpus.tagMessage(new Message(id), tags);
		}
	}
}
