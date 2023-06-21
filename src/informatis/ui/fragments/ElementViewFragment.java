package informatis.ui.fragments;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.struct.SnapshotSeq;
import arc.util.Align;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;

import static arc.Core.settings;

public class ElementViewFragment extends Element {
    private final Group[] roots;

    public ElementViewFragment(Group... roots) {
        this.roots = roots;
        fillParent = true;
        touchable = Touchable.disabled;
        Core.scene.root.addChild(this);
    }

    @Override
    public void draw() {
        if(!settings.getBool("elementdebug")) return;

        super.draw();
        Draw.z(Layer.max);
        Lines.stroke(1);
        for(Group root : roots)
            drawElementRect(root.getChildren());
    }

    private void drawElementRect(SnapshotSeq<Element> elements) {
        elements.each(elem -> {
            elem.updateVisibility();
            if(elem.visible || settings.getBool("hiddenElem")) {
                Draw.color();
                Lines.stroke(1);
                elem.localToStageCoordinates(Tmp.v1.set(0, 0));
                if(elem.hasMouse()) {
                    Draw.color(Pal.accent);
                    Lines.stroke(3);
                    drawElementTransform(elem);
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

                if(elem instanceof Group group) drawElementRect(group.getChildren());
            }
        });
    }

    private void drawElementTransform(Element element) {
        element.localToStageCoordinates(Tmp.v1.set(0, 0));
        Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * 1.5f));
        Lines.stroke(1.5f);
        Lines.rect(Tmp.v1.x, Tmp.v1.y, element.getWidth(), element.getHeight());
        Fonts.outline.draw(element.getWidth() + ", " + element.getHeight(), Tmp.v1.x, Tmp.v1.y,
                Pal.accent, 1f, false, Align.center);
    }
}
