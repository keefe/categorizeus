package us.categorize;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        Server server = new Server(8080); 
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        MessageServlet messageServlet = new MessageServlet("base");
        context.addServlet(new ServletHolder(messageServlet), "/");
        MessageServlet messageServlet2 = new MessageServlet("inner");
        context.addServlet(new ServletHolder(messageServlet2), "/test");
        server.start();
        server.join();
    }
}
