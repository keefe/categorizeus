package us.categorize;

import java.util.*;
import java.sql.*;
import java.io.*;
import us.categorize.config.*;
import us.categorize.repository.*;
import us.categorize.model.*;
import us.categorize.api.*;

public class CLI{
    
    
    private Scanner scanner;
    private Corpus corpus;
    private Config config;
    
    public CLI(){
        scanner = new Scanner(System.in);
    }
    
    public static void main(String args[]) throws Exception{
        CLI initializer = new CLI();
        initializer.mainLoop();
    }

    public void mainLoop() throws Exception{
        config = Config.readRelativeConfig();
        corpus = config.initialize();
        
        String input = null;
    	System.out.println("Welcome to admin initialization interface");
    	System.out.println("If this is your first time, please select resetDatabase");//TODO this needs to be a command line argument
    	String greeting = "Please Select Admin function, valid choices are addAdmin,resetDatabase,addUser,createMessage,readMessage,tagSearch,readThread,login or exit to stop";
    	do{
	        System.out.println(greeting);
	        input = scanner.nextLine();
//		System.out.print("READ |||"+input+"|||");
            switch(input){//requires JDK7, more efficient now I guess
                case "addAdmin"://once again I am tempted to use reflection
                    createAdmin();
                    break;
                case "resetDatabase":
                    initializeDB(config);
                    break;
                case "addUser":
                    addUser();
                    break;
                case "createMessage":
                    createMessage();
                    break;
                case "readThread":
                    readThread();
                    break;
                case "readMessage":
                    readMessage();
                    break;
                case "tagSearch":
                    tagSearch();
                    break;
                case "login":
                    login();
                    break;
                default:
                    System.out.println("That was not a recognized choice, please trying again");
                    break;
		}
        }while(!"exit".equals(input));
    }
    
    private void readThread(){
        System.out.println("Load a message and all related messages based on some predicate");
        System.out.print("Base Message ID: ");
        String baseIdLine = scanner.nextLine();
        System.out.print("Transitive Predicate: ");
        String predicate = scanner.nextLine();
        //TODO add depth, search sink fields
        if(predicate.length()==0){
            System.out.println("Error, must specify a predicate to read a thread");
            return;
        }
        try{
            ThreadRequest request = new ThreadRequest();
            request.setBaseMessage(new Message(Long.parseLong(baseIdLine)));
	    Tag transitivePredicate = new Tag();
	    transitivePredicate.setTag(predicate);
	    request.setTransitivePredicate(transitivePredicate);
	    System.out.println("Performing Request With " + request);
            ThreadResponse response = corpus.findThread(request);
            System.out.println(response);
        }catch(NumberFormatException nfe){
            nfe.printStackTrace();
        }
    }
    
    private void createMessage(){
        System.out.println("Creating a new message, please enter details: ");
        System.out.print("Title: ");
        String title = scanner.nextLine();
        System.out.println();
        System.out.print("Body: ");
        String body = scanner.nextLine();
        System.out.print("User ID of Posted By: ");
        long userId = scanner.nextLong();
    	System.out.print("Message ID to reply to: ");
    	String replyLine = scanner.nextLine();
    	System.out.println("Finish Refactoring how to deal with reply " + replyLine);
        User fauxUser = new User();
        fauxUser.setId(userId);
        Message message = new Message();
        message.setTitle(title);
        message.setBody(body);
        message.setPostedBy(fauxUser);
        if(corpus.create(message)){
            System.out.println("New Message Created with ID " + message.getId());
        }else{
            System.out.println("Failed to create message for some reason");
        }
    }
    
    private Long readLongMaybe(){
        String input = scanner.nextLine();
        if(input.length()!=0){
            try{
                return Long.parseLong(input);                
            }catch(NumberFormatException nfe){
                System.out.println(input + " is not a number, IDs are longs right now");
            }
        }
        return null;
    }
    
    private void tagSearch(){
        String input = null;
        System.out.println("Performing Tag Search, last seen message ID? ");
        TagSearchRequest request = new TagSearchRequest();
        System.out.println("Enter last seen message ID or enter for none");
        Long lastSeenId = readLongMaybe();
        if(lastSeenId!=null){
            request.setLastKnownMessage(new Message(lastSeenId));
        }
        System.out.println("Enter Maximum Results or enter for none");
        Long maxMessages = readLongMaybe();
        if(maxMessages!=null){
            request.setMaximumResults(maxMessages);
        }
        System.out.println("Tags to search, one at a time, -1 to stop");
        do{
            if(input!=null){
                Tag tag = new Tag(input.trim());
                corpus.readOrCreate(tag);
                request.getTags().add(tag);
            }
        }while((input = scanner.nextLine())!=null && !"-1".equals(input));
        System.out.println(request);
        List<Message> results = corpus.tagSearch(request);
        for(Message m : results){
            System.out.println(m);
        }
    }
    
    private void login(){
        System.out.println("Enter Login Details");
        System.out.print("Username :");
        String userName = scanner.nextLine();
        System.out.print("Password :");
        String password = scanner.nextLine();
        System.out.println("Credentials entered as " + userName + " , " + password);
    }
    private void createAdmin(){
        System.out.println("Creating Admin User");
        System.out.println("Enter Login Details");
        System.out.print("Username :");
        String userName = scanner.nextLine();
        System.out.print("Password :");
        String password = scanner.nextLine();
        System.out.println("Credentials entered as " + userName + " , " + password);
    }
    private void addUser(){
        System.out.println("Adding User");
        System.out.println("Enter Login Details");//mmmm copy paste to get to more interesting stuff
        System.out.print("Username :");
        String userName = scanner.nextLine();
        System.out.print("Password :");
        String password = scanner.nextLine();
        System.out.println("Credentials entered as " + userName + " , " + password);
    }
    

	
	private void readMessage(){
	    System.out.print("Enter Message ID : ");
	    long id = scanner.nextLong();
	    scanner.nextLine();//this is confusing, maybe there is better way to approach this
	    Message stub = new Message();
	    stub.setId(id);
	    if(corpus.read(stub)){
	        System.out.println("Message Found as " + stub);
	    }else{
	        System.out.println("Message with id " + id + " was not found");
	    }
	}



//TODO this stuff absolutely does not belong here, needs to be backing store specific
    public static void initializeDB(Config config) throws ClassNotFoundException, SQLException, IOException {
        //this is a bloody mess!
		//System.out.println("Connecting with " + dbUser + " , " + dbPass);
		System.out.println("Attempting connect to " + config.getConnectString());
		Connection conn = DriverManager.getConnection(config.getConnectString(), config.getDbUser(), config.getDbPass());
		System.out.println("Connected to database for initialization");
		Statement st = conn.createStatement();
		executeFile(config.getClearSql(), st);
		executeFile(config.getCreateSql(), st);
		executeFile(config.getIndexSql(), st);
		executeFile(config.getSeedSql(), st);
		st.close();
		conn.close();
	}
	
	private static void executeFile(String filename, Statement st) throws IOException, SQLException {
		SQLReader init = new SQLReader(filename);
		for (String sql : init.getStatements()) {
			System.out.println("Executing " + sql);
			try {
				st.execute(sql);
			} catch (Exception e) {
				System.out.println("Error " + e.getMessage());
			}
		}
	}
    
    
}

