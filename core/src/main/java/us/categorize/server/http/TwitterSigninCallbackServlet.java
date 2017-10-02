package us.categorize.server.http;

import javax.servlet.*;
import javax.servlet.http.*;
import twitter4j.*;
import twitter4j.auth.*;
import us.categorize.config.Config;
import us.categorize.repository.SQLUserRepository;
import us.categorize.repository.UserRepository;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TwitterSigninCallbackServlet extends HttpServlet{
	
	private UserRepository userRepository;
	
	public TwitterSigninCallbackServlet(Config config) {
		Connection conn;
		try {
			conn = DriverManager.getConnection(config.getConnectString(), config.getDbUser(), config.getDbPass());
			userRepository = new SQLUserRepository(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Twitter Signin Callback Servlet Handling GET");
        Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
        String verifier = request.getParameter("oauth_verifier");
        try {
            twitter.getOAuthAccessToken(requestToken, verifier);
            request.getSession().removeAttribute("requestToken");
            System.out.println("Logged in with Twitter As " + twitter.getScreenName() + " id " + twitter.getId());
            us.categorize.model.User user = userRepository.findByTwitter(twitter.getId());
            if(user==null) {
            	user = userRepository.createUserFromTwitter(twitter.getScreenName(), twitter.getId());
            }
    		String sessionUUID =  (String) request.getSession().getAttribute("categorizeus");
    		userRepository.createSessionUser(sessionUUID, user);
    		request.getSession().setAttribute("user", user);
            System.out.println("We have a user " + user.getUserName() + " for twitter " + user.getTwitterId() + " at " + user.getId());
        } catch (TwitterException e) {
            throw new ServletException(e);
        } catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        response.sendRedirect(request.getContextPath() + "/");
    }
}
