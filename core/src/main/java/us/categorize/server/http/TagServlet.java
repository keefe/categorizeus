package us.categorize.server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

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
import us.categorize.util.ServletUtil;

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
    {//PUT is probably not a good verb here as there is no ID in the URI
		JsonNode bodyObj = ServletUtil.readyBody(request);
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
			try {
				Tag tagObjs[] = tagRepository.tagsFor(tags);
				messageRepository.tag(messages, tagObjs);
		        response.setStatus(HttpServletResponse.SC_OK);
		        response.getWriter().println("OK");
		        response.getWriter().close();
		        return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("uhm, we done fucked up");
        response.getWriter().close();		
		
		//some kind of error handling or a 500 or something here
				
    }
	
	@Override
	public void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		JsonNode bodyObj = ServletUtil.readyBody(request);
		JsonNode tagNode = bodyObj.get("tags");
		if(tagNode.isArray()){
			String tags[] = new String[tagNode.size()];
			int i =0;
			for(JsonNode aTag : tagNode){
				tags[i++] = aTag.asText();
			}
			try {
				Tag tagObjs[] = tagRepository.tagsFor(tags);
				List<Message> messages = messageRepository.findMessages(tagObjs);
				ObjectMapper mapper = new ObjectMapper();
				String messageString = mapper.writeValueAsString(messages.toArray(new Message[]{}));
		        response.setContentType("application/json");
		        response.setStatus(HttpServletResponse.SC_OK);
		        response.getWriter().println(messageString);
		        response.getWriter().close();
		        return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("uhm, we done fucked up");
        response.getWriter().close();
		//some kind of error handling or a 500 or something here
		
    }
}
