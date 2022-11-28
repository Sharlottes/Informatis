package informatis.ui.windows;

import arc.Events;
import arc.scene.Element;
import arc.struct.Seq;
import informatis.draws.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import informatis.ui.components.OverScrollPane;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

import static arc.Core.bundle;
import static arc.Core.settings;

public class ToolWindow extends Window {
    Vec2 scrollPos = new Vec2(0, 0);
    OverDrawCategory selected;
    float heat;

    public ToolWindow() {
        super(Icon.edit, "tool");
        only = true;

        Events.run(EventType.Trigger.update, () -> {
            heat += Time.delta;
            if(heat >= 60f) {
                heat = 0f;
                ((ScrollPane) find("tool-pane")).setWidget(rebuild());
            }
        });
    }

    @Override
    public void buildBody(Table table) {
        scrollPos = new Vec2(0, 0);

        table.background(Styles.black8)
            .top()
            .left();
        table.table(pane -> {
            pane.add(new OverScrollPane(rebuild(), Styles.noBarPane, scrollPos).disableScroll(true, false)).name("tool-pane");
        })
        .top()
        .marginLeft(4f)
        .marginRight(12f);
        table.table(stats -> {
            stats.top();
            stats.add(rebuildStats()).name("tool-stats");
        }).growY();
    }

    Table rebuild() {
        return new Table(icons -> {
            for(OverDrawCategory category : OverDrawCategory.values()) {
                icons.button(category.icon, () -> {
                    selected = category;
                    Table table = find("tool-stats");
                    table.clearChildren();
                    table.add(rebuildStats());
                })
                .grow()
                .tooltip(t -> t
                    .background(Styles.black8)
                    .add(category.name)
                    .color(Pal.accent)
                )
                .row();
            }
        });
    }

    Table rebuildStats() {
        return new Table(tool -> {
            if(selected == null) return;
            tool.top();
            tool.table(Tex.underline2, label ->
                label.add(selected.name).color(Pal.accent)
            ).row();
            tool.table(desc -> {
                desc.background(Styles.squarei.up).left();
                for(OverDraw draw : OverDraws.getDraws().get(selected, new Seq<>())) {
                    desc.check(bundle.get("setting."+draw.name+".name"), settings.getBool(draw.name), b->settings.put(draw.name, b))
                        .tooltip(t -> {
                            t.background(Styles.black8).add(bundle.get("setting."+draw.name+".description"));
                        })
                        .disabled(!selected.enabled)
                        .left()
                        .row();
                }
            })
                .name("unit-stats")
                .left()
                .row();
            tool.check("@mod.enable", selected.enabled, c -> {
                selected.enabled = c;
                Table table = find("unit-stats");
                for (Element elem : table.getChildren()) {
                    if (elem instanceof CheckBox cb) cb.setDisabled(!c);
                }
            });
        });
    }
}