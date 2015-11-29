package model;

import infrastructure.Utf8String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JavaTiny {
    private Utf8String name;
    public JavaTiny(Utf8String name){
        this.name = name;
    }    
    
    public JavaTiny(String name){
        this.name = new Utf8String(name);
    }
    
    public String getName(){ return  name.toString();}
    Map<String, String> Attributes =  new HashMap<>();
    List<JavaTiny> Children = new ArrayList<>();

    @Override
    public String toString(){
        return getName();
    }

    public  String extractAttribute(String attrName ){
        if (Attributes.containsKey(attrName)) {
            String result = Attributes.get(attrName);
            Attributes.remove(attrName);
            return result;
        }
        return "";
    }

    public Map<String, String> getAttributes(){
        return Attributes;
    }

    public Boolean hasChildren() {
        return !Children.isEmpty();
    }
    public List<JavaTiny> getChildren(){
        return Children;
    }
}
