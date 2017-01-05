package us.categorize.communication.streams;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.communication.query.ThreadCriteria;
import us.categorize.model.Tag;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.TagRepository;

public class ThreadStreamReader {
	
	private TagRepository tagRepository;
	private MessageRepository messageRepository;
	public ThreadStreamReader(TagRepository tagRepository, MessageRepository messageRepository) {
		this.tagRepository = tagRepository;
		this.messageRepository = messageRepository;
	}
	
	public ThreadCriteria readThreadCriteria(InputStream input) throws JsonProcessingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode bodyObj = mapper.readTree(input);
		mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true); 

		String reString = mapper.writeValueAsString(bodyObj);
		System.out.println("We're checking out " + reString);
		ThreadCriteria criteria = new ThreadCriteria();
		criteria.setSearchTags(tagsFromJson(bodyObj.get("searchTags")));
		criteria.setTransitiveTags(tagsFromJson(bodyObj.get("transitiveTags")));
		if(bodyObj.has("startingId") && bodyObj.get("startingId").isInt()){
			criteria.setStartingId(bodyObj.get("startingId").asInt());//TODO validation
		}
		if(bodyObj.has("reverse")){
			criteria.setReverse(bodyObj.get("reverse").asBoolean());
		}
		
		return criteria;
	}
	
	
	private Tag[] tagsFromJson(JsonNode tagNode){
		if(tagNode.isArray()){
			Tag tags[] = new Tag[tagNode.size()];
			int i =0;//TODO this needs to be a method
			for(JsonNode aTag : tagNode){
				try {
					if(aTag.isTextual()){
						tags[i++] = tagRepository.tagFor(aTag.asText());						
					}else if(aTag.isObject() && aTag.has("tag")){
						tags[i++] = tagRepository.tagFor(aTag.get("tag").asText());
					}else{
						System.out.println("What do I do now? " + aTag.asText());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return tags;
		}
		return new Tag[]{};
	}
}
