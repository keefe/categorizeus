/**
 * 
 */
package us.categorize.server.http.legacy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.categorize.communication.MessageCommunicator;
import us.categorize.communication.creation.MessageAssertion;
import us.categorize.communication.creation.attachment.AttachmentHandler;
import us.categorize.model.User;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.TagRepository;
import us.categorize.repository.UserRepository;

/**
 * @author keefe
 *
 */
public class MessageServlet extends HttpServlet {
	
	private MessageCommunicator communicator;
	private double maxUploadSize;

	public MessageServlet(MessageCommunicator communicator, double maxUploadSize){
		super();
		this.communicator = communicator;
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
		        response.setContentType("application/json");
				communicator.readMessage(id, response.getOutputStream());
		        response.setStatus(HttpServletResponse.SC_OK);
		        response.getOutputStream().flush();
		        response.getOutputStream().close();
		        return;		        
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
	        response.setContentType("application/json");
			MessageAssertion messageAssertion = communicator.createMessageFromStream(request.getInputStream());
	        response.setStatus(HttpServletResponse.SC_OK);
	        String prototypeJson = "{\"id\":\"IDVALUE\"}";
	        response.getWriter().println(prototypeJson.replace("IDVALUE", messageAssertion.getMessage().getId()+""));
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
