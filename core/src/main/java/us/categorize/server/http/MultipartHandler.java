package us.categorize.server.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import us.categorize.model.User;

public abstract class MultipartHandler {
	
	public abstract void handleFileUpload(User user, Map<String, String> formFields, String name, String filename, String contentType, InputStream inputStream);
	
	public boolean handle(HttpServletRequest request) throws FileUploadException, IOException{
		if(!ServletFileUpload.isMultipartContent(request)){
			return false;
		}
		ServletFileUpload upload = new ServletFileUpload();
		
		FileItemIterator it = upload.getItemIterator(request);
		Map<String, String> formFields = new HashMap<>();
		//now, we are at this moment dependent on client ordering, due to FileItemStream.ItemSkippedException
		//we can look at updating the message after in case the ordering comes out weird
		
		User user = (User) request.getSession().getAttribute("user");

		while(it.hasNext()){
			FileItemStream item = it.next();
			String name = item.getFieldName();
			if(item.isFormField()){
				formFields.put(name, Streams.asString(item.openStream()));
			}else{
				//think about the impact of opening the file here
				handleFileUpload(user, formFields, item.getFieldName(), item.getName(), item.getContentType(), item.openStream());
			}
		}
				
		return true;
		
	}
}
