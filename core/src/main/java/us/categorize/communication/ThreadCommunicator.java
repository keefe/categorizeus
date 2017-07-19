package us.categorize.communication;

import java.io.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import us.categorize.communication.query.ThreadCriteria;
import us.categorize.model.*;
import us.categorize.repository.*;

public class ThreadCommunicator {
	private MessageRepository messageRepository;
	private Corpus corpus;
	public ThreadCommunicator(Corpus corpus, MessageRepository messageRepository) {
		this.corpus = corpus;
		this.messageRepository = messageRepository;
	}
	
	public void queryStreams(InputStream input, OutputStream output) throws Exception{
		ThreadCriteria criteria = readThreadCriteria(input);
		MessageThread thread = messageRepository.loadThread(criteria);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(output, thread);
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
						tags[i++] = corpus.tagFor(aTag.asText());						
					}else if(aTag.isObject() && aTag.has("tag")){
						tags[i++] = corpus.tagFor(aTag.get("tag").asText());
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
