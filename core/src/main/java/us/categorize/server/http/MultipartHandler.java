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
	
	public abstract void handleFileUpload(User user, Map<String, String> formFields, String name, String filename, InputStream inputStream);
	
	public boolean handle(HttpServletRequest request) throws FileUploadException, IOException{
		if(!ServletFileUpload.isMultipartContent(request)){
			return false;
		}
		ServletFileUpload upload = new ServletFileUpload();
		
		FileItemIterator it = upload.getItemIterator(request);
		Map<String, String> formFields = new HashMap<>();
		
		//fields and files sent in client order, we want the fields to process the files
		List<DeferredUpload> fileStreams = new LinkedList<DeferredUpload>();
		
		while(it.hasNext()){
			FileItemStream item = it.next();
			String name = item.getFieldName();
			if(item.isFormField()){
				formFields.put(name, Streams.asString(item.openStream()));
			}else{
				//think about the impact of opening the file here
				fileStreams.add(new DeferredUpload(item.getFieldName(), item.getName(), item.openStream()));
			}
		}
		User user = (User) request.getSession().getAttribute("user");
		
		for(DeferredUpload deferredUpload : fileStreams)
			handleFileUpload(user, formFields, deferredUpload.name, deferredUpload.filename, deferredUpload.stream);
		
		return true;
		
	}
	
	private class DeferredUpload{
		public String name;
		public String filename;
		public InputStream stream;
		public DeferredUpload(String name, String filename, InputStream stream) {
			this.name = name;
			this.filename = filename;
			this.stream = stream;
		}
		
	}
}
