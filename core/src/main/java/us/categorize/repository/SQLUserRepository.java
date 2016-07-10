package us.categorize.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import us.categorize.model.User;

public class SQLUserRepository implements UserRepository {
	
	private Connection connection; 
	private Map<Long, User> id2user = new ConcurrentHashMap<>();
	
	
	public SQLUserRepository(Connection connection){
		this.connection = connection;
	}
	
	@Override
	public User find(long id) throws Exception {
		if(id2user.containsKey(id))
			return id2user.get(id);
		
		String findUser = "select * from users where id=?";
		PreparedStatement stmt = connection.prepareStatement(findUser);
		stmt.setLong(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs!=null && rs.next()){
			User user = new User();
			user.setUserId(rs.getLong("id"));
			user.setEmail(rs.getString("email"));
			user.setPasshash(rs.getString("passhash"));
			user.setUserName(rs.getString("username"));
			id2user.put(user.getUserId(), user);
			return user;
		}
		return null;
	}

	@Override
	public User validateUser(String username, String passhash) throws Exception {
		String findUser = "select * from users where username=? and passhash=?";
		PreparedStatement stmt = connection.prepareStatement(findUser);
		ResultSet rs = stmt.executeQuery();
		if(rs!=null && rs.next()){
			return find(rs.getLong("id"));			
		}
		return null;
	}

	@Override
	public User register(String username, String passhash) throws Exception{
		String insertUser = "insert into users(username, passhash) values (?,?)";
		PreparedStatement stmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
		stmt.setString(1, username);
		stmt.setString(2, passhash);
		stmt.executeUpdate();
		ResultSet rs = stmt.getGeneratedKeys();
		rs.next();
		long newId = rs.getLong(1);
		User user = find(newId);
		id2user.put(user.getUserId(), user);
		return user;
	}

}
