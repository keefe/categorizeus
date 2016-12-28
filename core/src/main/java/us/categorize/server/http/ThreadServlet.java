package us.categorize.server.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.categorize.communication.ThreadCommunicator;

public class ThreadServlet extends HttpServlet {
	private ThreadCommunicator communicator;
	
	public ThreadServlet(ThreadCommunicator comms) {
		communicator = comms;
	}
	@Override
	public void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		try{
	        response.setContentType("application/json");
			communicator.queryStreams(request.getInputStream(), response.getOutputStream());			
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getOutputStream().flush();
	        response.getOutputStream().close();
	        return;
		}catch(Exception e){
			e.printStackTrace();
		}
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("error");
        response.getWriter().close();	
    }
}
