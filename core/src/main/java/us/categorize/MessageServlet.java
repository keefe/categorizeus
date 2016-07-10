/**
 * 
 */
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
import us.categorize.model.User;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.UserRepository;
import us.categorize.util.ServletUtil;

/**
 * @author keefe
 *
 */
public class MessageServlet extends HttpServlet {
	
	private MessageRepository messageRepository; 
	private UserRepository userRepository;
	
	public MessageServlet(MessageRepository repository, UserRepository userRepository){
		super();
		this.messageRepository = repository;
		this.userRepository = userRepository;
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
		try {
			User user = userRepository.find(1);//#TODO add authentication, replace this hard coded value
			Message message = new Message();
			message.setBody(messageBody);
			message.setTitle(messageTitle);
			message.setPostedBy(user);
			messageRepository.addMessage(message);
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
