package informatis.ui.fragments.sidebar.windows;

import arc.graphics.Color;
import arc.scene.ui.CheckBox;
import arc.scene.ui.ScrollPane;
import informatis.ui.fragments.sidebar.windows.tools.draws.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import informatis.ui.components.PageTabsFragment;
import informatis.ui.fragments.sidebar.windows.tools.tools.ToolManager;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

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

                    //set current corrination to next point so that scroll position is not end of this element.
                    anchorPoints[Math.min(i + 1, anchorPoints.length - 1)] = anchorPoints[i] + bodyTable.table(categoryTable -> {
                        categoryTable.top().left().defaults().growX().left();
                        categoryTable.add(category.name).color(Pal.accent).labelAlign(Align.left);
                        categoryTable.row();
                        categoryTable.image().color(Color.gray).height(2f).pad(8, 0, 8, 0);
                        categoryTable.row();
                        categoryTable.table(configListTable -> {
                            configListTable.top().left().defaults().left().grow();
                            for (ToolConfigable toolConfigable : OverDrawManager.draws.get(category)) {
                                configListTable.add(buildConfigTable(toolConfigable));
                                configListTable.row();
                            }
                        });
                    }).marginBottom(32f).prefHeight();
                    bodyTable.row();
                }
            }).growX().get();
        });
    }

    private Table rebuildToolsTable() {
        return new Table(mainTable -> {
            mainTable.pane(Styles.smallPane, bodyTable -> {
                bodyTable.top().left().defaults().top().left().grow();
                for (ToolConfigable toolConfigable : ToolManager.tools) {
                    bodyTable.add(buildConfigTable(toolConfigable));
                    bodyTable.row();
                }
            }).grow();
        });
    }

    private Table buildConfigTable(ToolConfigable toolConfigable) {
        return new Table(configTable -> {
            configTable.top().left().defaults().left().grow();
            configTable.button(bundle.get("setting." + toolConfigable.getName() + ".name"), Styles.flatToggleMenut, () -> toolConfigable.setEnabled(!toolConfigable.isEnabled()))
                    .tooltip(t -> {
                        t.background(Styles.black8).add(bundle.get("setting." + toolConfigable.getName() + ".description"));
                    }).checked(toolConfigable.isEnabled()).padBottom(4).get().getLabelCell().labelAlign(Align.left).pad(8, 16, 8, 8).get().setFontScale(0.9f);
            configTable.row();
            if(toolConfigable.getSubConfigs().length == 0) return;
            configTable.table(subConfigTable -> {
                subConfigTable.left().defaults().left().padBottom(4).labelAlign(Align.left);

                for (ToolConfigable subConfigable : toolConfigable.getSubConfigs()) {
                    CheckBox checkBox = subConfigTable.check(bundle.get("setting." + subConfigable.getName() + ".name"), subConfigable.isEnabled(), subConfigable::setEnabled)
                            .tooltip(t -> {
                                t.background(Styles.black8).add(bundle.get("setting." + subConfigable.getName() + ".description"));
                            }).disabled(x -> !toolConfigable.isEnabled()).get();
                    checkBox.getLabel().setFontScale(0.8f);
                    checkBox.getImage().setScale(0.7f);
                    subConfigTable.row();
                }
            }).pad(4, 24, 16, 4);
        });
    }
}