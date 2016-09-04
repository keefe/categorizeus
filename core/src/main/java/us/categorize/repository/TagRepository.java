package us.categorize.repository;

import java.sql.SQLException;

import us.categorize.model.Tag;

public interface TagRepository {//#TODO should this merge into message repository?
	Tag tagFor(String label)  throws Exception;
	Tag[] tagsFor(String labels[]) throws Exception;
}
