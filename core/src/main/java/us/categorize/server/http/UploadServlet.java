package us.categorize.server.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;

import us.categorize.repository.MessageRepository;
import us.categorize.repository.TagRepository;

public class UploadServlet extends HttpServlet{
	
	private MessageRepository messageRepository;
	private TagRepository tagRepository;
	private MultipartHandler multipartHandler;
	
	
	public UploadServlet(MessageRepository messageRepository, TagRepository tagRepository, MultipartHandler multipartHandler) {
		super();
		this.messageRepository = messageRepository;
		this.tagRepository = tagRepository;
		this.multipartHandler = multipartHandler;
	}


	@Override
	public void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		try {
			if(!multipartHandler.handle(request)){//should we be returning an id here or just putting it at the top of user queue
		        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		        response.getWriter().println("No Multipart Data");//#TODO replace this with json structure
		        response.getWriter().close();
			}else{
		        response.setStatus(HttpServletResponse.SC_OK);
		        response.getWriter().println("OK");//#TODO replace this with json structure
		        response.getWriter().close();
			}
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        e.printStackTrace(response.getWriter());
	        response.getWriter().close();
		}
    }
}
