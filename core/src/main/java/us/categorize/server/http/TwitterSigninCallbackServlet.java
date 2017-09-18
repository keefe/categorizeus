package us.categorize.server.http;

import javax.servlet.*;
import javax.servlet.http.*;
import twitter4j.*;
import twitter4j.auth.*;
import java.io.*;

public class TwitterSigninCallbackServlet extends HttpServlet{
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Twitter Signin Callback Servlet Handling GET");
        Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
        String verifier = request.getParameter("oauth_verifier");
        try {
            twitter.getOAuthAccessToken(requestToken, verifier);
            request.getSession().removeAttribute("requestToken");
            System.out.println("Logged in with Twitter As " + twitter.getScreenName() + " id " + twitter.getId());
        } catch (TwitterException e) {
            throw new ServletException(e);
        }
        response.sendRedirect(request.getContextPath() + "/");
    }
}
