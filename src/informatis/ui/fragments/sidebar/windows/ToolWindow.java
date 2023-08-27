package informatis.ui.fragments.sidebar.windows;

import arc.graphics.Color;
import arc.scene.ui.ScrollPane;
import informatis.draws.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import informatis.ui.components.PageTabsFragment;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

import java.util.concurrent.atomic.AtomicReference;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ToolWindow extends Window {
    private final PageTabsFragment tabsFragment = new PageTabsFragment(
            "OverDraw", buildOverDrawTable(),
            "Tools", rebuildToolsTable()
    );

    public ToolWindow() {
        super(Icon.edit, "tool");
        disableRootScroll = true;
        height = 300;
        width = 300;
    }

    @Override
    public void buildBody(Table table) {
        table.background(Styles.black8);
        table.add(tabsFragment).growX();
        table.row();
        table.add(tabsFragment.content).grow();
    }

    private Table buildOverDrawTable() {
        final ScrollPane[] bodyScroll = new ScrollPane[1];
        final OverDrawCategory[] categories = OverDrawCategory.values();
        final float[] anchorPoints = new float[categories.length];

        return new Table(mainTable -> {
            mainTable.top().left().defaults().top();
            mainTable.table(sidebarTable -> {
                sidebarTable.top();
                for (int i = 0; i < categories.length; i++) {
                    final int j = i;
                    final OverDrawCategory category = categories[i];
                    sidebarTable.button(category.icon, Styles.cleari, () -> {
                        if(bodyScroll[0] == null) return;
                        bodyScroll[0].setScrollY(anchorPoints[j]);
                    }).size(iconLarge);
                    sidebarTable.row();
                }
            }).padRight(2 * 8f);
             bodyScroll[0] = mainTable.pane(Styles.smallPane, bodyTable -> {
                 bodyTable.top().left().margin(12).defaults().growX();
                for (int i = 0; i < categories.length; i++) {
                    final OverDrawCategory category = categories[i];

                    anchorPoints[Math.min(i + 1, anchorPoints.length - 1)] = anchorPoints[i] + bodyTable.table(sectionTable -> {
                        sectionTable.top().left().defaults().growX().left();
                        sectionTable.add(category.name).color(Pal.accent).labelAlign(Align.left);
                        sectionTable.row();
                        sectionTable.image().color(Color.gray).height(2f).pad(8, 0, 8, 0);
                        sectionTable.row();
                        sectionTable.table(desc -> {
                            desc.top().left().defaults().left().labelAlign(Align.left);
                            for (OverDraw draw : OverDraws.draws.get(category)) {
                                desc.check(bundle.get("setting." + draw.name + ".name"), draw.isEnabled(), draw::setEnabled)
                                        .tooltip(t -> {
                                            t.background(Styles.black8).add(bundle.get("setting." + draw.name + ".description"));
                                        });
                                desc.row();
                            }
                        });
                    }).marginBottom(32f).prefHeight();
                    bodyTable.row();
                }
            }).growX().get();
        });
    }

    private Table rebuildToolsTable() {
        return new Table();
    }
}