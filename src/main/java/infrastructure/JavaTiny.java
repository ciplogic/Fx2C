package infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JavaTiny {
    public Map<String, String> Attributes = new HashMap<>();
    public List<JavaTiny> Children = new ArrayList<>();
    private String name;
    public JavaTiny(String name){
        this.name = name;
    }

    public String getName(){ return  name.toString();}

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
