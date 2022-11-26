package informatis.ui;

import arc.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.*;

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
            t.center().left();

            t.table(body -> {
                ImageButton button = new ImageButton();
                button.clicked(() -> {
                    SnapshotSeq<Element> children = ((Group) body.getChildren().first()).getChildren();
                    Element currentSidebar = children.get(showIndex);
                    showIndex = (showIndex + 1) % children.size;
                    Element nextSidebar = children.get(showIndex);

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
                button.setWidth(sidebars[0].getWidth());

                body.top().left()
                    .defaults().growY();
                body.table(sides -> {
                    sides.top().left().defaults().growY();
                    for(int i = 0; i < sidebars.length; i++) {
                        Element elem = sidebars[i];
                        if(elem instanceof Table table) table.setBackground(Tex.buttonEdge3);
                        sides.add(elem).visible(i == 0);
                    }
                }).row();
                body.add(button).growX();
            });
        });
    }
}
