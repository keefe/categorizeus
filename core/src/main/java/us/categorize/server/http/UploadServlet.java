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
	private long maxUploadSize;

	
	
	public UploadServlet(long maxUploadSize, MessageRepository messageRepository, TagRepository tagRepository, MultipartHandler multipartHandler) {
		super();
		this.messageRepository = messageRepository;
		this.tagRepository = tagRepository;
		this.multipartHandler = multipartHandler;
		this.maxUploadSize = maxUploadSize;

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
		
		String contentType = request.getHeader("Content-Type");
		System.out.println("Content Type Comes through as " + contentType);
		//TODO externalize this to a list of acceptable types

		

		try {
			if(!multipartHandler.handle(request)){//should we be returning an id here or just putting it at the top of user queue
		        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		        response.getWriter().println("No or Bad Multipart Data");//#TODO replace this with json structure
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
