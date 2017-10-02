package us.categorize.communication;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.communication.creation.MessageAssertion;
import us.categorize.communication.creation.MessageAssertionAttachment;
import us.categorize.model.Message;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.communication.creation.MessageAssertion;
import us.categorize.communication.creation.MessageAssertionAttachment;
import us.categorize.communication.creation.attachment.AttachmentHandler;
import us.categorize.model.*;
import us.categorize.repository.*;
import java.util.*;

public class MessageCommunicator {
	
	private AttachmentHandler attachmentHandler;
	private double maxThumbWidth, maxThumbHeight, maxUploadSize;
	private Corpus corpus;

	public MessageCommunicator(Corpus corpus, AttachmentHandler attachmentHandler, double maxThumbWidth, double maxThumbHeight, double maxUploadSize){
		this.corpus = corpus;
		this.attachmentHandler = attachmentHandler;
		this.maxThumbWidth = maxThumbWidth;
		this.maxThumbHeight = maxThumbHeight;
		this.maxUploadSize = maxUploadSize;
	}
	
	public MessageAssertion createMessageFromStream(InputStream stream, User speaker) throws Exception{
		
		MessageAssertion assertion = readMessageAssertion(stream);
		assertion.getMessage().setPostedBy(speaker);
		Long repliesToId = null;
		
		if(assertion.getRelationships().containsKey("repliesToId")){
			repliesToId = Long.parseLong(assertion.getRelationships().get("repliesToId"));
		}else{
			assertion.getTags().add("top");
		}
		corpus.create(assertion.getMessage(), repliesToId);
		List<Tag> tags = new LinkedList<Tag>();
		for(String tag : assertion.getTags()){
			tags.add(corpus.tagFor(tag));
		}
		corpus.tagMessage(assertion.getMessage(), tags);
		
		if(assertion.getAttachment()!=null && attachmentHandler!=null){
			String furi = attachmentHandler.storeAttachment(""+assertion.getMessage().getId(), assertion.getAttachment(), assertion.getAttachment().getDataInputStream());
			int resolutions[][] = generateThumbnail(assertion.getAttachment(), assertion.getAttachment().getDataInputStream(), assertion.getAttachment().getType(), furi, assertion.getMessage());
			if(resolutions!=null){
				assertion.getMessage().setImgWidth(resolutions[0][0]);
				assertion.getMessage().setImgHeight(resolutions[0][1]);
				assertion.getMessage().setThumbWidth(resolutions[1][0]);
				assertion.getMessage().setThumbHeight(resolutions[1][1]);
			}else{
				System.out.println("ERROR creating thumbnail for " + furi);
			}
			assertion.getMessage().setLink(furi);
			corpus.update(assertion.getMessage());
		}
		return assertion;
	}
	
	public void readMessage(long id, OutputStream output) throws Exception{
		Message message = new Message(id);
		corpus.read(message);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(output, message);
	}
	
	private int[][] generateThumbnail(MessageAssertionAttachment attachment, InputStream sourceImageStream, String contentType, String furi, Message message) {
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
			byte byteA[] = baos.toByteArray();
			InputStream is = new ByteArrayInputStream(byteA);
			attachment.setSize(byteA.length+"");
			String thumbURI =attachmentHandler.storeAttachment(smallURI, attachment, is);
			message.setThumbLink(thumbURI);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Creating Image Stream");
			e.printStackTrace();
			return null;
		}		
		
		return returns;
	}

	
	public Message readMessage(InputStream stream) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode bodyObj = mapper.readTree(stream);		
		return readMessage(bodyObj);
	}
	
	public MessageAssertion readMessageAssertion(InputStream stream) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode bodyObj = mapper.readTree(stream);
		MessageAssertion assertion = new MessageAssertion();
		assertion.setMessage(readMessage(bodyObj));
		String tagString = bodyObj.get("tags").asText();
		if(tagString!=null && tagString.length()>0){
			String tagArray[] = tagString.split(" ");
			for(String tag : tagArray)
				assertion.getTags().add(tag);
		}
		//TODO change this payload to have a generic relationships object
		if(bodyObj.has("repliesToId")){
			assertion.getRelationships().put("repliesToId",  bodyObj.get("repliesToId").asText());
		}
		
		if(bodyObj.has("attachment")){
			MessageAssertionAttachment attachment = new MessageAssertionAttachment();
			JsonNode attachmentNode = bodyObj.get("attachment");
			attachment.setDataURL(attachmentNode.get("dataURL").asText());
			attachment.setName(attachmentNode.get("name").asText());
			attachment.setType(attachmentNode.get("type").asText());
			attachment.setSize(attachmentNode.get("size").asText());
			assertion.setAttachment(attachment);
		}
		return assertion;
	}
	
	
	private Message readMessage(JsonNode bodyObj){
		Message message = new Message();
		message.setBody(bodyObj.get("body").asText());
		message.setTitle(bodyObj.get("title").asText());
		
		return message;
	}
}
