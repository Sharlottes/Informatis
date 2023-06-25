package informatis.ui.components;

import arc.Core;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.style.Drawable;
import informatis.SUtils;

public class OneDotBar extends Element {
    private static final Drawable barSprite = SUtils.getDrawable(Core.atlas.find("informatis-onepx"), 0, 0, 0, 0);
    private Floatp fraction;
    private float value;

    public OneDotBar(Prov<Color> color, Floatp fraction) {
        update(() -> setColor(color.get()));
        this.fraction = fraction;
    }

    @Override
    public void draw() {
        float computed = Mathf.clamp(fraction.get());

        value = Mathf.lerpDelta(value, Mathf.clamp(fraction.get()), 0.05f);
        Draw.colorl(0.1f);
        barSprite.draw(x, y, width, height);
        Draw.color(color);
        barSprite.draw(x, y, width * Math.min(value, computed), height);
    }

    public static class SBarData {
        public String name;
        public Floatp fraction;
        public Color fromColor;
        public Color toColor;

        public SBarData(String name, Color fromColor, Color toColor, Floatp fraction) {
            this.name = name;
            this.fromColor = fromColor;
            this.toColor = toColor;
            this.fraction = fraction;
        }

        public SBarData(String name, Color color, Floatp fraction) {
            this(name, color, color, fraction);
        }
    }
}
