package informatis.ui.widgets;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Scl;
import mindustry.graphics.Pal;

import static informatis.SVars.locked;

public class RectWidget {
    public static Image build() {
        return build(8);
    }

    public static Image build(float size) {
        return new Image() {
            @Override
            public void draw() {
                super.draw();

                Draw.color(locked? Pal.accent:Pal.gray);
                Draw.alpha(parentAlpha);
                Lines.stroke(Scl.scl(3f));
                Lines.rect(x-size/2f, y-size/2f, width+size, height+size);
                Draw.reset();
            }
        };
    }
}
