package informatis.ui.window;

import informatis.ui.*;
import informatis.ui.draws.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

public class ToolWindow extends Window implements Updatable {
    Vec2 scrollPos = new Vec2(0, 0);
    OverDraw selected;
    float heat;

    public ToolWindow() {
        super(Icon.edit, "tool");
    }

    @Override
    public void build(Table table) {
        scrollPos = new Vec2(0, 0);

        table.background(Styles.black8).top().left();
        table.table(pane->{
            pane.add(new OverScrollPane(rebuild(), Styles.noBarPane, scrollPos).disableScroll(true, false)).name("tool-pane");
        }).top().marginLeft(4f).marginRight(12f);
        table.table(stats->{
            stats.top();
            stats.add(rebuildStats()).name("tool-stats");
        }).growY();
    }

    @Override
    public void update() {
        heat += Time.delta;
        if(heat >= 60f) {
            heat = 0f;
            ScrollPane pane = find("tool-pane");
            pane.setWidget(rebuild());
        }
    }

    Table rebuild() {
        return new Table(icons->{
            for(OverDraw draw : OverDraws.all) {
                icons.button(draw.icon, ()->{
                    selected=draw;
                    Table table = find("tool-stats");
                    table.clearChildren();
                    table.add(rebuildStats());
                }).grow().tooltip(t->t.background(Styles.black8).add(draw.name).color(Pal.accent)).row();
            }
        });
    }

    Table rebuildStats() {
        return new Table(tool->{
            if(selected==null) return;
            tool.top();
            tool.table(Tex.underline2, label->label.add(selected.name).color(Pal.accent)).row();
            tool.table(des-> selected.displayStats(des)).name("unit-stats").row();
            tool.check("enable", selected.enabled, c->{
                selected.enabled=c;
                selected.onEnabled(find("unit-stats"));
            });
        });
    }
}