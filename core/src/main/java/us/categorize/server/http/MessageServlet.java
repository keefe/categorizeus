/**
 * 
 */
package us.categorize.server.http;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.communication.MessageCommunicator;
import us.categorize.communication.creation.MessageAssertion;
import us.categorize.communication.creation.MessageAssertionAttachment;
import us.categorize.communication.creation.attachment.AttachmentHandler;
import us.categorize.communication.streams.MessageStreamReader;
import us.categorize.model.Message;
import us.categorize.model.MessageRelation;
import us.categorize.model.Tag;
import us.categorize.model.User;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.TagRepository;
import us.categorize.repository.UserRepository;

/**
 * @author keefe
 *
 */
public class MessageServlet extends HttpServlet {
	
	private MessageRepository messageRepository; 
	private MessageCommunicator communicator;
	private double maxUploadSize;

	public MessageServlet(MessageRepository repository, UserRepository userRepository,TagRepository tagRepository, AttachmentHandler attachmentHandler, double maxThumbWidth, double maxThumbHeight, double maxUploadSize){
		super();
		this.messageRepository = repository;
		communicator = new MessageCommunicator(messageRepository, tagRepository, attachmentHandler, maxThumbWidth, maxThumbHeight, maxUploadSize);
		this.maxUploadSize = maxUploadSize;

	}
	
	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
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
		boolean validSize = true;
		try{
			long statedSize = Long.parseLong(request.getHeader("Content-Length"));
			if(statedSize>maxUploadSize){
				validSize = false;
			}
		}catch(Exception e){
			validSize = false;
			e.printStackTrace();
		}
		if(!validSize){
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        response.getWriter().println("Upload too Large or Content-Length missing or invalid");//#TODO replace this with json structure
	        response.getWriter().close();
	        return;
		}
		
		try {
			User user = (User) request.getSession().getAttribute("user");
			communicator.setSpeaker(user);//TODO threading issue here I think
			MessageAssertion messageAssertion = communicator.handleMessageStream(request.getInputStream());
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().println(messageAssertion.getMessage().getId());//#TODO replace this with json structure
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
