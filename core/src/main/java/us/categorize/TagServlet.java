package us.categorize;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.model.Message;
import us.categorize.model.Tag;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.TagRepository;

@SuppressWarnings("serial")
public class TagServlet extends HttpServlet {
	
	private MessageRepository messageRepository; 
	private TagRepository tagRepository;
	
	public TagServlet(MessageRepository messageRepository, TagRepository tagRepository){
		this.messageRepository = messageRepository;
		this.tagRepository = tagRepository;
	}

	@Override
	public void doGet( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		
    }
	
	@Override
	public void doPut( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		
    }
	
	@Override
	public void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		//TODO yuck, copy paste
		ObjectMapper mapper = new ObjectMapper();
		String body = "";
		BufferedReader reader = request.getReader();
		String line = null;
		while((line = reader.readLine())!=null){
			body = body + line;
		}
		System.out.println("Request Content Type " + request.getContentType());
		System.out.println("Read Body as " + body);
		JsonNode bodyObj = mapper.readTree(body);
		JsonNode tagNode = bodyObj.get("tags");
		long messageId = bodyObj.get("messageId").asLong();
		if(tagNode.isArray()){
			String tags[] = new String[tagNode.size()];
			int i =0;
			for(JsonNode aTag : tagNode){
				tags[i++] = aTag.asText();
			}
			try {
				Tag tagObjs[] = tagRepository.tagsFor(tags);
				Message message = messageRepository.getMessage(messageId);
				messageRepository.tag(message, tagObjs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//some kind of error handling or a 500 or something here
		
    }
}
