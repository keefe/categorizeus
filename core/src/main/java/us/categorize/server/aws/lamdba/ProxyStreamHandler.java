package us.categorize.server.aws.lamdba;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import us.categorize.App;
import us.categorize.Config;
import us.categorize.communication.Categorizer;
import us.categorize.model.User;

public class ProxyStreamHandler implements RequestStreamHandler {
	
	private Categorizer categorizer;
	
	public ProxyStreamHandler(){
		Properties properties = new Properties();
		
		try {
			properties.load(App.class.getResourceAsStream("/categorizeus.properties"));
			Config config = new Config(properties);
			Class.forName("org.postgresql.Driver");
			System.out.println("Initialization Complete");
			categorizer = new Categorizer(config);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaFrame frame = new LambdaFrame(input, output, context);
		if(categorizer==null){
			context.getLogger().log("Initialization has failed, panic!");
		}
		try {
			User user = categorizer.loadCurrentUser(frame);
			categorizer.handle(frame);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
