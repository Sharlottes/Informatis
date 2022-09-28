package informatis.ui.fragments;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Scl;
import arc.struct.SnapshotSeq;
import arc.util.Align;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;

import static arc.Core.scene;
import static arc.Core.settings;

public class ElementViewFragment extends Element {
    Group root;
    Element selected;

    public ElementViewFragment() {
        this(scene.root);
    }
    public ElementViewFragment(Group root) {
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


        selected.localToStageCoordinates(Tmp.v1.set(0, 0));
        Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * 1.5f));
        Lines.stroke(1.5f);
        Lines.rect(Tmp.v1.x, Tmp.v1.y, selected.getWidth(), selected.getHeight());
        Fonts.outline.draw(selected.getWidth() + ", " + selected.getHeight(), Tmp.v1.x, Tmp.v1.y,
                Pal.accent, 1f, false, Align.center);
    }

    void addRect(SnapshotSeq<Element> elements) {
        elements.each(elem-> {
            elem.updateVisibility();
            if(elem.visible || settings.getBool("hiddenElem")) {
                Draw.color();
                Lines.stroke(1);
                elem.localToStageCoordinates(Tmp.v1.set(0, 0));
                if(elem.hasMouse()) {
                    Draw.color(Pal.accent);
                    Lines.stroke(3);
                    selected = elem;
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

                if(elem instanceof Group group) addRect(group.getChildren());
            }
        });
    }
}
