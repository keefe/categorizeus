package us.categorize.communication;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.categorize.model.User;
import us.categorize.repository.UserRepository;

public class UserCommunicator {
	private UserRepository userRepository;

	public UserCommunicator(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public void writeUser(User user, OutputStream output) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(output, user);
	}
	
	public void logoutUser(User user, String sessionUUID, OutputStream output) throws Exception{
		userRepository.destroySessionUser(sessionUUID);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(output, user);
	}
	
	public void registerUser(User currentUser, InputStream input, OutputStream output) throws Exception{
		/***
		 * 				WARNING
		 * 
		 * 
		 * 				TODO remove hard coded garbage!
		 * 
		 * 				Due to time constraints, only the seed users are allowed to create new users. Security like a sieve right here.
		 * 
		 * 
		 * **/
		if(currentUser==null || currentUser.getUserId()>=7){
	        throw new Exception("[FORBIDDEN] Stay tuned for a user model with authorization that makes sense");
		}
		ObjectMapper mapper = new ObjectMapper();
		JsonNode bodyObj = mapper.readTree(input);
		String username = bodyObj.get("username").asText();
		String password = DigestUtils.sha256Hex(bodyObj.get("password").asText());
		User user = userRepository.register(username, password);
		if(user==null){
			throw new Exception("[ERROR] could not create user for some reason");
		}
		mapper.writeValue(output, user);
	}
	
	public User loginUser(InputStream input, OutputStream output, String sessionUUID) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode bodyObj = mapper.readTree(input);
		String username = bodyObj.get("username").asText();
		String password = DigestUtils.sha256Hex(bodyObj.get("password").asText());
		User user = userRepository.validateUser(username, password);
		if(user!=null){
			userRepository.createSessionUser(sessionUUID, user);//TODO wtf is this?
			mapper.writeValue(output, user);
		}
		return user;
		
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}

	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	

}
