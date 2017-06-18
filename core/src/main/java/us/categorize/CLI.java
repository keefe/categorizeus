package us.categorize;

import java.util.*;
import java.sql.*;
import java.io.*;
import us.categorize.config.*;

public class CLI{
    
    
    private Scanner scanner;
    
    public Initializer(){
        scanner = new Scanner(System.in);
    }
    
    public static void main(String args[]) throws Exception{
        Initializer initializer = new Initializer();
        initializer.mainLoop();
    }

    public void mainLoop() throws Exception{
        Config config = App.readConfig();
        String input = null;
	System.out.println("Welcome to admin initialization interface");
	System.out.println("If this is your first time, please select resetDatabase");//TODO this needs to be a command line argument
	String greeting = "Valid choices are addAdmin, resetDatabase, addUser, createMessage, tagSearch, login or exit to stop";
	do{
	    System.out.println(greeting);
	    input = scanner.nextLine();
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
    
    private void createMessage(){
        System.out.println("Enter Message Details");
    }
    
    private void tagSearch(){
        List<String> tags = new LinkedList<String>();
        String input = null;
        System.out.println("Tags to search, one at a time, -1 to stop");
        do{
            if(input!=null){
                tags.add(input);
            }
        }while((input = scanner.nextLine())!=null && !"-1".equals(input));
        System.out.println("Tags Entered Were ");
        for(String tag : tags){
            System.out.print(tag+",");
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

