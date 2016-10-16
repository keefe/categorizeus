package us.categorize.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
		stmt.setString(1, username);
		stmt.setString(2, passhash);
		System.out.println(stmt.toString());
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

	@Override
	public User findSessionUser(String sessionUUID) {
		String findSessionUser = "select * from user_sessions where session_uuid=?";
		try {
			PreparedStatement stmt = connection.prepareStatement(findSessionUser);
			stmt.setString(1,  sessionUUID);
			ResultSet rs = stmt.executeQuery();
			if(rs!=null && rs.next()){
				return find(rs.getLong("user_id"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public boolean createSessionUser(String sessionUUID, User user) {
		String createUserSession = "insert into user_sessions(session_uuid, user_id) values (?,?)";
		try{
			PreparedStatement stmt = connection.prepareStatement(createUserSession);
			stmt.setString(1,sessionUUID);
			stmt.setLong(2, user.getUserId());
			stmt.executeUpdate();
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean destroySessionUser(String sessionUUID) {
		String deleteUserSession = "delete from user_sessions where session_uuid = ?";
		try {
			PreparedStatement stmt = connection.prepareStatement(deleteUserSession);
			stmt.setString(1, sessionUUID);
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
