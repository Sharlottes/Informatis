package UnitInfo;

import arc.graphics.g2d.*;
import arc.scene.style.*;
import arc.util.Strings;
import mindustry.core.UI;

import java.lang.reflect.*;

public class SUtils {
    public static Drawable getDrawable(TextureAtlas.AtlasRegion region, int left, int right, int top, int bottom){
        int[] splits = {left, right, top, bottom};
        int[] pads = region.pads;
        NinePatch patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
        if(pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);

        return new ScaledNinePatchDrawable(patch, 1);
    }

    public static String floatFormat(float number){
        if(number >= 10000) return UI.formatAmount((long)number);
        if(String.valueOf(number).split("[.]")[1].matches("0")) return String.valueOf(number).split("[.]")[0];
        return Strings.fixed(number, 1);
    }

    public static Object getFinalStatic(Field field) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        return field.get(null);
    }
}
