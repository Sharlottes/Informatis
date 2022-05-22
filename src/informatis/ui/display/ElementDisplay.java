package informatis.ui.display;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.struct.SnapshotSeq;
import arc.util.Tmp;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

import static arc.Core.scene;
import static arc.Core.settings;

public class ElementDisplay extends Element {
    Group root;

    public ElementDisplay() {
        this(scene.root);
    }
    public ElementDisplay(Group root) {
        this.root = root;
        fillParent = true;
        touchable = Touchable.disabled;
    }

    @Override
    public void draw() {
        super.draw();
        if(!settings.getBool("elementdebug")) return;
        Draw.z(Layer.max);
        Lines.stroke(1);
        addRect(root.getChildren());
    }

    void addRect(SnapshotSeq<Element> elements) {
        elements.each(elem-> {
            elem.updateVisibility();
            if(elem.visible || settings.getBool("hiddenElem")) {
                elem.localToStageCoordinates(Tmp.v1.set(0, 0));
                if(elem.hasMouse()) {
                    Draw.color(Pal.accent);
                    Lines.stroke(3);
                }
                if(elem.hasScroll()) {
                    Draw.color(Pal.lancerLaser);
                    Lines.stroke(3);
                }
                if(elem.hasKeyboard()) {
                    Draw.color(Pal.sap);
                    Lines.stroke(5);
                }
                Lines.rect(Tmp.v1.x, Tmp.v1.y, elem.getWidth(), elem.getHeight());
                Draw.color();
                Lines.stroke(1);
                if(elem instanceof Group group) addRect(group.getChildren());
            }
        });
    }
}
