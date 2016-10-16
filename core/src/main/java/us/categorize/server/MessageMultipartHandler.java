package us.categorize.server;

import java.io.InputStream;
import java.util.Map;

import us.categorize.model.Message;
import us.categorize.model.MessageRelation;
import us.categorize.model.Tag;
import us.categorize.model.User;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.TagRepository;
import us.categorize.server.http.MultipartHandler;

//#TODO figure out decorator or something better to organize this, but various model extensions come first so push it
public abstract class MessageMultipartHandler extends MultipartHandler {

	private MessageRepository messageRepository;
	private TagRepository tagRepository;
	
	public MessageMultipartHandler(MessageRepository messageRepository, TagRepository tagRepository) {
		super();
		this.messageRepository = messageRepository;
		this.tagRepository = tagRepository;
	}

	//TODO abstract class is def not right here, but want to push forward
	public abstract String handleFileUpload(String name, String filename, String contentType, InputStream inputStream);
	
	public void handleFileUpload(User user, Map<String, String> formFields, String name, String filename,
			String contentType, InputStream inputStream) {
		Message message = new Message();
		message.setBody(formFields.get("body"));
		message.setTitle(formFields.get("title"));
		message.setPostedBy(user);
		String messageTags = formFields.get("tags");
		String repliesToId = null;
		if(formFields.containsKey("repliesToId")){
			repliesToId = formFields.get("repliesToId");
		}
		System.out.println("We are replying to " + repliesToId);

		
		messageRepository.addMessage(message);
		try {
			System.out.println("Tags given as " + messageTags);
			if(messageTags!=null){
				String tagArray[] = messageTags.split(" ");
				if(tagArray.length>0){
					Tag tags[];
						tags = tagRepository.tagsFor(tagArray);
						messageRepository.tag(message, tags);
				}
			}
			if(repliesToId!=null){
				MessageRelation relation = new MessageRelation();
				relation.setSource(message);
				relation.setRelation(tagRepository.tagFor("repliesTo"));
				Message fauxReplySource = new Message();
				fauxReplySource.setId(Long.parseLong(repliesToId));
				relation.setSink(fauxReplySource);
				messageRepository.relate(relation);
			}else{
				messageRepository.tag(message, new Tag[]{tagRepository.tagFor("top")}); //TODO ugh, more refactoring				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Uploaded new file message to " + message.getId());
		String furi = handleFileUpload(""+message.getId(), filename, contentType, inputStream);//TODO think through this mapping more
		message.setLink(furi);
		messageRepository.updateMessage(message);
	}

}
