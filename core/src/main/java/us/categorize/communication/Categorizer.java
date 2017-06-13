package us.categorize.communication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import us.categorize.config.Config;
import us.categorize.communication.creation.MessageAssertion;
import us.categorize.communication.creation.attachment.AttachmentHandler;
import us.categorize.communication.creation.attachment.FileSystemAttachmentHandler;
import us.categorize.communication.creation.attachment.S3AttachmentHandler;
import us.categorize.model.User;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.SQLMessageRepository;
import us.categorize.repository.SQLTagRepository;
import us.categorize.repository.SQLUserRepository;
import us.categorize.repository.TagRepository;
import us.categorize.repository.UserRepository;

public class Categorizer {
	private MessageCommunicator messageCommunicator;
	private TagCommunicator tagCommunicator;
	private ThreadCommunicator threadCommunicator;
	private UserCommunicator userCommunicator;
	private Config config;
	
	public Categorizer(Config config) throws SQLException{
		this.config = config;
		System.out.println("Connecting " + config.getConnectString()+","+config.getDbUser()+","+config.getDbPass());
		Connection conn = DriverManager.getConnection(config.getConnectString(), config.getDbUser(), config.getDbPass());
		UserRepository userRepository = new SQLUserRepository(conn);
		TagRepository tagRepository = new SQLTagRepository(conn);
		MessageRepository messageRepository = new SQLMessageRepository(conn, userRepository);
		threadCommunicator = new ThreadCommunicator(tagRepository, messageRepository);
		tagCommunicator = new TagCommunicator(tagRepository, messageRepository);
		userCommunicator = new UserCommunicator(userRepository);
		AttachmentHandler attachmentHandler = null;
		if("S3".equals(config.getUploadStorage())){
			attachmentHandler = new S3AttachmentHandler(config.getS3bucket(), config.getS3region(), config.getAttachmentURLPrefix());
		}else{
			attachmentHandler = new FileSystemAttachmentHandler(config.getFileBase());
		}
		messageCommunicator = new MessageCommunicator(messageRepository, tagRepository, attachmentHandler, config.getMaxThumbWidth(), config.getMaxThumbHeight(), config.getMaxUploadSize());
	}
	
	public void handle(Frame request) throws Exception{
		System.out.println("Handling " + request.getResource() + " and " + request.getPath() + " as " + request.getMethod());
		if("msg".equals(request.getResource())){//TODO this is a gnarly hardcoded block, but deal with that after deployable
			handleMessage(request);
		}else if("thread".equals(request.getResource())){
			handleThread(request);
		}else if("tag".equals(request.getResource())){
			handleTag(request);
		}else if("user".equals(request.getResource())){
			handleUser(request);
		}
	}
	
	public void handleMessage(Frame request) throws Exception{
		System.out.println("Handling Message");
		if("GET".equals(request.getMethod())){
			String path = request.getPath();
			Long id = Long.parseLong(path.replace("/", ""));
			String sessionUUID = request.findSessionUUID();
			Map<String, String> headers = new HashMap<>();
			String cookieString = "categorizeus="+sessionUUID;
			headers.put("Set-Cookie", cookieString);
			System.out.println("Looking for message " + id);
			request.prepareResponse("OK", headers);
			messageCommunicator.readMessage(id, request.getOutputStream());
			request.finalizeResponse();
		}else if("POST".equals(request.getMethod())){
			long statedSize = Long.parseLong(request.getHeader("Content-Length"));
			if(statedSize>config.getMaxUploadSize()){
				request.prepareResponse("BadRequest", new HashMap<>());
				//TODO where is the error message stuff going to go? make a respond arbitrarily method?
				request.getOutputStream().write("[BadRequest] Size too large!".getBytes());
				request.finalizeResponse();
			}else{
				if(request.getCurrentUser()==null){
					loadCurrentUser(request);
				}
				User user = request.getCurrentUser();
				messageCommunicator.setSpeaker(user);
				MessageAssertion messageAssertion = messageCommunicator.createMessageFromStream(request.bodyInputStream());
				request.prepareResponse("OK", new HashMap<>());
		        String prototypeJson = "{\"id\":\"IDVALUE\"}";
		        prototypeJson = prototypeJson.replace("IDVALUE", messageAssertion.getMessage().getId()+"");
		        request.getOutputStream().write(prototypeJson.getBytes());
		        request.finalizeResponse();
			}
		}else{
			throw new Exception("[BadRequest] Unsupported request found");
		}
	}
	public User loadCurrentUser(Frame request) throws Exception{
		User currentUser = userCommunicator.loadSessionUser(request.findSessionUUID());
		request.setCurrentUser(currentUser);
		return currentUser;
	}

	public void handleThread(Frame request) throws Exception{
		if("POST".equals(request.getMethod())){
			request.prepareResponse("OK",new HashMap<>());
			threadCommunicator.queryStreams(request.bodyInputStream(), request.getOutputStream());
			request.finalizeResponse();
		}else{
			throw new Exception("[BadRequest] Unsupported request found");			
		}		
	}
	public void handleTag(Frame request) throws Exception{
		if("PUT".equals(request.getMethod()) || "POST".equals(request.getMethod())){
			tagCommunicator.categorizeMessages(request.bodyInputStream());
			request.prepareResponse("OK", new HashMap<>());
			request.getOutputStream().write("{\"response\":\"OK\"}".getBytes());
			request.finalizeResponse();
		}else{
			throw new Exception("[BadRequest] Unsupported request found");			
		}
	}
	public void handleUser(Frame request) throws Exception{
		User user = request.getCurrentUser();
		if(user==null){
			user = loadCurrentUser(request);
		}
		if("GET".equals(request.getMethod())){
			if(user==null){
				request.prepareResponse("NotFound", new HashMap<>());
				request.getOutputStream().write("Not Found".getBytes());
				request.finalizeResponse();
				return;
			}
			
			request.prepareResponse("OK", new HashMap<>());
			userCommunicator.writeUser(request.getCurrentUser(), request.getOutputStream());
			request.finalizeResponse();

		}else if("POST".equals(request.getMethod())){
			if(user!=null){//we're already logged in
				System.out.println("User is Already Logged In");
				request.getOutputStream().write("OK".getBytes());
				request.finalizeResponse();
				return;
			}
			String sessionUUID = request.findSessionUUID();
			user = userCommunicator.loginUser(request.bodyInputStream(), request.getOutputStream(), sessionUUID);
			if(user==null){
				System.out.println("User Is Not Valid");
				request.prepareResponse("Forbidden", new HashMap<>());
				request.getOutputStream().write("Must Login!".getBytes());
				request.finalizeResponse();
				return;
			}else{
				System.out.println("User Not Logged in OK");
				request.setCurrentUser(user);
				Map<String, String> headers = new HashMap<>();
				String cookieString = "categorizeus="+sessionUUID;
				headers.put("Set-Cookie", cookieString);
				request.prepareResponse("OK", headers);//TODO this is feeling all kinds of wrong
				request.finalizeResponse();
			}
			
		}else if("PUT".equals(request.getMethod())){
			if(user==null){
				request.prepareResponse("UNAUTHORIZED", new HashMap<>());
				request.getOutputStream().write("Must Login as Admin!".getBytes());
				request.finalizeResponse();
				return;
			}
			
			request.prepareResponse("OK", new HashMap<>());
			userCommunicator.registerUser(request.getCurrentUser(), request.bodyInputStream(), request.getOutputStream());
			request.finalizeResponse();
			
		}else if("DELETE".equals(request.getMethod())){
			if(user==null){
				request.prepareResponse("NotFound", new HashMap<>());
				request.getOutputStream().write("Not Found".getBytes());
				request.finalizeResponse();
				return;
			}
			request.prepareResponse("OK", new HashMap<>());
			userCommunicator.logoutUser(request.getCurrentUser(), request.findSessionUUID(), request.getOutputStream());
			request.finalizeResponse();
		}else{
			throw new Exception("[BadRequest] Unsupported request found");			
		}
	}
	public MessageCommunicator getMessageCommunicator() {
		return messageCommunicator;
	}

	public void setMessageCommunicator(MessageCommunicator messageCommunicator) {
		this.messageCommunicator = messageCommunicator;
	}

	public TagCommunicator getTagCommunicator() {
		return tagCommunicator;
	}

	public void setTagCommunicator(TagCommunicator tagCommunicator) {
		this.tagCommunicator = tagCommunicator;
	}

	public ThreadCommunicator getThreadCommunicator() {
		return threadCommunicator;
	}

	public void setThreadCommunicator(ThreadCommunicator threadCommunicator) {
		this.threadCommunicator = threadCommunicator;
	}

	public UserCommunicator getUserCommunicator() {
		return userCommunicator;
	}

	public void setUserCommunicator(UserCommunicator userCommunicator) {
		this.userCommunicator = userCommunicator;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}
}
