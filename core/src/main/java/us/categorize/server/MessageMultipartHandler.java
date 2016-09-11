package us.categorize.server;

import java.io.InputStream;
import java.util.Map;

import us.categorize.model.Message;
import us.categorize.model.User;
import us.categorize.repository.MessageRepository;
import us.categorize.server.http.MultipartHandler;

//#TODO figure out decorator or something better to organize this, but various model extensions come first so push it
public abstract class MessageMultipartHandler extends MultipartHandler {

	private MessageRepository messageRepository;
	
	
	public MessageMultipartHandler(MessageRepository messageRepository) {
		super();
		this.messageRepository = messageRepository;
	}

	//TODO abstract class is def not right here, but want to push forward
	public abstract void handleFileUpload(String name, String filename, String contentType, InputStream inputStream);
	
	public void handleFileUpload(User user, Map<String, String> formFields, String name, String filename,
			String contentType, InputStream inputStream) {
		Message message = new Message();
		message.setBody(formFields.get("body"));
		message.setTitle(formFields.get("title"));
		message.setPostedBy(user);
		messageRepository.addMessage(message);
		System.out.println("Uploaded new file message to " + message.getId());
		handleFileUpload(""+message.getId(), filename, contentType, inputStream);//TODO think through this mapping more
	}

}
