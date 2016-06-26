package us.categorize;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.categorize.repository.MessageRepository;
import us.categorize.repository.TagRepository;

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
    {
		
    }
	
	@Override
	public void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		
    }
}
