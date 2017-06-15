package us.categorize;

import java.util.*;


public class Initializer{
    
    public Initializer(){
        
    }
    
    public static void main(String args[]){
        Initializer initializer = new Initializer();
    }

    public void mainLoop(){
        Config config = App.readConfig();
        Scanner scanner = new Scanner(System.in);
        String input = null;
        
        do{
            switch(input){//requires JDK7, more efficient now I guess
                case:"admin"//once again I am tempted to use reflection
                    createAdmin();
                    break;
                case:"resetDatabase"
                    initializeDB(config);
                    break;
                case:"addUser"
                    addUser();
                    break;
                default:
                    System.out.println("That was not a recognized choice, please trying again");
                    break;
            }
        }while((input = scanner.nextLine())!=null && !"exit".equals(input));
    }
    private void createAdmin(){
        System.out.println("Creating Admin User");
    }
    private void addUser(){
        System.out.println("Adding User");
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

