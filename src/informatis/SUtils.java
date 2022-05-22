package informatis;

import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.scene.style.*;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.UnitType;
import mindustry.type.weapons.*;
import mindustry.world.Tile;

import java.lang.reflect.*;

import static informatis.SVars.*;
import static arc.Core.*;
import static mindustry.Vars.player;

public class SUtils {
    @SuppressWarnings("unchecked")
    public static <T extends Teamc> T getTarget(){
        if(locked && target != null) {
            if(settings.getBool("deadTarget") && !Groups.all.contains(e -> e == target)) {
                target = player.unit();
                locked = false;
            }
            else return (T) target; //if there is locked target, return it first.
        }

        Seq<Unit> units = Groups.unit.intersect(input.mouseWorldX(), input.mouseWorldY(), 4, 4); // well, 0.5tile is enough to search them
        if(units.size > 0)
            return (T) units.peek(); //if there is unit, return it.
        else if(getTile() != null && getTile().build != null)
            return (T) getTile().build; //if there isn't unit but there is build, return it.
        else if(player.unit() instanceof BlockUnitUnit b && b.tile() != null)
            return (T)b.tile();
        return (T) player.unit(); //if there aren't unit and not build, return player.
    }

    @Nullable
    public static Tile getTile(){
        return Vars.world.tileWorld(input.mouseWorldX(), input.mouseWorldY());
    }

    public static Drawable getDrawable(TextureAtlas.AtlasRegion region, int left, int right, int top, int bottom){
        int[] splits = {left, right, top, bottom};
        int[] pads = region.pads;
        NinePatch patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
        if(pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);

        return new ScaledNinePatchDrawable(patch, 1);
    }

    public static <T extends Number> String formatNumber(T number){
        return formatNumber(number, 1);
    }
    public static <T extends Number> String formatNumber(T number, int step){
        if(number.intValue() >= 1000) return UI.formatAmount(number.longValue());
        return Strings.fixed(number.floatValue(), step);
    }

    public static float bulletRange(BulletType b) {
        float a = 0;
        float n = 1;
        for (int i = 0; i < b.lifetime; i++) {
            a += n;
            n *= (1 - b.drag);
        };
        a += n;
        a /= b.lifetime;
        return b.speed * a * Mathf.pow(1 - b.drag, b.lifetime / 2) * b.lifetime +
                Math.max(b.lightning > 0 || b instanceof LightningBulletType ? (b.lightningLength + b.lightningLengthRand) * 6 : 0,
                        b.fragBullet != null ? bulletRange(b.fragBullet) * b.fragLifeMax * b.fragVelocityMax : b.splashDamageRadius);
    }

    public static float unitRange(UnitType u) {
        final float[] mrng = {0};
        u.weapons.each(w -> w.bullet != null, w -> {
                mrng[0] = Math.max(mrng[0], (w instanceof RepairBeamWeapon || w instanceof PointDefenseWeapon) ? 0 : bulletRange(w.bullet));
                if(mrng[0] == 0) mrng[0] = w.bullet.range;
        });
        return mrng[0];
    }

    public static Object invoke(Object ut, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        Field field = ut.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(ut);
    }


    public static boolean isOutCamera(float x, float y) {
        return !isInCamera(x, y, 0);
    }

    public static boolean isInCamera(float x, float y, float size) {
        Tmp.r2.setCentered(x, y, size);
        return Tmp.r1.overlaps(Tmp.r2);
    }
}
