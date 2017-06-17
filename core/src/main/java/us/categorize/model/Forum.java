package us.categorize.model;
import java.util.*;

public class Forum{
    
    private long id;
    
    private User owner;
    
    private Tag topic;
    
    private Message basePost;
    
    public Forum(){
        
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Tag getTopic() {
        return topic;
    }

    public void setTopic(Tag topic) {
        this.topic = topic;
    }

    public Message getBasePost() {
        return basePost;
    }

    public void setBasePost(Message basePost) {
        this.basePost = basePost;
    }
    
}
