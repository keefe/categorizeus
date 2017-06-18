package us.categorize.api;

import us.categorize.model.*;

public class ThreadRequest{
    private Message baseMessage;
    private Tag transitivePredicate;
    private int maximumResults;
    public ThreadRequest(){
        
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

    public int getMaximumResults() {
        return maximumResults;
    }

    public void setMaximumResults(int maximumResults) {
        this.maximumResults = maximumResults;
    }
}