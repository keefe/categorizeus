package us.categorize.communication.creation.attachment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import us.categorize.communication.creation.MessageAssertionAttachment;

public class FileSystemAttachmentHandler implements AttachmentHandler {
	private String fileURIBase;//TODO this needs to be pulled out
	private String filebase;
	
	public FileSystemAttachmentHandler(String fileURIBase, String filebase){
		this.filebase = filebase;
		this.fileURIBase = fileURIBase;
	}
	public FileSystemAttachmentHandler(String filebase) {
		this("/files",filebase);
	}
	
	@Override
	public String storeAttachment(String label, MessageAssertionAttachment attachmentAssertion, InputStream stream) throws Exception{
		// TODO Auto-generated method stub
		String contentType = attachmentAssertion.getType().toLowerCase();
		String fileExtension = "dat";
		if(contentType.contains("png")){
			fileExtension = "png";
		}else if(contentType.contains("jpg") || contentType.contains("jpeg")){
			fileExtension = "jpg";
		}else if(contentType.contains("gif")){
			fileExtension = "gif";
		}
		BufferedInputStream bufferedStream = null;
		BufferedOutputStream outputStream = null;
		//TODO I forget why I did the buffering stuff manually like this buffering, performance I guess?
		try {
			bufferedStream = new BufferedInputStream(stream);
			String fname = label+"."+fileExtension;
			String fpath = filebase + File.separator + fname;
			String furi = fileURIBase + "/" + fname;
			System.out.println("Writing to this file " + fpath);
			outputStream = new BufferedOutputStream(new FileOutputStream(new File(fpath)));
			byte[] buff = new byte[8192];//arbitrary constant inline, yuck
			int readThisBatch = 0;
			while((readThisBatch = bufferedStream.read(buff)) != -1){
				outputStream.write(buff, 0, readThisBatch);
			}
			return furi;
		} catch (IOException e) {
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
		
		throw new Exception("Unexpected Termination on saving " + label);
	}

}
