package informatis.ui.fragments.sidebar.windows;

import arc.Events;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Angles;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import informatis.draws.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.entities.Units;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.Ranged;
import mindustry.ui.*;
import mindustry.world.blocks.ControlBlock;
import mindustry.world.blocks.defense.turrets.Turret;

import static arc.Core.*;
import static arc.Core.input;
import static mindustry.Vars.*;

public class ToolWindow extends Window {
    private OverDrawCategory selected = OverDrawCategory.values()[0];
    private Table mainTable;
    private Teamc shotTarget;

    public ToolWindow() {
        super(Icon.edit, "tool");
        height = 300;
        width = 300;

    }

    @Override
    public void buildBody(Table table) {
        table.background(Styles.black8).top().left().margin(12)
                .defaults().growY().top();
        table.pane(Styles.noBarPane, rebuildSideTab()).scrollY(true)
                .padRight(2 * 8f).growY();
        mainTable = table.add(rebuildMain()).growX().get();
    }

    private Table rebuildSideTab() {
        return new Table(icons -> {
            icons.top();
            for(OverDrawCategory category : OverDrawCategory.values()) {
                final OverDrawCategory catCategory = category;
                icons.button(catCategory.icon, Styles.clearTogglei, () -> {
                    selected = catCategory;
                    mainTable.clearChildren();
                    mainTable.add(rebuildMain());
                }).size(iconLarge).checked((x) -> selected == catCategory);
                icons.row();
            }
        });
    }

    private Table rebuildMain() {
        return new Table(tool -> {
            tool.top().left().defaults().growX().left();
            tool.add(selected.name).color(Pal.accent).labelAlign(Align.left);
            tool.row();
            tool.image().color(Color.gray).height(4f).pad(4, 0, 4, 0);
            tool.row();
            tool.table(desc -> {
                desc.top().left().defaults().left().labelAlign(Align.left);
                for(OverDraw draw : OverDraws.draws.get(selected)) {
                    desc.check(bundle.get("setting."+draw.name+".name"), draw.isEnabled(), draw::setEnabled)
                        .tooltip(t -> {
                            t.background(Styles.black8).add(bundle.get("setting."+draw.name+".description"));
                        });
                    desc.row();
                }
            });
        });
    }
}