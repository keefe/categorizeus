package us.categorize.server;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;

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
	private double maxThumbWidth, maxThumbHeight;

	
	public MessageMultipartHandler(double maxThumbWidth, double maxThumbHeight, MessageRepository messageRepository, TagRepository tagRepository) {
		super();
		this.messageRepository = messageRepository;
		this.tagRepository = tagRepository;
		this.maxThumbWidth = maxThumbWidth;
		this.maxThumbHeight = maxThumbHeight;
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
		InputStream sourceImageStream = inputStreamFor(furi);
		int resolutions[][] = generateThumbnail(sourceImageStream, contentType, furi, message);
		if(resolutions!=null){
			message.setImgWidth(resolutions[0][0]);
			message.setImgHeight(resolutions[0][1]);
			message.setThumbWidth(resolutions[1][0]);
			message.setThumbHeight(resolutions[1][1]);
		}else{
			System.out.println("ERROR creating thumbnail for " + furi);
		}
		message.setLink(furi);
		messageRepository.updateMessage(message);
	}

	private int[][] generateThumbnail(InputStream sourceImageStream, String contentType, String furi, Message message) {
		//returns returns[0] = width,height of origin returns[1] = width,height of thumbnail
		int [][] returns = new int[2][2];
		try {
			BufferedImage sourceImage = ImageIO.read(sourceImageStream);
			returns[0][0] = sourceImage.getWidth();
			returns[0][1] = sourceImage.getHeight();
			double scaleFactor = 1.0;
			if(sourceImage.getWidth()>sourceImage.getHeight()){
				scaleFactor = maxThumbWidth / ((double)sourceImage.getWidth());
			}else{
				scaleFactor = maxThumbHeight / ((double) sourceImage.getHeight());
			}
			if(scaleFactor>1) scaleFactor = 1;
			returns[1][0] = ((int) (scaleFactor * ((double)sourceImage.getWidth())));
			returns[1][1] = ((int) (scaleFactor * ((double)sourceImage.getHeight())));
			BufferedImage thumbnail = new BufferedImage(returns[1][0], returns[1][1], sourceImage.getType());
			Graphics2D graphics = thumbnail.createGraphics();
			graphics.drawImage(sourceImage, 0, 0, returns[1][0], returns[1][1], null);
			graphics.dispose();
			String parts[] = furi.split("\\.");
			String smallURI = "";
			for(int i=0; i<parts.length-1;i++){
				smallURI = smallURI+parts[i];//TODO stringbuilder?
			}
			smallURI = smallURI+"_small";//this is implied state, very bad
			String tokens[] = smallURI.split("/");
			smallURI = tokens[tokens.length-1];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(thumbnail, "png", baos);
			InputStream is = new ByteArrayInputStream(baos.toByteArray());
			
			String thumbURI = handleFileUpload(smallURI, "thumbnail", contentType, is);
			message.setThumbLink(thumbURI);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error Creating Image Stream");
			e.printStackTrace();
			return null;
		}		
		
		return returns;
	}

}
