package UnitInfo.ui.windows;

import UnitInfo.SVars;
import arc.scene.Element;
import arc.scene.style.*;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.Tex;
import mindustry.ui.Styles;

import static UnitInfo.SVars.modUiScale;
import static arc.Core.*;
import static mindustry.Vars.*;

public class CoreDisplay extends Table {
    static float itemScrollPos, heat;
    static Table table = new Table();

    public CoreDisplay() {
        fillParent = true;
        visibility = () -> 2 == SVars.hud.uiIndex;

        left().defaults().height(35f * 8f * Scl.scl(modUiScale));
        table(Tex.button, t -> {
            ScrollPane pane = t.pane(Styles.nonePane, rebuild()).get();
            pane.update(() -> {
                Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                if(pane.hasScroll() && (result == null || !result.isDescendantOf(pane)))
                    scene.setScrollFocus(null);
                itemScrollPos = pane.getScrollY();
            });
            pane.setOverscroll(false, false);
            pane.setScrollingDisabled(true, false);
            pane.setScrollYForce(itemScrollPos);

            t.update(() -> {
                NinePatchDrawable patch = (NinePatchDrawable)Tex.button;
                t.setBackground(patch.tint(Tmp.c1.set(patch.getPatch().getColor()).a(settings.getInt("uiopacity") / 100f)));
            });
        }).padRight(Scl.scl(modUiScale) * 39 * 8f);
    }

    public void setEvent() {
        heat += Time.delta;

        if(heat > 60f) {
            heat = 0f;
            rebuild();
        }
    }

    public Table rebuild() {
        table.clear();
        table.table(t -> {
            for(int i = 0; i < CoresItemsDisplay.tables.size; i++){
                if((state.rules.pvp && CoresItemsDisplay.teams[i] != player.team()) || CoresItemsDisplay.teams[i].cores().isEmpty()) continue;
                int finalI = i;
                t.table(tt -> {
                    tt.center().defaults().width(Scl.scl(modUiScale) * 44 * 8f);
                    CoresItemsDisplay.tables.get(finalI).setBackground(((NinePatchDrawable)Tex.underline2).tint(CoresItemsDisplay.teams[finalI].color));
                    tt.add(CoresItemsDisplay.tables.get(finalI)).left();
                }).pad(4);
                t.row();
            }
        });

        return table;
    }
}
