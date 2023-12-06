package informatis.ui.components;

import informatis.SUtils;
import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.util.Align;
import arc.util.Tmp;
import informatis.ui.fragments.sidebar.windows.UnitWindow;
import mindustry.graphics.*;
import mindustry.ui.Fonts;

public class SBar extends Element {
    static final Rect scissor = new Rect();
    private static final Drawable
            barSprite = SUtils.getDrawable(Core.atlas.find("informatis-barS"), 10, 10, 9, 9),
            barTopSprite = SUtils.getDrawable(Core.atlas.find("informatis-barS-top"), 10, 10, 9, 9);

    public final UnitWindow.BarData barData;
    float value, lastValue, blink;

    public SBar(String name, Color color, Floatp fraction){
        this(new UnitWindow.BarData(() -> name, () -> color, fraction));
    }
    public SBar(String name, Color fromColor, Color toColor, Floatp fraction){
        this(new UnitWindow.BarData(pro -> name, pro -> Tmp.c1.set(fromColor).lerp(toColor, pro), fraction));
    }
    public SBar(UnitWindow.BarData barData){
        this.barData = barData;
        lastValue = value = Mathf.clamp(barData.fraction.get());
    }

    @Override
    public void draw(){
        float computed = Mathf.clamp(barData.fraction.get());

        if(lastValue > computed){
            blink = 1f;
            lastValue = computed;
        }

        blink = Mathf.lerpDelta(blink, 0f, 0.2f);
        value = Mathf.lerpDelta(value, computed, 0.05f);
        setColor(barData.color.get());

        Draw.colorl(0.1f);
        barSprite.draw(x, y, width, height);
        drawBarSprite(Tmp.c1.set(color).mul(Pal.lightishGray), width * value);
        drawBarSprite(color, width * Math.min(value, computed));
        Fonts.outline.draw(barData.name.get(), x + width / 2f, y + height * 0.75f, Color.white, 1, false, Align.center);
    }

    private void drawBarSprite(Color color, float width) {
        Draw.color(color, Color.white, blink);
        if(width > barSprite.getMinWidth()){
            SBar.barTopSprite.draw(x, y, width, height);
        } else if(ScissorStack.push(scissor.set(x, y, width, height))){
            SBar.barTopSprite.draw(x, y, barSprite.getMinWidth(), height);
            ScissorStack.pop();
        }
    }

    public static class SBarData {
        public final String name;
        public final Floatp fraction;
        public final Color fromColor;
        public final Color toColor;

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

