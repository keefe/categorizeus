package us.categorize.util;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServletUtil {
	public static JsonNode readyBody(HttpServletRequest request) throws IOException{//TODO ewwww static
		System.out.println("Request Content Type " + request.getContentType());
		ObjectMapper mapper = new ObjectMapper();
		String body = "";
		BufferedReader reader = request.getReader();
		String line = null;
		while((line = reader.readLine())!=null){
			body = body + line;
		}
		System.out.println("Read Body as " + body);
		JsonNode bodyObj = mapper.readTree(body);
		return bodyObj;
	}
}