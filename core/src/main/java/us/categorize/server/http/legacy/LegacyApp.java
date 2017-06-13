package us.categorize.server.http.legacy;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import us.categorize.config.*;
import us.categorize.communication.Categorizer;
import us.categorize.server.http.AuthFilter;
import us.categorize.server.http.SessionCookieFilter;

public class LegacyApp {
	public static void serverUp(Config config) throws Exception{
		Categorizer categorizer = new Categorizer(config);
		
		System.out.println("Starting Server on Port " + config.getPort());
		Server server = new Server(config.getPort());
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);		
		context.setContextPath("/");
		System.out.println("Preparing to serve static files from " + config.getStaticDir());
		context.setResourceBase(config.getStaticDir());
		
		SessionCookieFilter sessionCookieFilter = new SessionCookieFilter(categorizer.getUserCommunicator().getUserRepository());
		FilterHolder sessionCookieFilterHolder = new FilterHolder(sessionCookieFilter);
		context.addFilter(sessionCookieFilterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
		ServletHandler handler = new ServletHandler();
		FilterHolder filterHolder = handler.addFilterWithMapping(AuthFilter.class, "/msg/*", EnumSet.of(DispatcherType.REQUEST));
		context.addFilter(filterHolder, "/msg/*", EnumSet.of(DispatcherType.REQUEST));
		
		MessageServlet messageServlet = null;
		messageServlet = new MessageServlet(categorizer.getMessageCommunicator(), config.getMaxUploadSize());
		context.addServlet(new ServletHolder(messageServlet), "/msg/*");
		ThreadServlet threadServlet = new ThreadServlet(categorizer.getThreadCommunicator());
		context.addServlet(new ServletHolder(threadServlet), "/thread/*");
		TagServlet tagServlet = new TagServlet(categorizer.getTagCommunicator());
		context.addServlet(new ServletHolder(tagServlet), "/tag/*");
		UserServlet userServlet = new UserServlet(categorizer.getUserCommunicator());
		context.addServlet(new ServletHolder(userServlet), "/user/*");
		
		context.addServlet(DefaultServlet.class, "/");
		server.setHandler(context);
		server.start();
		server.join();
	}
}
