package us.categorize.model;
import java.util.*;

public class Forum extends Identifiable{
    
    private User owner;
    
    private Tag topic;
    
    private Message basePost;
    
    public Forum(){
        
    }
    
    public String toString(){
        String s = "";
        s+="Forum ID " + getId() + " has topic " + tag.toString();
        s+=" Owner is " + owner.toString();
        s+=" Base Message is " + basePost.toString();
        return s;
    }
    public Long getId() {

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
