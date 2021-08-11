package UnitInfo;

import arc.graphics.g2d.NinePatch;
import arc.graphics.g2d.TextureAtlas;
import arc.scene.style.Drawable;
import arc.scene.style.ScaledNinePatchDrawable;
import arc.util.Strings;
import mindustry.core.UI;

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
}
