package us.categorize.server.http;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.categorize.model.User;
import us.categorize.repository.UserRepository;

public class SessionCookieFilter implements Filter{
	private UserRepository userRepository;
	
	public SessionCookieFilter(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("Initializing Session Cookie Filter");

	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response; 
		if(httpRequest.getSession().getAttribute("categorizeus")==null){
			Cookie categorizeusCookie = null;
			for(Cookie cookie : httpRequest.getCookies()){
				if(cookie.getName().equals("categorizeus")){
					categorizeusCookie = cookie;
					break;
				}
			}
			if(categorizeusCookie==null){
				categorizeusCookie = new Cookie("categorizeus", UUID.randomUUID().toString());
				categorizeusCookie.setMaxAge(60*60*24*7);
				httpResponse.addCookie(categorizeusCookie);
			}else{//if(httpRequest.getSession().getAttribute("userLoadAttempted")==null) not needed, as only when attribute null so once
				User user = userRepository.findSessionUser(categorizeusCookie.getValue());
				if(user!=null){
					httpRequest.getSession().setAttribute("user", user);
					System.out.println("Currently Logged in User Is " + user.getUserName());
				}
			}
			System.out.println("Setting session ID " + categorizeusCookie.getValue());
			httpRequest.getSession().setAttribute("categorizeus", categorizeusCookie.getValue());			
		}else{
			//System.out.println(httpRequest.getSession().getAttribute("categorizeus"));
		}
		chain.doFilter(request, response);
	}
	
	@Override
	public void destroy() {
		System.out.println("destroying session cookie filter");
	}
}
