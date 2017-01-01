package us.categorize.communication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import us.categorize.Config;
import us.categorize.communication.creation.attachment.AttachmentHandler;
import us.categorize.communication.creation.attachment.FileSystemAttachmentHandler;
import us.categorize.communication.creation.attachment.S3AttachmentHandler;
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
		if("msg".equals(request.getResource())){//TODO this is a gnarly hardcoded block, but deal with that after deployable
			
		}else if("thread".equals(request.getResource())){
			
		}else if("tag".equals(request.getResource())){
			
		}else if("user".equals(request.getResource())){
			
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
