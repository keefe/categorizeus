package us.categorize.communication;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import us.categorize.model.User;

public interface Frame {//metadata around this action, will be context in aws lambda verse
	public InputStream bodyInputStream() throws Exception;//TODO maybe it would be better to go from inputstream to json here, 
	public String getHeader(String name);
	public String getPath();
	public User getCurrentUser();
	public void setCurrentUser(User user);
	public String getMethod();//showing some legacy HTTP entanglement here
	public String getResource();
	public OutputStream getOutputStream() throws Exception;//TODO I'm torn about whether it makes more sense to get this and pass it into a method or to use this class as a proxy as below, for now this is easier
	//	public void respond(String status, Map<String, String> headers, JsonNode body) throws Exception;//TODO same comment as above, wrote the comms classes without fully groking this
	public void prepareResponse(String status, Map<String, String> headers) throws Exception;//TODO same comment as above, wrote the comms classes without fully groking this
	public void finalizeResponse() throws Exception;//TODO this is why, because we have to build this meta JSON object for lambda vs directly responding in http
	public void log(String line);
	public String findSessionUUID();
	void clearUser();
}
