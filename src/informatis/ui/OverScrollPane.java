package informatis.ui;

import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.ScrollPane;
import arc.util.Log;

import static arc.Core.input;
import static arc.Core.scene;

public class OverScrollPane extends ScrollPane {
    Vec2 scrollPos;


    public OverScrollPane(Element widget, ScrollPaneStyle style, Vec2 scrollPos){
        super(widget, style);
        this.scrollPos = scrollPos;
        setScrollYForce(scrollPos.x);
        setScrollYForce(scrollPos.y);
    }

    @Override
    public Element update(Runnable r) {
        Element result = hit(input.mouseX(), input.mouseY(), true);
        if (result == null || !result.isDescendantOf(this)) {
            //scene.setScrollFocus(null);
            cancelTouchFocus();
        }
        scrollPos.x = getScrollX();
        scrollPos.y = getScrollY();

        return super.update(r);
    }

    public OverScrollPane disableScroll(boolean x, boolean y) {
        setScrollingDisabled(x, y);
        return this;
    }
}
