package us.categorize.server.aws.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import us.categorize.App;
import us.categorize.config.Config;
import us.categorize.communication.Categorizer;
import us.categorize.model.User;

public class ProxyStreamHandler implements RequestStreamHandler {
	
	private Categorizer categorizer;
	
	public ProxyStreamHandler(){

	}
	
	
	private void initialize() throws Exception{
		Properties properties = new Properties();
		
		try {
			properties.load(App.class.getResourceAsStream("/categorizeus.properties"));
			Config config = new Config(properties);
			Class.forName("org.postgresql.Driver");
			categorizer = new Categorizer(config);
			System.out.println("Initialization Complete");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	public static void main(String args[]){
		//java -cp core-1.0-SNAPSHOT.jar us.categorize.server.aws.lambda.ProxyStreamHandler
		System.out.println("I've found the class in the command line");
	}

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaFrame frame = new LambdaFrame(input, output, context);

		try {
			initialize();
			if(categorizer==null){
				context.getLogger().log("Initialization has failed, panic!");
			}
			User user = categorizer.loadCurrentUser(frame);
			categorizer.handle(frame);
		} catch (Exception e) {
			context.getLogger().log(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
