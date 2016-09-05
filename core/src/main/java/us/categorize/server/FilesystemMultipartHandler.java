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
	public FilesystemMultipartHandler(MessageRepository messageRepository, String filebase) {
		super(messageRepository);
		this.filebase = filebase;
	}

	public void handleFileUpload(String name, String filename, InputStream stream) {
		BufferedInputStream bufferedStream = null;
		BufferedOutputStream outputStream = null;
		System.out.println("We're doing the actual upload with " + filebase);
		
		try {
			bufferedStream = new BufferedInputStream(stream);
			String fname = filebase+File.separator+name;
			System.out.println("Writing to this file " + fname);
			outputStream = new BufferedOutputStream(new FileOutputStream(new File(fname)));
			byte[] buff = new byte[8192];
			int readThisBatch = 0;
			while((readThisBatch = bufferedStream.read(buff)) != -1){
				outputStream.write(buff, 0, readThisBatch);
			}
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
	}
	
	
	
}
