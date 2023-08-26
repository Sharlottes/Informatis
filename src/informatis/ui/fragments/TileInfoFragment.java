package informatis.ui.fragments;

import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import static informatis.SUtils.*;
import static informatis.SVars.*;
import static arc.Core.*;
import static mindustry.Vars.*;

public class TileInfoFragment {
    private boolean waveShown;
    Table tileInfoTable = new Table(Tex.buttonEdge4);
    Stack tileInfoContainer;

    public TileInfoFragment() {
        Table waveTable = (Table) scene.find("waves");
        Table infoTable = (Table) scene.find("infotable");
        waveTable.removeChild(infoTable);
        waveTable.row();
        tileInfoContainer = waveTable.stack(
            new Table(tt -> tt.collapser(t -> t.stack(tileInfoTable, infoTable).growX(), true, () -> waveShown).growX()).top(),
            new Table(tt -> tt.button(Icon.downOpen, Styles.clearTogglei, () -> waveShown = !waveShown).size(4 * 8f).checked(b -> {
                b.getImage().setDrawable(waveShown ? Icon.upOpen : Icon.downOpen);
                return waveShown;
            })).left().top()
        ).fillX().get();

        rebuildTileInfoTable();
    }

    public void rebuildTileInfoTable() {
        tileInfoContainer.visible = settings.getBool(("tileinfo"));
        if(!tileInfoContainer.visible) return;
        tileInfoTable.clear();
        tileInfoTable.center();
        tileInfoTable.table(head -> {
            head.table(image -> {
                image.left();
                image.image(() -> {
                    Tile tile = getTile();
                    if(tile == null) return clear;
                    Floor floor = tile.floor();
                    if(floor.uiIcon == error) return clear;
                    return floor.uiIcon;
                }).size(iconSmall);
                image.image(() -> {
                    Tile tile = getTile();
                    if(tile == null) return clear;
                    Floor floor = tile.overlay();
                    if(floor.uiIcon == error) return clear;
                    return floor.uiIcon;
                }).size(iconSmall);
                image.image(() -> {
                    Tile tile = getTile();
                    if(tile == null) return clear;
                    Block floor = tile.block();
                    if(floor.uiIcon == error) return clear;
                    return floor.uiIcon;
                }).size(iconSmall);
            });
            head.label(() -> {
                Tile tile = getTile();
                if(tile == null) return "(NaN, NaN)";
                return Strings.format("(@, @)", tile.x, tile.y);
            }).center();
        });
    }
}
