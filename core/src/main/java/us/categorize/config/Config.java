package us.categorize.config;

import java.util.*;
import java.io.*;
import us.categorize.repository.*;
import us.categorize.App;
import java.sql.*;
//TODO this needs to be broken off into multiple classes, for sure
//think about the annotations stuff or a builder/factory pattern probably something?
import twitter4j.*;
import twitter4j.conf.*;

public class Config {
	private String clearSql, createSql, dbHost, dbPort, dbName, dbUser, dbPass, staticDir, indexSql, seedSql, fileBase;
	private double maxThumbWidth, maxThumbHeight;
	
	private long maxUploadSize = -1;
	private  int port;
	
	private String s3bucket, s3region, attachmentURLPrefix, connectString;
	private String driverName = "org.postgresql.Driver";
	private String uploadStorage = "S3";
	//TODO this is going to the twitter section
	private String twitterConsumerKey, twitterConsumerSecret, twitterAccessToken, twitterAccessSecret;
	private String repositoryType;
	//TODO this is going tot he AWS specific section
	//TODO is AWS stuff appropriate to even use keys AT ALL with role based auth on EC2 instances of lambda roles?
	private String awsAccessKey, awsAccessSecret;
	
	

	public static Config readRelativeConfig() throws Exception{
		Properties properties = new Properties();
		InputStream input = App.class.getResourceAsStream("/categorizeus.properties");
		properties.load(input);
		StringWriter writer = new StringWriter();
		properties.list(new PrintWriter(writer));
		System.out.println("Properties File Read As ");
	  	System.out.println(writer.getBuffer().toString());
		Config config = new Config(properties);
		return config;
	}
	
	public Config(Properties properties){
		clearSql = properties.getProperty("SQL_BASE") + "core/src/main/resources/sql/clear.sql";//TODO refactor to load from the jar as above
		createSql = properties.getProperty("SQL_BASE") + "core/src/main/resources/sql/tables.sql";
		indexSql = properties.getProperty("SQL_BASE") + "core/src/main/resources/sql/indices.sql";
		seedSql = properties.getProperty("SQL_BASE") + "core/src/main/resources/sql/seed.sql";		
		dbName = properties.getProperty("DB_NAME");
		dbHost = properties.getProperty("DB_HOST");
		dbPort = properties.getProperty("DB_PORT");
		dbUser = properties.getProperty("DB_USER");
		dbPass = properties.getProperty("DB_PASS");
		s3bucket = properties.getProperty("S3_ASSETS_BUCKET");
		s3region = properties.getProperty("AWS_REGION");
		repositoryType = properties.getProperty("REPOSITORY_TYPE");
		twitterConsumerKey= properties.getProperty("TWITTER_CONSUMER_KEY");
		twitterConsumerSecret= properties.getProperty("TWITTER_CONSUMER_SECRET");
		twitterAccessToken= properties.getProperty("TWITTER_ACCESS_TOKEN");
		twitterAccessSecret= properties.getProperty("TWITTER_ACCESS_SECRET");
		if(properties.containsKey("UPLOAD_STORAGE")){
			uploadStorage = properties.getProperty("UPLOAD_STORAGE");
		}
		attachmentURLPrefix = properties.getProperty("ATTACHMENT_URL_PREFIX");
		connectString = "jdbc:postgresql:" +"/"+"/"+ dbHost+":"+dbPort+"/"+dbName;
		System.out.println("nc Connect String Detected As " + connectString);
		maxUploadSize = Long.parseLong(properties.getProperty("MAX_UPLOAD_SIZE"));
		maxThumbWidth = Double.parseDouble(properties.getProperty("MAX_THUMB_WIDTH"));
		maxThumbHeight = Double.parseDouble(properties.getProperty("MAX_THUMB_HEIGHT"));
		port = Integer.parseInt(properties.getProperty("PORT"));
		staticDir = properties.getProperty("STATIC_DIR");
		fileBase = staticDir + "/files";
	}
	
	public Corpus initialize() throws Exception{
		if(repositoryType==null || !"sql".equals(repositoryType.toLowerCase())){
			throw new Exception(repositoryType + " is not a known repository type, exiting");
		}
		Class.forName("org.postgresql.Driver");
		System.out.println("Postgres Driver Loaded");
		System.out.println("Connecting " + getConnectString()+","+getDbUser()+","+getDbPass());
		Connection conn = DriverManager.getConnection(getConnectString(), getDbUser(), getDbPass());
		return new SQLCorpus(conn);
	}

	public Twitter configureTwitter(){

		return configureTwitter(true);
	}

	public Twitter configureTwitter(boolean systemAccessToken){
		System.out.println("Twitter Read as " + twitterConsumerKey+","+twitterConsumerSecret+","+twitterAccessToken+","+twitterAccessSecret); 
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)//TODO what do about this?
		  .setOAuthConsumerKey(twitterConsumerKey)
		  .setOAuthConsumerSecret(twitterConsumerSecret);
		
		if(systemAccessToken) {
			  cb.setOAuthAccessToken(twitterAccessToken)
			  .setOAuthAccessTokenSecret(twitterAccessSecret);			
		}
		TwitterFactory tf = new TwitterFactory(cb.build());
		return tf.getInstance();
	}
	
	public String getRepositoryType(){
		return repositoryType;
	}
	public void setRepositoryType(String type){
		this.repositoryType = type;
	}
	
	
	public String getAttachmentURLPrefix() {
		return attachmentURLPrefix;
	}

	public String getClearSql() {
		return clearSql;
	}

	public String getConnectString() {
		return connectString;
	}

	public String getCreateSql() {
		return createSql;
	}

	public String getDbHost() {
		return dbHost;
	}

	public String getDbName() {
		return dbName;
	}

	public String getDbPass() {
		return dbPass;
	}

	public String getDbPort() {
		return dbPort;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getFileBase() {
		return fileBase;
	}

	public String getIndexSql() {
		return indexSql;
	}

	public double getMaxThumbHeight() {
		return maxThumbHeight;
	}

	public double getMaxThumbWidth() {
		return maxThumbWidth;
	}

	public long getMaxUploadSize() {
		return maxUploadSize;
	}

	public int getPort() {
		return port;
	}

	public String getS3bucket() {
		return s3bucket;
	}

	public String getS3region() {
		return s3region;
	}

	public String getSeedSql() {
		return seedSql;
	}

	public String getStaticDir() {
		return staticDir;
	}

	public void setAttachmentURLPrefix(String attachmentURLPrefix) {
		this.attachmentURLPrefix = attachmentURLPrefix;
	}

	public void setClearSql(String clearSql) {
		this.clearSql = clearSql;
	}

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	public void setCreateSql(String createSql) {
		this.createSql = createSql;
	}

	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public void setDbPass(String dbPass) {
		this.dbPass = dbPass;
	}

	public void setDbPort(String dbPort) {
		this.dbPort = dbPort;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public void setFileBase(String fileBase) {
		this.fileBase = fileBase;
	}

	public void setIndexSql(String indexSql) {
		this.indexSql = indexSql;
	}

	public void setMaxThumbHeight(double maxThumbHeight) {
		this.maxThumbHeight = maxThumbHeight;
	}

	public void setMaxThumbWidth(double maxThumbWidth) {
		this.maxThumbWidth = maxThumbWidth;
	}

	public void setMaxUploadSize(long maxUploadSize) {
		this.maxUploadSize = maxUploadSize;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setS3bucket(String s3bucket) {
		this.s3bucket = s3bucket;
	}

	public void setS3region(String s3region) {
		this.s3region = s3region;
	}

	public void setSeedSql(String seedSql) {
		this.seedSql = seedSql;
	}

	public void setStaticDir(String staticDir) {
		this.staticDir = staticDir;
	}
	

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getUploadStorage() {
		return uploadStorage;
	}

	public void setUploadStorage(String uploadStorage) {
		this.uploadStorage = uploadStorage;
	}
}
