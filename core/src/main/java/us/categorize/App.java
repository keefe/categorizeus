package us.categorize;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
    	ObjectMapper mapper = new ObjectMapper();
    	JsonNode actualObj = mapper.readTree("{\"k1\":\"v1\"}");
    	System.out.println("Verifying Jackson Install " + actualObj.get("k1").asText());
    	
    	String dbName = System.getenv("CATEGORIZEUS_DB");
    	String dbUser = System.getenv("CATEGORIZEUS_DB_USER");
    	String dbPass = System.getenv("CATEGORIZEUS_DB_PASS");
    	
    	Class.forName("org.postgresql.Driver");
    	Connection conn = DriverManager.getConnection("jdbc:postgresql:"+dbName, dbUser, dbPass);
    	
    	Statement st = conn.createStatement();
    	ResultSet rs = st.executeQuery("SELECT * FROM testing");
    	while (rs.next())
    	{
    	   System.out.print("Column 1 returned ");
    	   System.out.println(rs.getString(1));
    	} rs.close();
    	st.close();
    	
    	conn.close();
    	int port = Integer.parseInt(System.getenv("CATEGORIZEUS_PORT"));
    	System.out.println("Starting Server on Port " + port);
        Server server = new Server(port); 
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        String staticDir = System.getenv("CATEGORIZEUS_STATIC");
        System.out.println("Preparing to serve static files from " + staticDir);
        context.setResourceBase(staticDir);
        server.setHandler(context);
        MessageServlet messageServlet = new MessageServlet("msg");
        context.addServlet(new ServletHolder(messageServlet), "/msg/*");
        ThreadServlet threadServlet = new ThreadServlet();
        context.addServlet(new ServletHolder(threadServlet), "/thread/*");
        TagServlet tagServlet = new TagServlet();
        context.addServlet(new ServletHolder(tagServlet), "/tag/*");
        context.addServlet(DefaultServlet.class, "/");
        server.start();
        server.join();
    }
}
