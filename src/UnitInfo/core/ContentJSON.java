package UnitInfo.core;

import arc.Core;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import mindustry.*;
import mindustry.ctype.*;
import org.hjson.*;

import java.lang.reflect.*;

import static mindustry.Vars.modDirectory;

public class ContentJSON {
    public static void collect(String name, Object object, JsonObject obj, boolean isloop) {
        Object preval = obj.get(name);
        if(preval != null) obj.set(name, preval + " or " + object);
        else {
            if(object == null) obj.add(name, "null");
            else if(!Modifier.isStatic(object.getClass().getModifiers())) { //no static
                if(object instanceof Integer val) obj.add(name, val);
                else if(object instanceof Double val) obj.add(name, val);
                else if(object instanceof Float val) obj.add(name, val);
                else if(object instanceof Long val) obj.add(name, val);
                else if(object instanceof String val) obj.add(name, val);
                else if(object instanceof Boolean val) obj.add(name, val);
                else if(object instanceof Content cont){ //create new json object
                    try {
                        obj.add(name, getContent(cont, new JsonObject(), isloop));
                    } catch(Throwable e) {
                        Log.info(e.getMessage() + " ### " + cont);
                    }
                }
                else if(object instanceof ObjectMap map){ //create new json object
                    /*
                    try {
                        JsonObject object1 = new JsonObject();
                        map.each((k, v) -> {
                            object1.add(k.toString(), getContent(v, new JsonObject(), isloop))
                        });
                        obj.add(name, getContent(cont, new JsonObject(), isloop));
                    } catch(Throwable e) {
                        Log.info(e.getMessage() + " ### " + cont);

                    }
                    */
                }
                else if(object instanceof Seq seq && seq.any()) {
                    StringBuilder str = new StringBuilder("[");
                    for(int i = 0; i < seq.size; i++) {
                        if(seq.get(i) != null) str.append(seq.get(i).toString()).append(i+1==seq.size ? "" : ", ");
                    }
                    str.append("]");
                    obj.add(name, str.toString());
                }
                else {
                    if(object.getClass().isArray()) {
                        StringBuilder str = new StringBuilder("[");
                        for(int i = 0; i < Array.getLength(object); i++) {
                            if(Array.get(object, i) != null) str.append(Array.get(object, i).toString()).append(i+1==Array.getLength(object) ? "" : ", ");
                        }
                        str.append("]");
                        obj.add(name, str.toString());
                    } else {
                        obj.add(name, object.toString());
                    }
                }
            }
        }
    }

    public static JsonObject getContent(Content cont, JsonObject obj, boolean isloop) {
         for(Field field : cont.getClass().getFields()){
            try {
                if(!isloop) collect(field.getName(), field.get(cont), obj, true);
            } catch(Throwable e) {
                Log.info(e.getMessage() + " ### " + cont);
                obj.add(field.getName(), "### ERROR ###");
            }
        }

        return obj;
    }

    public static void save() {
        for(Seq<Content> content : Vars.content.getContentMap()) {
            if(content.isEmpty()) continue;

            JsonObject data = new JsonObject();
            content.each(cont -> {
                JsonObject obj = new JsonObject();
                obj.add("type", cont.getClass().getName());
                getContent(cont, obj, false);

                String name = cont.toString();
                if(cont instanceof MappableContent mapCont) name = mapCont.name;
                data.add(name, obj);
            });
            try {
                modDirectory.child("UnitInfo").child(content.peek().getContentType().toString() + ".json").writeString(data.toString(Stringify.FORMATTED));
            } catch (Throwable e){
                Log.warn(e.getMessage());
            }
        }
        Log.info("JSON file is completely updated!");
        Core.app.exit();
    }
}
