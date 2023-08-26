package informatis.ui.fragments.sidebar.windows;

import arc.graphics.Color;
import arc.struct.Seq;
import informatis.draws.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

import static arc.Core.bundle;
import static arc.Core.settings;
import static mindustry.Vars.*;

public class ToolWindow extends Window {
    private OverDrawCategory selected = OverDrawCategory.values()[0];
    private Table mainTable;

    public ToolWindow() {
        super(Icon.edit, "tool");
    }

    @Override
    public void buildBody(Table table) {
        table.background(Styles.black8).top().left().margin(12)
                .defaults().growY().top();
        table.pane(Styles.noBarPane, rebuildSideTab()).scrollY(true)
                .padRight(2 * 8f).growY();
        mainTable = table.add(rebuildMain()).growX().get();
    }

    Table rebuildSideTab() {
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

    Table rebuildMain() {
        return new Table(tool -> {
            tool.top().left().defaults().growX().left();
            tool.add(selected.name).color(Pal.accent).labelAlign(Align.left);
            tool.row();
            tool.image().color(Color.gray).height(4f).pad(4, 0, 4, 0);
            tool.row();
            tool.table(desc -> {
                desc.top().left().defaults().left().labelAlign(Align.left);
                for(OverDraw draw : OverDraws.getDraws().get(selected, new Seq<>())) {
                    desc.check(bundle.get("setting."+draw.name+".name"), settings.getBool(draw.name), b -> settings.put(draw.name, b))
                        .tooltip(t -> {
                            t.background(Styles.black8).add(bundle.get("setting."+draw.name+".description"));
                        });
                    desc.row();
                }
            });
        });
    }
}