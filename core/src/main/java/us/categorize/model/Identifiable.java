package us.categorize.model;

public class Identifiable{
    
    private Long id;//TODO think about UUIDs, OK for now, maybe we do this as a generic?
    
    public Identifiable(){
                
    }
    
    public boolean isPersistent(){
        return id!=null;//for now, a null ID means that the object isn't stored yet, e.g. a create request
    }
    
    public void setId(Long id){
        this.id = id;
    }
    
    public Long getId(){
        return id;
    }
    
    
    
}
