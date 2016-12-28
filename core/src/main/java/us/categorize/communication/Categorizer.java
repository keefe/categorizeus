package us.categorize.communication;

import us.categorize.Config;

public class Categorizer {
	private MessageCommunicator messageCommunicator;
	private TagCommunicator tagCommunicator;
	private ThreadCommunicator threadCommunicator;
	private UserCommunicator userCommunicator;
	private Config config;
	
	public Categorizer(Config config){
		this.config = config;
	}
	
	public void handle(Frame request) throws Exception{
		if("msg".equals(request.getResource())){//TODO this is a gnarly hardcoded block, but deal with that after deployable
			
		}else if("thread".equals(request.getResource())){
			
		}else if("tag".equals(request.getResource())){
			
		}else if("user".equals(request.getResource())){
			
		}
	}
}
