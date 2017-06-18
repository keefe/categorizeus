package us.categorize.repository;

import us.categorize.model.*;

public interface Corpus{
    
    public boolean create(Message message);
    public boolean create(Tag tag);
    public boolean create(User user);
    
    public boolean read(Message message);
    public boolean read(Tag tag);
    public boolean readOrCreate(Tag tag);
    public boolean read(User user);
    
    public List<Message> tagSearch(TagSearchRequest request);
    public ThreadResponse findThread(ThreadRequest request);
    
}