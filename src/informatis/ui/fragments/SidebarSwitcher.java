package informatis.ui.fragments;

import arc.*;
import arc.graphics.g2d.Lines;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.Pal;

public class SidebarSwitcher {
    private int showIndex = 0;
    private final Table sidebarTable = new Table();
    private Table[] sidebars;

    private final ImageButton switchButton = new ImageButton(style) {
        @Override
        public void draw() {
            super.draw();
            Lines.stroke(5, Pal.gray);
            Lines.line(this.x, this.y + this.height, this.x + this.width, this.y + this.height);
        }
     {
        clicked(() -> {
            Element currentSidebar = sidebars[showIndex];
            showIndex = (showIndex + 1) % sidebars.length;
            Element nextSidebar = sidebars[showIndex];

            actShowMoveX(currentSidebar, 0, -currentSidebar.getWidth());
            actShowMoveX(nextSidebar, -nextSidebar.getWidth(),0);
            actResizeWidth(this, nextSidebar.getWidth());
        });
    }};
    private static final ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle() {{
        up = Tex.buttonEdge4;
        imageUp = Icon.right;
    }};

    public SidebarSwitcher(Table ...sidebars) {
        this.sidebars = sidebars;
        rebuildSidebarTable();

        Vars.ui.hudGroup.fill(t -> {
            t.name = "informatis sidebar";
            t.left();
            t.add(sidebarTable);
        });
    }

    private static void actShowMoveX(Element element, float from, float to) {
        MoveToAction moveToAction = new MoveToAction();
        moveToAction.setDuration(0.2f);
        moveToAction.setX(to);
        moveToAction.setInterpolation(Interp.circleOut);
        VisibleAction visibleAction = new VisibleAction();
        visibleAction.setVisible(to >= 0);

        element.setPosition(from, element.y);
        if(to >= 0) element.actions(visibleAction, moveToAction);
        else element.actions(moveToAction, visibleAction);
        element.act(Core.graphics.getDeltaTime());
        element.draw();
    }

    private static void actResizeWidth(Element element, float width) {
        SizeToAction sizeToAction = new SizeToAction();
        sizeToAction.setSize(width, element.getHeight());
        sizeToAction.setDuration(0.2f);
        sizeToAction.setInterpolation(Interp.circleOut);
        element.actions(sizeToAction);
        element.act(Core.graphics.getDeltaTime());
        element.draw();
    }

    public void rebuildSidebarTable() {
        sidebarTable.visible = Core.settings.getBool("sidebar");
        if(!sidebarTable.visible) return;

        Stack sidebarTables = new Stack();
        for(Table elem : sidebars) {
            sidebarTables.add(new Table(table -> {
                table.left();
                table.add(elem).growY();
            }));
            elem.setBackground(Tex.buttonEdge3);
            elem.visible = false;
        }
        sidebars[showIndex].visible = true;
        actResizeWidth(switchButton, sidebars[showIndex].getWidth());

        sidebarTable.clear();
        sidebarTable.top().left();
        sidebarTable.add(sidebarTables).grow();
        sidebarTable.row();
        sidebarTable.add(switchButton).growX();
    }
}