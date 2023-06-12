package informatis;

import arc.Core;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.geom.Position;
import arc.scene.style.*;
import arc.struct.Seq;
import arc.util.*;
import informatis.core.Pathfinder;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.input.DesktopInput;
import mindustry.world.Tile;

import java.util.Iterator;

import static arc.Core.*;
import static mindustry.Vars.*;

public class SUtils {
    /**
     * loop iterator with "final i", which can be referenced in callback scope.
     * @param iterator - just an iterator to loop.
     * @param callback - callback function with "i" parameter.
     */
    public static <T> void loop(Iterator<T> iterator, Cons2<T, Integer> callback) {
        SUtils.loop(iterator, (t, i) -> {
            callback.get(t, i);
            return false;
        });
    }
    public static <T> void loop(Iterator<T> iterator, Func2<T, Integer, Boolean> callback) {
        int i = 0;
        while(iterator.hasNext()) {
            if(callback.get(iterator.next(), i)) break;
            i++;
        }
    }

    /**
     * loop array with "final i", which can be referenced in callback scope.
     * @param array - just an array object to loop.
     * @param callback - callback function with "i" parameter.
     */
    public static <T> void loop(T[] array, Cons2<T, Integer> callback) {
        SUtils.loop(array, (t, i) -> {
            callback.get(t, i);
            return false;
        });
    }
    public static <T> void loop(T[] array, Func2<T, Integer, Boolean> callback) {
        for(int i = 0; i < array.length; i++) {
            if(callback.get(array[i], i)) break;
        }
    }

    /**
     * loop iterable object with "final i", which can be referenced in callback scope.
     * @param iterable - just an iterable object to loop.
     * @param callback - callback function with "i" parameter.
     */
    public static <T> void loop(Iterable<T> iterable, Cons2<T, Integer> callback) {
        SUtils.loop(iterable, (t, i) -> {
            callback.get(t, i);
            return false;
        });
    }
    public static <T> void loop(Iterable<T> iterable, Func2<T, Integer, Boolean> callback) {
        int i = 0;
        for(T t : iterable) {
            if(callback.get(t, i)) break;
            i++;
        }
    }

    /**
     * for-loop with "final i", which can be referenced in callback scope.
     * @param number - max loop count number.
     * @param callback - callback function with "i" parameter.
     */
    public static void loop(int number, Cons<Integer> callback) {
        SUtils.loop(number, (i) -> {
            callback.get(i);
            return false;
        });
    }
    public static void loop(int number, Func<Integer, Boolean> callback) {
        for(int i = 0; i < number; i++) {
            if(callback.get(i)) break;
        }
    }

    public static <T, RT> RT[] pickFromArray(T[] array, Class<RT> returnType, Boolf<Integer> condition) {
        Seq<RT> list = new Seq<>();
        for(int i = 0; i < array.length; i++) {
            if(condition.get(i)) list.add((RT) array[i]);
        }
        return list.toArray(returnType);
    }

    /**
     * move camera to given coordination
     * @param x world unit x
     * @param y world unit y
     */
    public static void moveCamera(float x, float y) {
        if(control.input instanceof DesktopInput)
            ((DesktopInput) control.input).panning = true;
        Core.camera.position.set(x, y);
    }
    /**
     * move camera to given coordination
     * @param pos world unit coordination
     */
    public static void moveCamera(Position pos) {
        moveCamera(pos.getX(), pos.getY());
    }
    @SuppressWarnings("unchecked")
    public static <T extends Teamc> T getTarget(){
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
        if(number instanceof Integer || number.longValue() % 10 == 0) return String.valueOf(number.intValue());
        return Strings.fixed(number.floatValue(), step);
    }

    public static Seq<Tile> generatePathTiles() {
        Seq<Tile> pathTiles = new Seq<>();

        spawner.getSpawns().each(tile -> pathTiles.addAll(generatePathTiles(tile)));

        return pathTiles;
    }
    public static Seq<Tile> generatePathTiles(Tile startTile) {
        Seq<Tile> pathTiles = new Seq<>();

        for(int p = 0; p < 3; p++) {
            getNextTile(startTile, SVars.pathfinder.getField(state.rules.waveTeam, p, Pathfinder.fieldCore), pathTiles);
        }

        return pathTiles;
    }

    public static Seq<Tile> generatePathTiles(Tile startTile, Team team, int type) {
        Seq<Tile> pathTiles = new Seq<>();

        getNextTile(startTile, SVars.pathfinder.getField(team, type, Pathfinder.fieldCore), pathTiles);

        return pathTiles;
    }

    static void getNextTile(Tile tile, Pathfinder.Flowfield field, Seq<Tile> pathTiles) {
        Tile nextTile = SVars.pathfinder.getTargetTile(tile, field);
        pathTiles.add(nextTile);
        if(nextTile == tile || nextTile == null) return;
        getNextTile(nextTile, field, pathTiles);
    }

    public static boolean isInCamera(float x, float y, float size) {
        Tmp.r2.setCentered(x, y, size);
        return Tmp.r1.overlaps(Tmp.r2);
    }
}
