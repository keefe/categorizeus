package us.categorize.api;

import us.categorize.model.*;

public class ReadRequest<T extends Identifiable>{
    
    private T payload;
    
    public ReadRequest(T payload){
        this.payload = payload;
    }
    
    public T getPayload(){
        return payload;
    }
}