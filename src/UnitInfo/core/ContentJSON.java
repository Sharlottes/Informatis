package UnitInfo.core;

import arc.Core;
import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.*;
import mindustry.ctype.Content;
import mindustry.ctype.MappableContent;
import org.hjson.*;

import java.lang.reflect.Field;

import static mindustry.Vars.modDirectory;

public class ContentJSON {
    public static void save() {
        for(Seq<Content> content : Vars.content.getContentMap()) {
            JsonObject data = new JsonObject();
            content.each(cont -> {
                JsonObject obj = new JsonObject();
                Seq<Field> seq = new Seq<Field>(cont.getClass().getFields());
                seq.reverse();
                obj.add("type", cont.getClass().getName());
                for(Field field : seq){
                    try {
                        String str = field.getName();
                        Object object = field.get(cont);
                        if(object instanceof Integer val) obj.add(str, val);
                        if(object instanceof Long val) obj.add(str, val);
                        if(object instanceof Float val) obj.add(str, val);
                        if(object instanceof String val) obj.add(str, val);
                        if(object instanceof Double val) obj.add(str, val);
                        if(object instanceof Boolean val) obj.add(str, val);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                String name = cont.toString();
                if(cont instanceof MappableContent mapCont) name = mapCont.name;
                data.add(name, obj);
            });
            try {
                modDirectory.child("UnitInfo").child(content.peek().getContentType().toString() + ".json").writeString(data.toString(Stringify.FORMATTED));
            } catch (Throwable t){

            }
        }
        Log.info("JSON file is completely updated!");
        Core.app.exit();
    }
}
