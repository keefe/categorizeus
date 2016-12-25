package us.categorize.communication.streams;

import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.communication.creation.CategoricalAssertion;
import us.categorize.model.Tag;
import us.categorize.repository.TagRepository;

public class TagStreamReader {
	private TagRepository tagRepository;
	public TagStreamReader(TagRepository tagRepository){
		this.tagRepository = tagRepository;
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
			Tag tagObjs[] = tagRepository.tagsFor(tags);
			tagStuff.setMessageIds(messages);
			tagStuff.setTags(tagObjs);
		}else{
			tagStuff = null;
		}
		return tagStuff;
	}
}
