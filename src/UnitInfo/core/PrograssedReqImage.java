package UnitInfo.core;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.graphics.*;

public class PrograssedReqImage extends Stack {
    public PrograssedReqImage(Element image, Boolp valid, float prograss){
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
}

