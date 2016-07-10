package us.categorize;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class AuthFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("Initializing Filter");

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		System.out.println("Executing Auth Filter");
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if(httpRequest.getSession().getAttribute("testToken")==null){
			httpRequest.getSession().setAttribute("testToken", "kdbr " + System.currentTimeMillis());
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		System.out.println("destroying filter");
	}

}
