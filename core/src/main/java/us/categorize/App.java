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

import us.categorize.communication.TagCommunicator;
import us.categorize.communication.ThreadCommunicator;
import us.categorize.communication.UserCommunicator;
import us.categorize.communication.creation.attachment.AttachmentHandler;
import us.categorize.communication.creation.attachment.FileSystemAttachmentHandler;
import us.categorize.communication.creation.attachment.S3AttachmentHandler;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.SQLMessageRepository;
import us.categorize.repository.SQLTagRepository;
import us.categorize.repository.SQLUserRepository;
import us.categorize.repository.TagRepository;
import us.categorize.repository.UserRepository;
import us.categorize.server.http.AuthFilter;
import us.categorize.server.http.MessageServlet;
import us.categorize.server.http.SessionCookieFilter;
import us.categorize.server.http.TagServlet;
import us.categorize.server.http.ThreadServlet;
import us.categorize.server.http.UserServlet;

public class App {
	
	public static void main(String args[]) throws Exception {


		Properties properties = new Properties();
		
		properties.load(App.class.getResourceAsStream("/categorizeus.properties"));
		Config config = new Config(properties);
		Class.forName("org.postgresql.Driver");
		System.out.println("Postgres Driver Loaded");
		if (args.length > 0 && "initialize".equals(args[0])){
			initializeDB(config);
		}
		System.out.println("Initialization Complete");
		serverUp(config);
	}

	public static void initializeDB(Config config) throws ClassNotFoundException, SQLException, IOException {
		//System.out.println("Connecting with " + dbUser + " , " + dbPass);
		System.out.println("Attempting connect to " + config.getConnectString());
		Connection conn = DriverManager.getConnection(config.getConnectString(), config.getDbUser(), config.getDbPass());
		System.out.println("Connected to database for initialization");
		Statement st = conn.createStatement();
		executeFile(config.getClearSql(), st);
		executeFile(config.getCreateSql(), st);
		executeFile(config.getIndexSql(), st);
		executeFile(config.getSeedSql(), st);
		st.close();
		conn.close();
	}

	private static void executeFile(String filename, Statement st) throws IOException, SQLException {
		SQLReader init = new SQLReader(filename);
		for (String sql : init.getStatements()) {
			System.out.println("Executing " + sql);
			try {
				st.execute(sql);
			} catch (Exception e) {
				System.out.println("Error " + e.getMessage());
			}
		}
	}
	
	public static void serverUp(Config config) throws Exception{
		Connection conn = DriverManager.getConnection(config.getConnectString(), config.getDbUser(), config.getDbPass());
		UserRepository userRepository = new SQLUserRepository(conn);
		TagRepository tagRepository = new SQLTagRepository(conn);
		MessageRepository messageRepository = new SQLMessageRepository(conn, userRepository);

		System.out.println("Starting Server on Port " + config.getPort());
		Server server = new Server(config.getPort());
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);		
		context.setContextPath("/");
		System.out.println("Preparing to serve static files from " + config.getStaticDir());
		context.setResourceBase(config.getStaticDir());
		
		SessionCookieFilter sessionCookieFilter = new SessionCookieFilter(userRepository);
		FilterHolder sessionCookieFilterHolder = new FilterHolder(sessionCookieFilter);
		context.addFilter(sessionCookieFilterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
		
		ServletHandler handler = new ServletHandler();
		FilterHolder filterHolder = handler.addFilterWithMapping(AuthFilter.class, "/msg/*", EnumSet.of(DispatcherType.REQUEST));
		
		context.addFilter(filterHolder, "/msg/*", EnumSet.of(DispatcherType.REQUEST));
		MessageServlet messageServlet = null;
		if("S3".equals(config.getUploadStorage())){
			AttachmentHandler s3AttachmentHandler = new S3AttachmentHandler(config.getS3bucket(), config.getS3region(), config.getAttachmentURLPrefix());
			messageServlet = new MessageServlet(messageRepository, userRepository, tagRepository, s3AttachmentHandler, config.getMaxThumbWidth(), config.getMaxThumbHeight(), config.getMaxUploadSize());			
		}else{
			AttachmentHandler localAttachmentHandler = new FileSystemAttachmentHandler(config.getFileBase());
			 messageServlet = new MessageServlet(messageRepository, userRepository, tagRepository, localAttachmentHandler, config.getMaxThumbWidth(), config.getMaxThumbHeight(), config.getMaxUploadSize());
		}
		context.addServlet(new ServletHolder(messageServlet), "/msg/*");
		ThreadCommunicator threadCommunicator = new ThreadCommunicator(tagRepository, messageRepository);
		ThreadServlet threadServlet = new ThreadServlet(threadCommunicator);
		context.addServlet(new ServletHolder(threadServlet), "/thread/*");
		TagCommunicator tagCommunicator = new TagCommunicator(tagRepository, messageRepository);
		TagServlet tagServlet = new TagServlet(tagCommunicator);
		context.addServlet(new ServletHolder(tagServlet), "/tag/*");
		UserCommunicator userCommunicator = new UserCommunicator(userRepository);
		UserServlet userServlet = new UserServlet(userCommunicator);
		context.addServlet(new ServletHolder(userServlet), "/user/*");
		
		context.addServlet(DefaultServlet.class, "/");
		server.setHandler(context);
		server.start();
		server.join();
	}
	
}
