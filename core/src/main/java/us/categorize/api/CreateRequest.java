package us.categorize.api;

import us.categorize.model.*;

public class CreateRequest<T extends Identifiable>{
    
    private T payload;
    
    public CreateRequest(T payload){
        this.payload = payload;
    }
    
    public T getPayload(){
        return payload;
    }
}