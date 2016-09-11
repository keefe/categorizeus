package us.categorize.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import us.categorize.repository.MessageRepository;

public class FilesystemMultipartHandler extends MessageMultipartHandler{

	private String filebase;
	private String fileURIBase = "/files/";//TODO this needs to be pulled out
	public FilesystemMultipartHandler(MessageRepository messageRepository, String filebase) {
		super(messageRepository);
		this.filebase = filebase;
	}

	public String handleFileUpload(String name, String filename, String contentType, InputStream stream) {
		BufferedInputStream bufferedStream = null;
		BufferedOutputStream outputStream = null;
		System.out.println("We're doing the actual upload with " + filebase);
		String fileExtension = "dat";
		contentType = contentType.toLowerCase();//probably should be doing some error checking ;)
		if(contentType.contains("png")){
			fileExtension = "png";
		}else if(contentType.contains("jpg") || contentType.contains("jpeg")){
			fileExtension = "jpg";
		}else if(contentType.contains("gif")){
			fileExtension = "gif";
		}
		try {
			bufferedStream = new BufferedInputStream(stream);
			String fname = name+"."+fileExtension;
			String fpath = filebase + File.separator + fname;
			String furi = fileURIBase + fname;
			System.out.println("Writing to this file " + fpath);
			outputStream = new BufferedOutputStream(new FileOutputStream(new File(fpath)));
			byte[] buff = new byte[8192];//arbitrary constant inline, yuck
			int readThisBatch = 0;
			while((readThisBatch = bufferedStream.read(buff)) != -1){
				outputStream.write(buff, 0, readThisBatch);
			}
			return furi;
		} catch (IOException e) {
			// TODO Auto-generated catch block, yep deal with this later
			e.printStackTrace();
		} finally {
			try {
				if(bufferedStream!=null)
					bufferedStream.close();
				if(outputStream!=null)
					outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	
}
