package us.categorize.communication.creation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

public class MessageAssertionAttachment {
	private String name, type, dataURL, size;

	public String getName() {
		return name;
	}
	
	public InputStream getDataInputStream(){
		String realData = dataURL.substring(dataURL.indexOf(",")+1);
		byte[] rawFileData = Base64.getDecoder().decode(realData);
		return new ByteArrayInputStream(rawFileData);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDataURL() {
		return dataURL;
	}

	public void setDataURL(String dataURL) {
		this.dataURL = dataURL;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
}
