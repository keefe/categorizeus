/**
 * 
 */
package us.categorize.server.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.model.Message;
import us.categorize.model.MessageRelation;
import us.categorize.model.Tag;
import us.categorize.model.User;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.TagRepository;
import us.categorize.repository.UserRepository;
import us.categorize.util.ServletUtil;

/**
 * @author keefe
 *
 */
public class MessageServlet extends HttpServlet {
	
	private MessageRepository messageRepository; 
	private UserRepository userRepository;
	private TagRepository tagRepository;
	
	public MessageServlet(MessageRepository repository, UserRepository userRepository,TagRepository tagRepository){
		super();
		this.messageRepository = repository;
		this.userRepository = userRepository;
		this.tagRepository = tagRepository;
	}
	
	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
		System.out.println("Request made to " + request.getPathInfo());
		System.out.println("Session Check " + request.getSession().getAttribute("testToken"));

		String path = request.getPathInfo();
		if(path!=null && path.length()>0){
			try {
				Long id = Long.parseLong(path.replace("/", ""));
				Message message = messageRepository.getMessage(id);
				if(message!=null){
					ObjectMapper mapper = new ObjectMapper();
					String jsonMessage = mapper.writeValueAsString(message);
			        response.setContentType("application/json");
			        response.setStatus(HttpServletResponse.SC_OK);
			        response.getWriter().println(jsonMessage);
			        response.getWriter().close();
			        return;
				}
		        
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().println("Not Found");
        response.getWriter().close();
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
		JsonNode bodyObj = ServletUtil.readyBody(request);
		System.out.println("Session Check " + request.getSession().getAttribute("testToken"));
		String messageBody = bodyObj.get("body").asText();
		String messageTitle = bodyObj.get("title").asText();
		String messageTags = bodyObj.get("tags").asText();
		String repliesToId = null;
		if(bodyObj.has("repliesToId")){
			repliesToId = bodyObj.get("repliesToId").asText();
		}
		System.out.println("Message is replying to " + repliesToId);
		String tagArray[] = messageTags.split(" ");
		try {
			User user = (User) request.getSession().getAttribute("user");
			Message message = new Message();
			message.setBody(messageBody);
			message.setTitle(messageTitle);
			message.setPostedBy(user);
			messageRepository.addMessage(message);
			if(tagArray.length>0){
				Tag tags[] = tagRepository.tagsFor(tagArray);
				messageRepository.tag(message, tags);
			}
			if(repliesToId!=null){
				MessageRelation relation = new MessageRelation();
				relation.setSource(message);
				relation.setRelation(tagRepository.tagFor("repliesTo"));
				Message fauxReplySource = new Message();
				fauxReplySource.setId(Long.parseLong(repliesToId));
				relation.setSink(fauxReplySource);
				messageRepository.relate(relation);
			}else{
				messageRepository.tag(message, new Tag[]{tagRepository.tagFor("top")}); //TODO ugh, more refactoring				
			}
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().println(message.getId());//#TODO replace this with json structure
	        response.getWriter().close();
	        return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("Could not add new message for some reason");
        response.getWriter().close();
    }
}
