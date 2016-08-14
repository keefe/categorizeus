package us.categorize.model;

import org.apache.commons.codec.digest.DigestUtils;

public class User {
	private String userName;
	private String passhash;
	private String email; 
	private long userId;
	
	public static void main(String args[]){
		String password = "";
		System.out.println(DigestUtils.sha256Hex(password));
	}
	public String toString(){
		return userId+":"+userName+":"+email;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
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
