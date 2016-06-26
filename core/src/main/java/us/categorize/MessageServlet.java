/**
 * 
 */
package us.categorize;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.model.Message;
import us.categorize.repository.MessageRepository;

/**
 * @author keefe
 *
 */
public class MessageServlet extends HttpServlet {
	
	private MessageRepository messageRepository; 
	
	public MessageServlet(MessageRepository repository){
		super();
		this.messageRepository = repository;
	}
	
	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
		System.out.println("Request made to " + request.getPathInfo());
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
		
    }
}
