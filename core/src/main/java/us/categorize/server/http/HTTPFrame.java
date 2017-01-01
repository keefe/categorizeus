package us.categorize.server.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.categorize.communication.Frame;
import us.categorize.model.User;

public class HTTPFrame implements Frame {
	private HttpServletRequest request;
    private HttpServletResponse response;
    private User user;
    private String resource, method;
	
	public HTTPFrame(String resource, String method, HttpServletRequest request,
                          HttpServletResponse response){
		this.request = request;
		this.response = response;
		this.resource = resource;
		this.method = method;
		User user = (User) request.getSession().getAttribute("user");
		setCurrentUser(user);
	}
	
	@Override
	public InputStream bodyInputStream() throws Exception{
		return request.getInputStream();
	}

	@Override
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	@Override
	public String getPath() {
		return request.getPathInfo();
	}

	@Override
	public User getCurrentUser() {
		return user;
	}

	@Override
	public void setCurrentUser(User user) {
		this.user = user;
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getResource() {
		return resource;
	}
	
	public String findSessionUUID(){
		
		String sessionUUID =  (String) request.getSession().getAttribute("categorizeus");
		if(sessionUUID==null){
			Cookie categorizeusCookie = findCategorizeUsCookie();
			if(categorizeusCookie!=null)
				sessionUUID = categorizeusCookie.getValue();
		}
		
		return sessionUUID;
	}

	private Cookie findCategorizeUsCookie() {
		Cookie categorizeusCookie = null;
		if(request.getCookies()!=null){
			for(Cookie cookie : request.getCookies()){
				if(cookie.getName().equals("categorizeus")){
					categorizeusCookie = cookie;
					break;
				}
			}
		}
		return categorizeusCookie;
	}
	public void clearUser(){
		Cookie categorizeusCookie = findCategorizeUsCookie();
		if(categorizeusCookie!=null){//TODO this doesn't appear to be actually removing the cookie, look into this further
			categorizeusCookie.setMaxAge(0);
			response.addCookie(categorizeusCookie);
		}
		request.getSession().removeAttribute("categorizeus");
		request.getSession().removeAttribute("user");
	}
	
	@Override
	public void prepareResponse(String status, Map<String, String> headers) throws Exception {
		response.setContentType("application/json");
		if("OK".equals(status)){
			response.setStatus(HttpServletResponse.SC_OK);
		}else if("Forbidden".equals(status)){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}else if("Not Found".equals(status)){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}else{
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		
	}

	@Override
	public OutputStream getOutputStream() throws Exception{
		return response.getOutputStream();
	}

	@Override
	public void finalizeResponse() throws Exception {
		response.getOutputStream().flush();//I think this is redundant
		response.getOutputStream().close();
	}

	@Override
	public void log(String line) {
		System.out.println(line);
	}

}
