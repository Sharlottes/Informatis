package UnitInfo.core;

import arc.Core;
import arc.struct.Seq;
import arc.util.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.type.*;
import mindustry.world.*;
import org.hjson.*;

import java.lang.reflect.*;

import static mindustry.Vars.modDirectory;

public class ContentJSON {
    static JsonValue parse(Object object) {
        if(object instanceof Integer val) return JsonObject.valueOf(val);
        else if(object instanceof Double val) return JsonObject.valueOf(val);
        else if(object instanceof Float val) return JsonObject.valueOf(val);
        else if(object instanceof Long val) return JsonObject.valueOf(val);
        else if(object instanceof String val) return JsonObject.valueOf(val);
        else if(object instanceof Boolean val) return JsonObject.valueOf(val);
        else if(object instanceof Content) {
            if(object instanceof Block c) return getContent(c, Block.class, new JsonObject());
            if(object instanceof BulletType c) return getContent(c, BulletType.class, new JsonObject());
            if(object instanceof Item c) return getContent(c, Item.class, new JsonObject());
            if(object instanceof Liquid c) return getContent(c, Liquid.class, new JsonObject());
            if(object instanceof UnitType c) return getContent(c, UnitType.class, new JsonObject());
            if(object instanceof Weather c) return getContent(c, Weather.class, new JsonObject());
        }
        else if(object instanceof Weapon val) return getContent(val, new JsonObject());
        else if(object instanceof Ability val) return getContent(val, new JsonObject());
        else if(object instanceof Seq seq && seq.any()) {
            JsonArray array = new JsonArray();
            for(int i = 0; i < seq.size; i++) {
                if(seq.get(i) != null) array.add(parse(seq.get(i)));
            }
            return array;
        }
        else {
            if(object.getClass().isArray()) {
                JsonArray array = new JsonArray();
                for(int i = 0; i < Array.getLength(object); i++) {
                    if(Array.get(object, i) != null) array.add(parse(Array.get(object, i)));
                }
                return array;
            }
        }
        return JsonObject.valueOf(object.toString());
    }

    static <T extends Object> JsonObject getContent(T cont, JsonObject obj) {
        return getContent(cont, cont.getClass(), obj);
    }

    static JsonObject getContent(Object cont, Class objClass, JsonObject obj) {
        obj.add("type", objClass.getName());
        for(Field field : objClass.getFields()){
            if(Modifier.isStatic(field.getModifiers())) continue;
            try {
                String name = field.getName();
                Object object = field.get(cont);
                Object preval = obj.get(name);
                if(preval != null) obj.set(name, preval + " or " + object);
                else {
                    if(object == null) obj.add(name, "null");
                    else if(!cont.getClass().isAssignableFrom(field.get(cont).getClass()) && !field.get(cont).getClass().isAssignableFrom(cont.getClass())){
                        obj.add(name, parse(object));
                    }
                }
            } catch(Throwable e) {
                try {
                    Log.info(e + " ### " + cont + " ### " + objClass + " ### " + cont.getClass() + " ### " + field.get(cont));
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
                obj.add(field.getName(), "### ERROR ###");
            }
        }

        return obj;
    }

    static void save() {
        for(Seq<Content> content : Vars.content.getContentMap()) {
            if(content.isEmpty()) continue;

            JsonObject data = new JsonObject();
            content.each(cont -> {
                JsonObject obj = new JsonObject();
                getContent(cont, obj);

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
