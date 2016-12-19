package us.categorize.communication.creation.attachment;

import java.io.InputStream;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import us.categorize.communication.creation.MessageAssertionAttachment;
public class S3AttachmentHandler implements AttachmentHandler {

	private String bucket;
	private String region;
	
	public S3AttachmentHandler(String bucket, String region){
		this.bucket = bucket;
		this.region = region;
	}
	
	public String storeAttachment(String label, MessageAssertionAttachment attachmentAssertion, InputStream stream) {
		AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
		Region currentRegion = Region.getRegion(Regions.fromName(region));
		String contentType = attachmentAssertion.getType().toLowerCase();
		String fileExtension = "dat";
		if(contentType.contains("png")){
			fileExtension = "png";
		}else if(contentType.contains("jpg") || contentType.contains("jpeg")){
			fileExtension = "jpg";
		}else if(contentType.contains("gif")){
			fileExtension = "gif";
		}
		String fname = label+"."+fileExtension;
		ObjectMetadata s3Metadata = new ObjectMetadata();//TODO deal with specifying content length, per docs
		s3Metadata.setContentLength(Integer.parseInt(attachmentAssertion.getSize()));
		s3Metadata.setContentType(attachmentAssertion.getType());
		PutObjectRequest por = new PutObjectRequest(bucket, fname, stream, s3Metadata);
		PutObjectResult result = s3client.putObject(por);
		return fname;
	}

}
