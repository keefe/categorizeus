package us.categorize.server.aws.lambda;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import com.amazonaws.auth.policy.Resource;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import us.categorize.communication.Frame;
import us.categorize.model.User;

public class LambdaFrame implements Frame {
	private InputStream input;
	private OutputStream output;
	private Context context;
	private LambdaLogger logger;
	private JsonNode requestPlus;
	private JsonNode body;
	private JsonNode headers;
	private JsonNode inputJSON;
	private ObjectMapper mapper;
	private User user;
	private ByteArrayOutputStream responseBodyStream;
	private String responseStatus;
	private String path = null;
	private String resource;
	private Map<String, String> responseHeaders;
	private String sessionUUID;

	/*
	 * 
	 * Example input from AWS
	 {
  "message": "Hello me!",
  "input": {
    "resource": "/{proxy+}",
    "path": "/hello/world",
    "httpMethod": "POST",
    "headers": {
      "Accept": "",
      "Accept-Encoding": "gzip, deflate",
      "cache-control": "no-cache",
      "CloudFront-Forwarded-Proto": "https",
      "CloudFront-Is-Desktop-Viewer": "true",
      "CloudFront-Is-Mobile-Viewer": "false",
      "CloudFront-Is-SmartTV-Viewer": "false",
      "CloudFront-Is-Tablet-Viewer": "false",
      "CloudFront-Viewer-Country": "US",
      "Content-Type": "application/json",
      "headerName": "headerValue",
      "Host": "gy415nuibc.execute-api.us-east-1.amazonaws.com",
      "Postman-Token": "9f583ef0-ed83-4a38-aef3-eb9ce3f7a57f",
      "User-Agent": "PostmanRuntime/2.4.5",
      "Via": "1.1 d98420743a69852491bbdea73f7680bd.cloudfront.net (CloudFront)",
      "X-Amz-Cf-Id": "pn-PWIJc6thYnZm5P0NMgOUglL1DYtl0gdeJky8tqsg8iS_sgsKD1A==",
      "X-Forwarded-For": "54.240.196.186, 54.182.214.83",
      "X-Forwarded-Port": "443",
      "X-Forwarded-Proto": "https"
    },
    "queryStringParameters": {
      "name": "me"
    },
    "pathParameters": {
      "proxy": "hello/world"
    },
    "stageVariables": {
      "stageVariableName": "stageVariableValue"
    },
    "requestContext": {
      "accountId": "12345678912",
      "resourceId": "roq9wj",
      "stage": "testStage",
      "requestId": "deef4878-7910-11e6-8f14-25afc3e9ae33",
      "identity": {
        "cognitoIdentityPoolId": null,
        "accountId": null,
        "cognitoIdentityId": null,
        "caller": null,
        "apiKey": null,
        "sourceIp": "192.168.196.186",
        "cognitoAuthenticationType": null,
        "cognitoAuthenticationProvider": null,
        "userArn": null,
        "userAgent": "PostmanRuntime/2.4.5",
        "user": null
      },
      "resourcePath": "/{proxy+}",
      "httpMethod": "POST",
      "apiId": "gy415nuibc"
    },
    "body": "{\r\n\t\"a\": 1\r\n}",
    "isBase64Encoded": false
  }
}
	 
	 
	 * */
	public LambdaFrame(InputStream input, OutputStream output, Context context) throws JsonProcessingException, IOException{
		this.input = input;
		this.output = output;
		this.context = context;
		this.logger = context.getLogger();
		mapper = new ObjectMapper();
		requestPlus = mapper.readTree(input);
		logger.log(mapper.writeValueAsString(requestPlus));
		if(requestPlus.has("input")){
			inputJSON = requestPlus.get("input");			
		}else{
			inputJSON = requestPlus;
		}
		if(inputJSON.has("body")){
			String bodyText = inputJSON.get("body").asText();
			//System.out.println("Body Received From Lambda As " + bodyText);
			body = mapper.readTree(bodyText);	
		}
		if(inputJSON.has("headers")){
			headers = inputJSON.get("headers");	
			if(headers.has("Cookie")){
				String cookieString = headers.get("Cookie").asText();
				String crumbs[] = cookieString.split("=");
				System.out.println("Cookie Found! it was " + cookieString);
				if("categorizeus".equals(crumbs[0])){
					sessionUUID = crumbs[1];
					System.out.println("Found UUID " + sessionUUID);
				}

			}else
				sessionUUID = UUID.randomUUID().toString();
				
		}
		String fullPath = inputJSON.get("path").asText();
		if(fullPath.startsWith("/msg/")){
			resource = "msg";
			path = fullPath.replace("/msg/", "");
		}else if (fullPath.startsWith("/thread/")){
			resource = "thread";
			path = fullPath.replace("/thread/", "");			
		}else if (fullPath.startsWith("/tag/")){
			resource = "tag";
			path = fullPath.replace("/tag/", "");
		}else if (fullPath.startsWith("/user/")){
			resource = "user";
			path = fullPath.replace("/user/", "");
		}
		responseBodyStream = new ByteArrayOutputStream();
	}
	
	@Override
	public InputStream bodyInputStream() throws Exception {
		// TODO Auto-generated method stub
		ObjectWriter writer = mapper.writer();
		byte[] bits = writer.writeValueAsBytes(body);//TODO this is probably not very efficient, it definitely seems streamable
		return new ByteArrayInputStream(bits);
	}

	@Override
	public String getHeader(String name) {
		if(headers.has(name)){
			return headers.get(name).asText();
		}
		return null;
	}


	@Override
	public String getPath() {
		return path;
	}

	@Override
	public User getCurrentUser() {
		return user;
	}

	@Override
	public void setCurrentUser(User user) {
		this.user = user;
	}

	@Override
	public String getMethod() {
		return inputJSON.get("httpMethod").asText();
	}

	@Override
	public String getResource() {
		return resource;
	}

	@Override
	public OutputStream getOutputStream() throws Exception {
		return responseBodyStream;
	}

	@Override
	public void prepareResponse(String status, Map<String, String> headers) throws Exception {
		if("OK".equals(status)){
			responseStatus = ""+(HttpServletResponse.SC_OK);
		}else if("Forbidden".equals(status)){
			responseStatus = ""+(HttpServletResponse.SC_FORBIDDEN);
		}else if("Not Found".equals(status)){
			responseStatus = ""+(HttpServletResponse.SC_NOT_FOUND);
		}else{
			responseStatus = ""+(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		this.responseHeaders = headers;
	}

	@Override
	public void finalizeResponse() throws Exception {
		byte[] bodyBytes = responseBodyStream.toByteArray();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode bodyNode = mapper.readTree(bodyBytes);
		ObjectNode baseNode = JsonNodeFactory.instance.objectNode();
		String bodyString = mapper.writeValueAsString(bodyNode);
		baseNode.put("body", bodyString);
		ObjectNode headerNode = JsonNodeFactory.instance.objectNode();
		headerNode.put("Content-Type", "application/json");
		for(String responseHead : responseHeaders.keySet()){
			headerNode.put(responseHead, responseHeaders.get(responseHead));
		}
		baseNode.set("headers", headerNode);
		baseNode.put("statusCode", Long.parseLong(responseStatus));
		mapper.writeValue(output, baseNode);
	}

	@Override
	public void log(String line) {
		logger.log(line);
	}

	@Override
	public String findSessionUUID() {
		return sessionUUID;
	}

	@Override
	public void clearUser() {
		// TODO Auto-generated method stub
		
	}

}
