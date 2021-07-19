package UnitInfo.ui;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.pooling.*;
import mindustry.graphics.*;
import mindustry.ui.*;

public class SBar extends Element{
    private static final Rect scissor = new Rect();

    private final Floatp fraction;
    private String name = "";
    private float value, lastValue, blink;
    private final Color blinkColor = new Color();

    public SBar(Prov<String> name, Prov<Color> color, Floatp fraction){
        this.fraction = fraction;
        try{
            lastValue = value = Mathf.clamp(fraction.get());
        }catch(Exception e){ //getting the fraction may involve referring to invalid data
            lastValue = value = 0f;
        }
        update(() -> {
            try{
                this.name = name.get();
                this.blinkColor.set(color.get());
                setColor(color.get());
            }catch(Exception e){ //getting the fraction may involve referring to invalid data
                this.name = "";
            }
        });
    }

    public Drawable drawable(String name, int left, int right, int top, int bottom){
        Drawable out;

        TextureAtlas.AtlasRegion region = Core.atlas.find(name);

        int[] splits = {left, right, top, bottom};
        NinePatch patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
        int[] pads = region.pads;
        if(pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
        out = new ScaledNinePatchDrawable(patch, 1f);

        return out;
    }

    @Override
    public void draw(){
        boolean valid = true;
        if(fraction == null || !valid) return;
        boolean ssim = Core.settings.getBool("ssim");
        boolean shar = Core.settings.getBool("shar");

        float computed;
        try{
            computed = Mathf.clamp(fraction.get());
        }catch(Exception e){ //getting the fraction may involve referring to invalid data
            computed = 0f;
        }

        if(lastValue > computed){
            blink = 1f;
            lastValue = computed;
        }

        if(Float.isNaN(lastValue)) lastValue = 0;
        if(Float.isInfinite(lastValue)) lastValue = 1f;
        if(Float.isNaN(value)) value = 0;
        if(Float.isInfinite(value)) value = 1f;
        if(Float.isNaN(computed)) computed = 0;
        if(Float.isInfinite(computed)) computed = 1f;

        blink = Mathf.lerpDelta(blink, 0f, 0.2f);
        value = Mathf.lerpDelta(value, computed, 0.05f);

        NinePatchDrawable bar = (NinePatchDrawable) drawable("unitinfo-barS", 10, 10, 9, 9);
        NinePatchDrawable top = (NinePatchDrawable) drawable("unitinfo-barS-top", 10, 10, 9, 9);
        float spriteWidth = Core.atlas.find("unitinfo-barS").width;
        if(ssim){
            bar = (NinePatchDrawable) drawable("unitinfo-barSS", 14, 14, 19, 19);
            top = (NinePatchDrawable) drawable("unitinfo-barSS-top", 14, 14, 19, 19);
            spriteWidth = Core.atlas.find("unitinfo-barSS").width;
        }
        else if(shar){
            bar = (NinePatchDrawable) drawable("unitinfo-barSSS", 25, 25, 17, 17);
            top = (NinePatchDrawable) drawable("unitinfo-barSSS-top", 25, 25, 17, 17);
            spriteWidth = Core.atlas.find("unitinfo-barSSS").width;
        }

        Draw.colorl(0.1f);
        bar.draw(x, y, width, height);
        Draw.color(color.cpy().mul(Pal.lightishGray), blinkColor, blink);

        float topWidth = width * value;

        if(topWidth > spriteWidth){
            top.draw(x, y, topWidth, height);
        }else{
            if(ScissorStack.push(scissor.set(x, y, topWidth, height))){
                top.draw(x, y, spriteWidth, height);
                ScissorStack.pop();
            }
        }

        Draw.color(color, blinkColor, blink);
        float topWidthReal = width * (Math.min(value, computed));

        if(topWidthReal > spriteWidth){
            top.draw(x, y, topWidthReal, height);
        }else{
            if(ScissorStack.push(scissor.set(x, y, topWidthReal, height))){
                top.draw(x, y, spriteWidth, height);
                ScissorStack.pop();
            }
        }

        Draw.color();

        Font font = Fonts.outline;
        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        font.getData().setScale(Scl.scl());
        lay.setText(font, name);
        font.setColor(Color.white);
        font.draw(name, x + width / 2f - lay.width / 2f, y + height / 2f + lay.height / 2f + 1);
        font.getData().setScale(Scl.scl());

        Pools.free(lay);
        Draw.reset();
    }
}
