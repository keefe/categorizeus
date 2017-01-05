package us.categorize.server.http.legacy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.categorize.communication.TagCommunicator;

@SuppressWarnings("serial")
public class TagServlet extends HttpServlet {
	private TagCommunicator tagCommunicator;
	
	public TagServlet(TagCommunicator tagCommunicator){
		this.tagCommunicator = tagCommunicator;
	}

	@Override
	public void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		doPut(request, response);//bad form but whatever
    }
	
	@Override
	public void doPut( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {//PUT is probably not a good verb here as there is no ID in the URI
		try {
	        response.setContentType("application/json");
			tagCommunicator.categorizeMessages(request.getInputStream());
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().println("{\"response\":\"OK\"}");
	        response.getWriter().close();
	        return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("error");
        response.getWriter().close();		
    }
}
