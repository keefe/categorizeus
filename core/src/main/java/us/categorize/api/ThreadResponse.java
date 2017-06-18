package us.categorize.api;

import us.categorize.model.*;
import java.util.*;

//so what we're going to do is manually change these over to JSON for now
//do we need an explicit last seen or continuation token thing? for now is OK
public class ThreadResponse{
    private Message baseMessage;
    private List<Message> related;
    //this stuff is an issue. In a hibernate lazy loading world
    //these will be stubs of only IDs, so we'll treat them like that
    //but starting to show why something like hibernate is useful
    private Map<Message, Message> messageRelationships;
    
    public ThreadResponse(){
        
    }
    
    public Message getBaseMessage() {
        return baseMessage;
    }

    public void setBaseMessage(Message message) {
        this.baseMessage = message;
    }

    public List<Message> getRelated() {
        return related;
    }

    public void setRelated(List<Message> related) {
        this.related = related;
    }

    public Map<Message,Message> getMessageRelationships() {
        return messageRelationships;
    }

    public void setMessageRelationships(Map<Message,Message> messageRelationships) {
        this.messageRelationships = messageRelationships;
    }
}
