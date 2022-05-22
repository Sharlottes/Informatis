package informatis.ui.display;

import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.ui.*;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import static informatis.SUtils.*;
import static informatis.SVars.*;
import static arc.Core.*;
import static mindustry.Vars.*;

public class WaveInfoDisplay {
    public boolean waveShown;

    public void addWaveInfoTable() {
        Table waveInfoTable = new Table(Tex.buttonEdge4, table -> {
            table.center();
            table.table(head -> {
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
            table.row();
            table.image().height(4f).color(Pal.gray).growX().row();
            /*
            table.table(tttt -> {
                tttt.center();
                int[] i = {0};

                content.units().each(type -> Groups.unit.contains(u -> u.type == type && (state.rules.pvp ? (u.team != player.team()) : (u.team == state.rules.waveTeam)) && u.isBoss()), type -> {
                    tttt.table(stt ->
                            stt.stack(
                                    new Table(ttt -> ttt.image(type.uiIcon).size(iconSmall)),
                                    new Table(ttt -> {
                                        ttt.right().bottom();
                                        Label label = new Label(() -> Groups.unit.count(u -> u.type == type && (state.rules.pvp ? (u.team != player.team()) : (u.team == state.rules.waveTeam)) && u.isBoss()) + "");
                                        label.setFontScale(0.75f);
                                        ttt.add(label);
                                        ttt.pack();
                                    }),
                                    new Table(ttt -> {
                                        ttt.top().right();
                                        Image image = new Image(Icon.warning.getRegion()).setScaling(Scaling.fit);
                                        image.update(() -> image.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f))));
                                        ttt.add(image).size(12f);
                                        ttt.pack();
                                    })
                            ).pad(6)
                    );
                    if(++i[0] % 6 == 0) tttt.row();
                });
                tttt.row();
                i[0] = 0;
                content.units().each(type -> Groups.unit.contains(u -> u.type == type && (state.rules.pvp ? (u.team != player.team()) : (u.team == state.rules.waveTeam)) && !u.isBoss()), type -> {
                    tttt.table(ttt ->
                            ttt.add(new Stack() {{
                                add(new Table(ttt -> ttt.add(new Image(type.uiIcon)).size(iconSmall)));
                                add(new Table(ttt -> {
                                    ttt.right().bottom();
                                    Label label = new Label(() -> Groups.unit.count(u -> u.type == type &&(state.rules.pvp ? (u.team != player.team()) : (u.team == state.rules.waveTeam)) && !u.isBoss()) + "");
                                    label.setFontScale(0.75f);
                                    ttt.add(label);
                                    ttt.pack();
                                }));
                            }}).pad(6)
                    );
                    if(++i[0] % 6 == 0) tttt.row();
                });
            });
             */
        });

        Table waveTable = (Table) scene.find("waves");
        Table infoTable = (Table) scene.find("infotable");
        waveTable.removeChild(infoTable);
        waveTable.row();
        waveTable.stack(
            new Table(tt -> tt.collapser(t -> t.stack(waveInfoTable, infoTable).growX(), true, () -> waveShown).growX()).top(),
            new Table(tt -> tt.button(Icon.downOpen, Styles.clearTogglei, () -> waveShown = !waveShown).size(4 * 8f).checked(b -> {
                b.getImage().setDrawable(waveShown ? Icon.upOpen : Icon.downOpen);
                return waveShown;
            })).left().top()).fillX();
    }
}
