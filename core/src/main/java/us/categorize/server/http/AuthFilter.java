package us.categorize.server.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethod;

public class AuthFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("Initializing Filter");

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response; 
		
		if(httpRequest.getSession().getAttribute("testToken")==null){//TODO remove this once final session management is worked out
			httpRequest.getSession().setAttribute("testToken", "kdbr " + System.currentTimeMillis());
		}
		if(!httpRequest.getMethod().equals("GET")){
			System.out.println("Executing something other than GET, let's check for auth");
			if(httpRequest.getSession().getAttribute("user")==null){
				httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				httpResponse.getWriter().println("Login to Post");
				httpResponse.getWriter().close();
			}else{
				chain.doFilter(request, response);				
			}
		}else{
			chain.doFilter(request, response);			
		}
	}

	@Override
	public void destroy() {
		System.out.println("destroying filter");
	}

}
