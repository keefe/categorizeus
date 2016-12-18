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

import us.categorize.communication.creation.MessageAssertion;
import us.categorize.communication.streams.MessageStreamReader;
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
		MessageStreamReader messageStreamReader = new MessageStreamReader();
		try {
			MessageAssertion assertion = messageStreamReader.readMessageAssertion(request.getInputStream());
			User user = (User) request.getSession().getAttribute("user");
			assertion.getMessage().setPostedBy(user);

			messageRepository.addMessage(assertion.getMessage());
			
			if(assertion.getRelationships().containsKey("repliesToId")){
				MessageRelation relation = new MessageRelation();
				relation.setSource(assertion.getMessage());
				relation.setRelation(tagRepository.tagFor("repliesTo"));
				Message fauxReplySource = new Message();
				fauxReplySource.setId(Long.parseLong(assertion.getRelationships().get("repliesToId")));
				relation.setSink(fauxReplySource);
				messageRepository.relate(relation);
			}else{
				assertion.getTags().add("top");
			}
			String[] tags = assertion.getTags().toArray(new String[]{});
			Tag tagObjs[] = tagRepository.tagsFor(tags);
			messageRepository.tag(assertion.getMessage(), tagObjs);
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().println(assertion.getMessage().getId());//#TODO replace this with json structure
	        response.getWriter().close();
	        return;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("Could not add new message for some reason");
        response.getWriter().close();
    }
}
