/**
 * 
 */
package us.categorize.server.http;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.communication.creation.MessageAssertion;
import us.categorize.communication.creation.MessageAssertionAttachment;
import us.categorize.communication.creation.attachment.AttachmentHandler;
import us.categorize.communication.streams.MessageStreamReader;
import us.categorize.model.Message;
import us.categorize.model.MessageRelation;
import us.categorize.model.Tag;
import us.categorize.model.User;
import us.categorize.repository.MessageRepository;
import us.categorize.repository.TagRepository;
import us.categorize.repository.UserRepository;

/**
 * @author keefe
 *
 */
public class MessageServlet extends HttpServlet {
	
	private MessageRepository messageRepository; 
	private UserRepository userRepository;
	private TagRepository tagRepository;
	private AttachmentHandler attachmentHandler;
	private double maxThumbWidth, maxThumbHeight;
	
	public MessageServlet(MessageRepository repository, UserRepository userRepository,TagRepository tagRepository, AttachmentHandler attachmentHandler, double maxThumbWidth, double maxThumbHeight){
		super();
		this.messageRepository = repository;
		this.userRepository = userRepository;
		this.tagRepository = tagRepository;
		this.attachmentHandler = attachmentHandler;
		this.maxThumbWidth = maxThumbWidth;
		this.maxThumbHeight = maxThumbHeight;
	}
	
	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
		System.out.println("Request made to " + request.getPathInfo());
		System.out.println("Session Check " + request.getSession().getAttribute("testToken"));

		String path = request.getPathInfo();
		if(path!=null && path.length()>0){
			try {
				Long id = Long.parseLong(path.replace("/", ""));
				Message message = messageRepository.getMessage(id);
				if(message!=null){
					ObjectMapper mapper = new ObjectMapper();
					String jsonMessage = mapper.writeValueAsString(message);
			        response.setContentType("application/json");
			        response.setStatus(HttpServletResponse.SC_OK);
			        response.getWriter().println(jsonMessage);
			        response.getWriter().close();
			        return;
				}
		        
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().println("Not Found");
        response.getWriter().close();
    }
	
	@Override
	public void doPut( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		
    }
	
	@Override
	public void doPost( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException,
    IOException
    {
		MessageStreamReader messageStreamReader = new MessageStreamReader();
		try {
			MessageAssertion assertion = messageStreamReader.readMessageAssertion(request.getInputStream());
			User user = (User) request.getSession().getAttribute("user");
			assertion.getMessage().setPostedBy(user);

			messageRepository.addMessage(assertion.getMessage());
			
			if(assertion.getRelationships().containsKey("repliesToId")){
				MessageRelation relation = new MessageRelation();
				relation.setSource(assertion.getMessage());
				relation.setRelation(tagRepository.tagFor("repliesTo"));
				Message fauxReplySource = new Message();
				fauxReplySource.setId(Long.parseLong(assertion.getRelationships().get("repliesToId")));
				relation.setSink(fauxReplySource);
				messageRepository.relate(relation);
			}else{
				assertion.getTags().add("top");
			}
			String[] tags = assertion.getTags().toArray(new String[]{});
			Tag tagObjs[] = tagRepository.tagsFor(tags);
			messageRepository.tag(assertion.getMessage(), tagObjs);
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
				messageRepository.updateMessage(assertion.getMessage());
			}
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().println(assertion.getMessage().getId());//#TODO replace this with json structure
	        response.getWriter().close();
	        return;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("Could not add new message for some reason");
        response.getWriter().close();
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
			InputStream is = new ByteArrayInputStream(baos.toByteArray());
			String thumbURI =attachmentHandler.storeAttachment(smallURI, attachment, is);
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
