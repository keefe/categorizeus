package us.categorize;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.Properties;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import us.categorize.communication.Categorizer;
import us.categorize.communication.MessageCommunicator;
import us.categorize.communication.TagCommunicator;
import us.categorize.communication.ThreadCommunicator;
import us.categorize.communication.UserCommunicator;
import us.categorize.communication.creation.attachment.AttachmentHandler;
import us.categorize.communication.creation.attachment.FileSystemAttachmentHandler;
import us.categorize.communication.creation.attachment.S3AttachmentHandler;
import us.categorize.server.http.*;
import us.categorize.server.*;
import us.categorize.config.*;

import java.io.*;

public class App {
	
	public static void main(String args[]) throws Exception {
		serverUpGeneric();
	}
	public static void serverUpGeneric() throws Exception{
		Categorizer categorizer = new Categorizer();
		Config config = categorizer.getConfig();
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
		
		FramingServlet messageServlet = new FramingServlet("msg", categorizer);
		context.addServlet(new ServletHolder(messageServlet), "/msg/*");
		FramingServlet threadServlet = new FramingServlet("thread", categorizer);
		context.addServlet(new ServletHolder(threadServlet), "/thread/*");
		FramingServlet tagServlet = new FramingServlet("tag", categorizer);
		context.addServlet(new ServletHolder(tagServlet), "/tag/*");
		FramingServlet userServlet = new FramingServlet("user", categorizer);
		context.addServlet(new ServletHolder(userServlet), "/user/*");
		TwitterSigninServlet twitterSignin = new TwitterSigninServlet(config);
		context.addServlet(new ServletHolder(twitterSignin), "/twitter_signin/*");
		TwitterSigninCallbackServlet twitterSigninCallback = new TwitterSigninCallbackServlet();
		context.addServlet(new ServletHolder(twitterSigninCallback), "/twitter_callback/*");
		
		context.addServlet(DefaultServlet.class, "/");
		server.setHandler(context);
		server.start();
		server.join();
	}	
}
