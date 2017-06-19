package us.categorize.api;

import us.categorize.model.*;
import java.util.*;

//so what we're going to do is manually change these over to JSON for now
//do we need an explicit last seen or continuation token thing? for now is OK
public class ThreadResponse{
    private Message baseMessage;
    private List<Message> related = new LinkedList<Message>();
    //this stuff is an issue. In a hibernate lazy loading world
    //these will be stubs of only IDs, so we'll treat them like that
    //but starting to show why something like hibernate is useful
    private Map<Message, Message> messageRelationships = new HashMap<Message, Message>();
    
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


    /*
    Things are getting a little bit abstract here, because of
    the order of relationship stuff. 
    Should this be some kind of a constraint, like one edge of a particular
    label can only exist, so X repliesTo Y like single inheritance?
    something to think about...
    
    For now, let's do this, why not? so we store repliesTo and you can 
    only respond to once, so that is a more restrictive relationship
    public void relate(Message subj, Message obj){
        if(messageRelationships.get(subj)==null){
            messageRelationships.put(subj, new LinkedList<Message>());
        }
        messageRelationships.get(subj).add(obj);
    }
    */
    public Map<Message,Message> getMessageRelationships() {
        return messageRelationships;
    }
}
