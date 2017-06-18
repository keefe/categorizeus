package us.categorize.model;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User extends Identifiable{
	private String userName;
	
	@JsonIgnore//all this stuff is heinously tightly coupled why did I do this?
	private String passhash;
	private String email; 

	public String toString(){
		return "User String\n " + getId()+"\n"+userName+"\n"+email;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPasshash() {
		return passhash;
	}
	public void setPasshash(String passhash) {
		this.passhash = passhash;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
}
