package UnitInfo.core;

import arc.Core;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.*;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import org.hjson.*;

import java.lang.reflect.Field;

import static UnitInfo.SVars.modRoot;
public class ContentJSON {
    public static void createFile() {
        save();
    }

    public static void save() {
        for(Seq<Content> content : Vars.content.getContentMap()) {
            if(!content.contains(cont -> cont instanceof UnlockableContent)) continue;
            JsonObject data = new JsonObject();
            content.each(cont -> {
                UnlockableContent type = (UnlockableContent) cont;
                JsonObject obj = new JsonObject();
                Seq<Field> seq = new Seq<Field>(type.getClass().getFields());
                seq.reverse();
                obj.add("type", type.getClass().getName());
                for(Field field : seq){
                    try {
                        String str = field.getName();
                        Object object = field.get(type);
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
                data.add(type.localizedName, obj);
            });
            try {
                modRoot.child(content.peek().getContentType().toString() + ".hjson").writeString(data.toString(Stringify.HJSON));
            } catch (Throwable t){

            }
        }
        Log.info("JSON file is completely generated!");
        Core.app.exit();
    }
}
