package us.categorize.server.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.model.MessageThread;
import us.categorize.model.Tag;
import us.categorize.model.ThreadCriteria;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.TagRepository;
import us.categorize.util.ServletUtil;

public class ThreadServlet extends HttpServlet {
	
	public ThreadServlet(TagRepository tagRepository, MessageRepository messageRepository) {
		this.tagRepository = tagRepository;
		this.messageRepository = messageRepository;
	}

	private TagRepository tagRepository;
	private MessageRepository messageRepository;
	
	@Override
	public void doPut( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		
    }
	
	//TODO remove 
	
	
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
	@Override
	public void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		JsonNode bodyObj = ServletUtil.readyBody(request);
		ThreadCriteria criteria = new ThreadCriteria();
		ObjectMapper mapper = new ObjectMapper(); //how do I optionally load maxResults, just check for presence or...
		criteria.setSearchTags(tagsFromJson(bodyObj.get("searchTags")));
		criteria.setTransitiveTags(tagsFromJson(bodyObj.get("transitiveTags")));
		if(bodyObj.has("startingId") && bodyObj.get("startingId").isInt()){
			criteria.setStartingId(bodyObj.get("startingId").asInt());//TODO validation
		}
		if(bodyObj.has("reverse")){
			criteria.setReverse(bodyObj.get("reverse").asBoolean());
		}
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        MessageThread thread = messageRepository.loadThread(criteria);
		String messageString = mapper.writeValueAsString(thread);
        response.getWriter().println(messageString);
        response.getWriter().close();
    }
	
	@Override
	public void doGet( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		
    }
}
