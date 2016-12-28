package us.categorize.communication.streams;

import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.communication.creation.MessageAssertion;
import us.categorize.communication.creation.MessageAssertionAttachment;
import us.categorize.model.Message;

public class MessageStreamReader {
	public Message readMessage(InputStream stream) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode bodyObj = mapper.readTree(stream);		
		return readMessage(bodyObj);
	}
	
	public MessageAssertion readMessageAssertion(InputStream stream) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode bodyObj = mapper.readTree(stream);
		MessageAssertion assertion = new MessageAssertion();
		assertion.setMessage(readMessage(bodyObj));
		String tagString = bodyObj.get("tags").asText();
		if(tagString!=null && tagString.length()>0){
			String tagArray[] = tagString.split(" ");
			for(String tag : tagArray)
				assertion.getTags().add(tag);
		}
		//TODO change this payload to have a generic relationships object
		if(bodyObj.has("repliesToId")){
			assertion.getRelationships().put("repliesToId",  bodyObj.get("repliesToId").asText());
		}
		
		if(bodyObj.has("attachment")){
			MessageAssertionAttachment attachment = new MessageAssertionAttachment();
			JsonNode attachmentNode = bodyObj.get("attachment");
			attachment.setDataURL(attachmentNode.get("dataURL").asText());
			attachment.setName(attachmentNode.get("name").asText());
			attachment.setType(attachmentNode.get("type").asText());
			attachment.setSize(attachmentNode.get("size").asText());
			assertion.setAttachment(attachment);
		}
		return assertion;
	}
	
	
	private Message readMessage(JsonNode bodyObj){
		Message message = new Message();
		message.setBody(bodyObj.get("body").asText());
		message.setTitle(bodyObj.get("title").asText());
		
		return message;
	}
	
	
}
