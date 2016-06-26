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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User register(User user) throws Exception{
		String insertUser = "insert into users(username, email, passhash) values (?,?,?)";
		PreparedStatement stmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
		stmt.setString(1, user.getUserName());
		stmt.setString(2, user.getEmail());
		stmt.setString(3, user.getPasshash());
		stmt.executeUpdate();
		ResultSet rs = stmt.getGeneratedKeys();
		rs.next();
		user.setUserId(rs.getLong(1));
		id2user.put(user.getUserId(), user);
		return user;
	}

}
