package us.categorize.communication.creation.attachment;

import java.io.InputStream;

import us.categorize.communication.creation.MessageAssertionAttachment;

public interface AttachmentHandler {
	//returns URI for the attachment
	public String storeAttachment(String label, MessageAssertionAttachment attachmentAssertion, InputStream stream) throws Exception;
}
