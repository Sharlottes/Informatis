package unitinfo.ui;

import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.ScrollPane;
import mindustry.ui.Styles;

import static arc.Core.input;
import static arc.Core.scene;

public class OverScrollPane extends ScrollPane {
    Vec2 scrollPos;


    public OverScrollPane(Element widget, ScrollPaneStyle style, Vec2 scrollPos){
        super(widget, style);
        this.scrollPos = scrollPos;

        update(() -> {
            if (hasScroll()) {
                Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                if (result == null || !result.isDescendantOf(this)) {
                    scene.setScrollFocus(null);
                }
            }
            scrollPos.x = getScrollX();
            scrollPos.y = getScrollY();
        });
        setOverscroll(false, false);
        setScrollYForce(scrollPos.x);
        setScrollYForce(scrollPos.y);
    }

    public OverScrollPane disableScroll(boolean x, boolean y) {
        setScrollingDisabled(x, y);
        return this;
    }
}
