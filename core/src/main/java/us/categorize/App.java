package us.categorize;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.model.Message;
import us.categorize.model.User;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.SQLMessageRepository;
import us.categorize.repository.SQLTagRepository;
import us.categorize.repository.SQLUserRepository;
import us.categorize.repository.TagRepository;
import us.categorize.repository.UserRepository;

/**
 * Hello world!
 *
 */
public class App {
	private static  String clearSql, createSql, dbName, dbUser, dbPass, staticDir, indexSql;
	private static  int port;

	public static void main(String args[]) throws Exception {
		clearSql = System.getProperty("user.home") + "/projects/categorizeus/core/src/main/resources/sql/clear.sql";
		createSql = System.getProperty("user.home") + "/projects/categorizeus/core/src/main/resources/sql/tables.sql";
		indexSql = System.getProperty("user.home") + "/projects/categorizeus/core/src/main/resources/sql/indices.sql";

		dbName = System.getenv("CATEGORIZEUS_DB");
		dbUser = System.getenv("CATEGORIZEUS_DB_USER");
		dbPass = System.getenv("CATEGORIZEUS_DB_PASS");
		port = Integer.parseInt(System.getenv("CATEGORIZEUS_PORT"));
		staticDir = System.getenv("CATEGORIZEUS_STATIC");
		Class.forName("org.postgresql.Driver");

		if (args.length > 0 && "initialize".equals(args[0])){
			initializeDB(args);
		}
		System.out.println("Initialization Complete");
		//testRepos(args);
		serverUp(args);
	}

	public static void initializeDB(String args[]) throws ClassNotFoundException, SQLException, IOException {
		Connection conn = DriverManager.getConnection("jdbc:postgresql:" + dbName, dbUser, dbPass);
		Statement st = conn.createStatement();
		executeFile(clearSql, st);
		executeFile(createSql, st);
		executeFile(indexSql, st);
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
	
	public static void serverUp(String args[]) throws Exception{
		Connection conn = DriverManager.getConnection("jdbc:postgresql:" + dbName, dbUser, dbPass);
		UserRepository userRepository = new SQLUserRepository(conn);
		TagRepository tagRepository = new SQLTagRepository(conn);
		MessageRepository messageRepository = new SQLMessageRepository(conn, userRepository);


		
		System.out.println("Starting Server on Port " + port);
		Server server = new Server(port);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);		
		context.setContextPath("/");
		System.out.println("Preparing to serve static files from " + staticDir);
		context.setResourceBase(staticDir);
		server.setHandler(context);
		MessageServlet messageServlet = new MessageServlet(messageRepository);
		context.addServlet(new ServletHolder(messageServlet), "/msg/*");
		ThreadServlet threadServlet = new ThreadServlet();
		context.addServlet(new ServletHolder(threadServlet), "/thread/*");
		TagServlet tagServlet = new TagServlet(messageRepository, tagRepository);
		context.addServlet(new ServletHolder(tagServlet), "/tag/*");
		context.addServlet(DefaultServlet.class, "/");
		server.start();
		server.join();
	}
	
	public static void testRepos(String[] args) throws Exception {
		Connection conn = DriverManager.getConnection("jdbc:postgresql:" + dbName, dbUser, dbPass);
		System.out.println(conn);
		TagRepository tagRepository = new SQLTagRepository(conn);
		System.out.println(tagRepository.tagFor("replies"));
		System.out.println(tagRepository.tagFor("replies"));
		UserRepository userRepository = new SQLUserRepository(conn);
		User me = new User();
		me.setEmail("keefe@categorize.us");
		me.setUserName("keefe");
		me.setPasshash("redacted");
		userRepository.register(me);
		System.out.println(me);
		User lookupMe = userRepository.find(me.getUserId());
		System.out.println(lookupMe + " is the same " + (me==lookupMe));
		Message message = new Message();
		message.setBody("This is the body of the document");
		message.setTitle("a title, yes");
		message.setPostedBy(me);
		MessageRepository messageRepository = new SQLMessageRepository(conn, userRepository);
		messageRepository.addMessage(message);
		System.out.println(message);
		Message lookupMessage = messageRepository.getMessage(message.getId());
		System.out.println(lookupMessage + " is same " + (lookupMessage==message));
	}

	public static void test(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree("{\"k1\":\"v1\"}");
		System.out.println("Verifying Jackson Install " + actualObj.get("k1").asText());

		Class.forName("org.postgresql.Driver");
		Connection conn = DriverManager.getConnection("jdbc:postgresql:" + dbName, dbUser, dbPass);

		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM testing");
		while (rs.next()) {
			System.out.print("Column 1 returned ");
			System.out.println(rs.getString(1));
		}
		rs.close();
		st.close();

		conn.close();

	}
}
