package us.categorize;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SQLReader {

	private String statements[];
	
	public SQLReader(String fname) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(new File(fname)));
		String line = null;
		List<String> statements = new LinkedList<String>();
		String currentStatement = "";
		while((line = reader.readLine())!=null){
			currentStatement = currentStatement + line;
			if(line.contains(";")){
				statements.add(currentStatement.trim());
				currentStatement = "";
			}
		}
		reader.close();
		this.statements = statements.toArray(new String[]{});
	}

	public String[] getStatements() {
		return statements;
	}

	public void setStatements(String[] statements) {
		this.statements = statements;
	}
}
