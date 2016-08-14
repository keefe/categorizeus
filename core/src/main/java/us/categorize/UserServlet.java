package us.categorize;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.databind.JsonNode;

import us.categorize.model.User;
import us.categorize.repository.UserRepository;
import us.categorize.util.ServletUtil;

public class UserServlet extends HttpServlet {
	
	
	
	private UserRepository userRepository;
	public UserServlet(UserRepository userRepository){
		this.userRepository = userRepository;
	}
	
	public void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		JsonNode bodyObj = ServletUtil.readyBody(request);
		String username = bodyObj.get("username").asText();
		String password = DigestUtils.sha256Hex(bodyObj.get("password").asText());
		System.out.println(password);
		try {
			User user = userRepository.validateUser(username, password);
			if(user!=null){
				request.getSession().setAttribute("user", user);
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
