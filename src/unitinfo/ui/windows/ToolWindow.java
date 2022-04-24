package unitinfo.ui.windows;

import unitinfo.ui.OverScrollPane;
import unitinfo.ui.Updatable;
import unitinfo.ui.draws.OverDraw;
import unitinfo.ui.draws.OverDraws;
import arc.math.geom.Vec2;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

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
            pane.add(new OverScrollPane(rebuild(), Styles.nonePane, scrollPos).disableScroll(true, false)).name("tool-pane");
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