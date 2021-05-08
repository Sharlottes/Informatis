package UnitInfo.core;

import arc.func.Boolp;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.scene.Element;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Stack;
import mindustry.graphics.Pal;

public class PrograssedReqImage extends Stack {
    private final Boolp valid;

    public PrograssedReqImage(Element image, Boolp valid, float prograss){
        this.valid = valid;
        add(image);
        add(new Element(){
            {
                visible(() -> !valid.get());
            }

            @Override
            public void draw(){
                Lines.stroke(Scl.scl(2f), Pal.removeBack);
                Draw.alpha(1 - prograss);
                Lines.line(x, y - 2f + height, x + width, y - 2f);
                Draw.color(Pal.remove);
                Draw.alpha(1 - prograss);
                Lines.line(x, y + height, x + width, y);
                Draw.reset();
            }
        });
    }

    public PrograssedReqImage(TextureRegion region, Boolp valid, float prograss){
        this(new Image(region), valid, prograss);
    }

    public boolean valid(){
        return valid.get();
    }
}

