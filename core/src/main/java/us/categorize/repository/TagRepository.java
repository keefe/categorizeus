package us.categorize.repository;

import java.sql.SQLException;

import us.categorize.model.Tag;

public interface TagRepository {
	Tag tagFor(String label)  throws Exception;
}
