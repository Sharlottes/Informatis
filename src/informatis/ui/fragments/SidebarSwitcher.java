package informatis.ui.fragments;

import arc.*;
import arc.graphics.g2d.Lines;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.Log;
import mindustry.*;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.Pal;

public class SidebarSwitcher {
    private int showIndex = 0;
    private final Table sidebarTable = new Table();
    private final Table[] sidebars;

    private final ImageButton switchButton = new ImageButton() {
        @Override
        public void draw() {
            super.draw();
            Lines.stroke(5, Pal.gray);
            Lines.line(this.x, this.y + this.height, this.x + this.width, this.y + this.height);
        }
     {
         setStyle(new ImageButton.ImageButtonStyle() {{
             up = Tex.buttonEdge4;
             imageUp = Icon.right;
         }});
        clicked(() -> {
            Table[] sidebars = getSidebars();
            Element currentSidebar = sidebars[showIndex];
            showIndex = (showIndex + 1) % sidebars.length;
            Element nextSidebar = sidebars[showIndex];

            Log.info(sidebars[showIndex].getWidth());
            Log.info(sidebars[showIndex].getMinWidth());
            Log.info(sidebars[showIndex].getPrefWidth());
            actShowMoveX(currentSidebar, 0, -currentSidebar.getWidth());
            actShowMoveX(nextSidebar, -nextSidebar.getWidth(),0);
            actResizeWidth(this, nextSidebar.getWidth());
        });
    }};

    public SidebarSwitcher(Table ...sidebars) {
        this.sidebars = sidebars;

        Vars.ui.hudGroup.fill(t -> {
            t.name = "informatis sidebar";
            t.left();
            t.add(sidebarTable);
        });
        rebuildSidebarTable();
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

        sidebarTable.clear();
        sidebarTable.top().left();
        sidebarTable.add(sidebarTables).grow();
        sidebarTable.row();
        Cell cell = sidebarTable.add(switchButton).left();

        sidebars[showIndex].invalidate();
        Log.info(sidebars[showIndex].getWidth());
        Log.info(sidebars[showIndex].getMinWidth());
        Log.info(sidebars[showIndex].getPrefWidth());
        cell.minWidth(sidebars[showIndex].getPrefWidth());
    }

    private Table[] getSidebars() {
        return sidebars;
    }
}
