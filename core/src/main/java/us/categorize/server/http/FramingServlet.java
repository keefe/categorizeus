package us.categorize.server.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.categorize.communication.Categorizer;
import us.categorize.communication.Frame;

public class FramingServlet extends HttpServlet{
	private Categorizer categorizer;
	private String resource;
	public FramingServlet(String resource, Categorizer categorizer){
		this.categorizer = categorizer;
		this.resource = resource;
	}
	
	/*
	so I'm reimplementing this after it gets called weird
	was because quick hacking to get lambda up
	http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServlet.html#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	
	*/
	private void handle(String method, HttpServletRequest request, HttpServletResponse response) {
		Frame frame = new HTTPFrame(resource, method, request, response);
		try {
			categorizer.handle(frame);
			return;
		} catch (Exception e) {
			System.out.println("We've got a higher level exception, this is a bad thing");
			e.printStackTrace();
		}
		
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}
	
	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
		handle("GET", request, response);
    }

	
	@Override
    protected void doPost( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
		handle("POST", request, response);

    }
	
	@Override
    protected void doPut( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
		handle("PUT", request, response);

    }
	
	@Override
    protected void doDelete( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
		handle("DELETE", request, response);

    }
}
