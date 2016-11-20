package us.categorize.server.http;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.model.User;
import us.categorize.repository.UserRepository;
import us.categorize.util.ServletUtil;

public class UserServlet extends HttpServlet {

	private UserRepository userRepository;
	public UserServlet(UserRepository userRepository){
		this.userRepository = userRepository;
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
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonMessage = mapper.writeValueAsString(user);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(jsonMessage);
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
		userRepository.destroySessionUser(categorizeusCookieString);
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
		ObjectMapper mapper = new ObjectMapper();
		String jsonMessage = mapper.writeValueAsString(user);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(jsonMessage);
        response.getWriter().close();	
    }
	
	public void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		JsonNode bodyObj = ServletUtil.readyBody(request);
		String username = bodyObj.get("username").asText();
		String password = DigestUtils.sha256Hex(bodyObj.get("password").asText());
		try {
			User user = userRepository.validateUser(username, password);
			if(user!=null){
				String categorizeusUUID = (String) request.getSession().getAttribute("categorizeus");
				userRepository.createSessionUser(categorizeusUUID, user);
				request.getSession().setAttribute("user", user);
				ObjectMapper mapper = new ObjectMapper();
				String jsonMessage = mapper.writeValueAsString(user);
		        response.setContentType("application/json");
				response.setStatus(HttpServletResponse.SC_OK);
		        response.getWriter().println(jsonMessage);
		        response.getWriter().close();
			}
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
		JsonNode bodyObj = ServletUtil.readyBody(request);
		String username = bodyObj.get("username").asText();
		String password = DigestUtils.sha256Hex(bodyObj.get("password").asText());
		try {
			User user = userRepository.register(username, password);
			if(user!=null){
				request.setAttribute("user", user);
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().println("Invalid User");
		response.getWriter().close();
		
    }
}
