package UnitInfo.ui.windows;

import UnitInfo.ui.OverScrollPane;
import UnitInfo.ui.Updatable;
import UnitInfo.ui.draws.OverDraw;
import UnitInfo.ui.draws.OverDraws;
import UnitInfo.ui.draws.UnitDraw;
import arc.math.Scaled;
import arc.math.geom.Vec2;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import arc.util.Time;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

public class ToolDisplay extends WindowTable implements Updatable {
    Vec2 scrollPos = new Vec2(0, 0);
    OverDraw selected;
    float heat;

    public ToolDisplay() {
        super("Tool Display", Icon.edit, t -> {
        });
    }

    @Override
    public void build() {
        scrollPos = new Vec2(0, 0);

        top();
        topBar();

        table(Styles.black8, t -> {
            t.left();
            t.table(pane->{
                pane.add(new OverScrollPane(rebuild(), Styles.nonePane, scrollPos).disableScroll(true, false)).name("tool-pane");
            }).top().marginLeft(4f).marginRight(12f);
            t.table(stats->{
                stats.top();
                stats.add(rebuildStats()).name("tool-stats");
            }).growY();
        }).top().right().grow().get().parent = null;

        resizeButton();
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