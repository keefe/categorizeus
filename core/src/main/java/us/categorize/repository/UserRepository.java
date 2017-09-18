package us.categorize.repository;

import java.sql.SQLException;

import us.categorize.model.User;

public interface UserRepository {
	User find(long id) throws Exception;
	User findByTwitter(long id) throws Exception;
	User validateUser(String username, String passhash) throws Exception;
	User register(String username, String passhash) throws Exception; 
	User findSessionUser(String sessionUUID);
	boolean createSessionUser(String sessionUUID, User user);
	boolean destroySessionUser(String sessionUUID);
	User createUserFromTwitter(String sn, long id) throws SQLException;

}
