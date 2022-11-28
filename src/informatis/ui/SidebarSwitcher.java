package informatis.ui;

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

import javax.sound.sampled.Line;

public class SidebarSwitcher {
    int showIndex = 0;
    final Element[] sidebars;

    public SidebarSwitcher(Element ...sidebars) {
        this.sidebars = sidebars;
    }

    void actShowMoveX(Element element, float from, float to) {
        MoveToAction moveToAction = new MoveToAction();
        moveToAction.setDuration(1);
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

    void actResizeWidth(Element element, float width) {
        SizeToAction sizeToAction = new SizeToAction();
        sizeToAction.setSize(width, element.getHeight());
        sizeToAction.setDuration(1);
        sizeToAction.setInterpolation(Interp.circleOut);
        element.actions(sizeToAction);
        element.act(Core.graphics.getDeltaTime());
        element.draw();
    }

    public void init() {
        Vars.ui.hudGroup.fill(t -> {
            t.name = "informatis sidebar";
            t.left();

            t.table(body -> {
                ImageButton button = new ImageButton() {
                    @Override
                    public void draw() {
                        super.draw();
                        Lines.stroke(5, Pal.gray);
                        Lines.line(this.x, this.y + this.height, this.x + this.width, this.y + this.height);
                    }
                };
                button.clicked(() -> {
                    SnapshotSeq<Element> children = ((Stack) body.getChildren().first()).getChildren();
                    Element currentSidebar = ((Table) children.get(showIndex)).getChildren().first();
                    showIndex = (showIndex + 1) % children.size;
                    Element nextSidebar = ((Table) children.get(showIndex)).getChildren().first();

                    actShowMoveX(currentSidebar, 0, -currentSidebar.getWidth());
                    actShowMoveX(nextSidebar, -nextSidebar.getWidth(),0);
                    actResizeWidth(button, nextSidebar.getWidth());

                    button.setDisabled(true);
                    Time.run(60, () -> button.setDisabled(false));
                });
                ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
                style.up = Tex.buttonEdge4;
                style.imageUp = Icon.right;
                button.setStyle(style);
                actResizeWidth(button, sidebars[showIndex].getWidth());

                Stack sidebarTables = new Stack();
                for(int i = 0; i < sidebars.length; i++) {
                    int j = i;
                    sidebarTables.add(new Table(table -> {
                        Element elem = sidebars[j];
                        if (elem instanceof Table elemTable) elemTable.setBackground(Tex.buttonEdge3);

                        table.left();
                        table.add(elem).growY();
                        elem.visible = j == 0;
                    }));
                }
                body.top().left();
                body.add(sidebarTables).grow().row();
                body.add(button).growX();
            });
        });
    }
}
