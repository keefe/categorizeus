package us.categorize.integration;
import twitter4j.*;
import java.util.*;
import us.categorize.config.*;
import us.categorize.*;

public class TwitterVerify{
    public static void main(String args[]) throws Exception{
        System.out.println("Hey Look I'm a class act");
        try {
            // gets Twitter instance with default credentials
            Config config = App.readConfig();
            
            Twitter twitter = config.configureTwitter();
            User user = twitter.verifyCredentials();
            List<Status> statuses = twitter.getHomeTimeline();
            System.out.println("Showing @" + user.getScreenName() + "'s home timeline.");
            for (Status status : statuses) {
                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
            }
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get timeline: " + te.getMessage());
            System.exit(-1);
        }
    }
}
