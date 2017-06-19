package us.categorize.api;

import us.categorize.model.*;

public class ThreadRequest{
    private Message baseMessage;
    private Tag transitivePredicate;
    private int maxDepth = 3;//unclear how pagination and max result size works here yet
    private boolean searchSink = false;//this is the most confusing part, a relation like repliesTo
                                       //you will search the source, X repliesTo Y, Z repliesTo Y and so forth
                                        //this is called a "SINK" search, because the source of the relation changes
                                        //maybe this should migrate into the table structure, I dunno
                                        //on the other hand side, hasReply is the opposite, for exmple Y hasReply X
                                        // Y hasReplyZ and would be searchSink = true
    public ThreadRequest(){
        
    }
    public String toString(){
        return baseMessage+"\n"+transitivePredicate+"\n Max Depth " + maxDepth + "\n Search Sink? " + searchSink + "\n";
    }
    
    public boolean getSearchSink(){
        return searchSink;
    }
    
    public void setSearchSink(boolean searchSink){
        this.searchSink = searchSink;
    }
    public Message getBaseMessage() {
        return baseMessage;
    }

    public void setBaseMessage(Message baseMessage) {
        this.baseMessage = baseMessage;
    }

    public Tag getTransitivePredicate() {
        return transitivePredicate;
    }

    public void setTransitivePredicate(Tag transitivePredicate) {
        this.transitivePredicate = transitivePredicate;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
}