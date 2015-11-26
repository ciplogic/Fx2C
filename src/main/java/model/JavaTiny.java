package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JavaTiny {
    private String name;
    public JavaTiny(String name){
        this.name = name;
    }

    public String getName(){ return  name;}
    Map<String, String> Attributes =  new HashMap<>();
    List<JavaTiny> Children = new ArrayList<>();

    public String toString(){
        return name;
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
        return Children.size() != 0;
    }
    public List<JavaTiny> getChildren(){
        return Children;
    }
}
