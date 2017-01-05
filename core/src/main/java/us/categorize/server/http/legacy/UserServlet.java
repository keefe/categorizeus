package us.categorize.server.http.legacy;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.categorize.communication.UserCommunicator;
import us.categorize.model.User;

public class UserServlet extends HttpServlet {

	private UserCommunicator communicator;
	
	public UserServlet(UserCommunicator communicator){
		this.communicator = communicator;
	}
	
	public void doGet( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		User user = (User) request.getSession().getAttribute("user");
		if(user==null){
	        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        response.getWriter().println("Not Found");
	        response.getWriter().close();
	        return;
		}
		try {
			response.setContentType("application/json");
			communicator.writeUser(user, response.getOutputStream());
	        response.setStatus(HttpServletResponse.SC_OK);
	        return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("error");
        response.getWriter().close();
    }
	
	public void doDelete( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		User user = (User) request.getSession().getAttribute("user");
		if(user==null){
	        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        response.getWriter().println("Not Found");
	        response.getWriter().close();
	        return;
		}
		String categorizeusCookieString = (String) request.getSession().getAttribute("categorizeus");
		Cookie categorizeusCookie = null;
		for(Cookie cookie : request.getCookies()){
			if(cookie.getName().equals("categorizeus")){
				categorizeusCookie = cookie;
				break;
			}
		}
		if(categorizeusCookie!=null){//TODO this doesn't appear to be actually removing the cookie, look into this further
			categorizeusCookie.setMaxAge(0);
			response.addCookie(categorizeusCookie);
		}
		request.getSession().removeAttribute("categorizeus");
		request.getSession().removeAttribute("user");
		try {
	        response.setContentType("application/json");
			communicator.logoutUser(user, categorizeusCookieString, response.getOutputStream());
	        response.setStatus(HttpServletResponse.SC_OK);
	        return;
		} catch (Exception e) {
			e.printStackTrace();
		}
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("error");
        response.getWriter().close();
    }
	
	public void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		try {
			String categorizeusUUID = (String) request.getSession().getAttribute("categorizeus");
	        response.setContentType("application/json");
			User user = communicator.loginUser(request.getInputStream(), response.getOutputStream(), categorizeusUUID);
			if(user!=null){
				request.getSession().setAttribute("user", user);
				response.setStatus(HttpServletResponse.SC_OK);
			}
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().println("Invalid User");
		response.getWriter().close();
		
    }
	
	public void doPut( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {//TODO this is not really a PUT in the REST sense
		User currentUser = (User) request.getSession().getAttribute("user");
		try {
	        response.setContentType("application/json");
			communicator.registerUser(currentUser, request.getInputStream(), response.getOutputStream());
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().println("Invalid User");
		response.getWriter().close();
    }
}
