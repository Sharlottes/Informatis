package informatis.ui;

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
import mindustry.graphics.*;
import mindustry.ui.Fonts;

public class SBar extends Element{
    static final Rect scissor = new Rect();

    Floatp fraction;
    String name = "";
    float value, lastValue, blink;
    final Color blinkColor = new Color();
    NinePatchDrawable bar, top;
    float spriteWidth;

    public boolean onedot = false;

    //static bar
    public SBar(String name, Color color, float fraction){
        this.fraction = ()->fraction;
        this.name = Core.bundle.get(name, name);
        this.blinkColor.set(color);
        lastValue = value = fraction;
        setColor(color);
        init();
    }

    //dynamic bar
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
        init();
    }

    public SBar rect(){
        onedot = true;
        return this;
    }

    public SBar init(){
        int h = Core.settings.getInt("barstyle");

        bar = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-barS"), 10, 10, 9, 9);
        top = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-barS-top"), 10, 10, 9, 9);
        spriteWidth = Core.atlas.find("informatis-barS").width;
        if(onedot){
            bar = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-1dotbar"), 0,0,0,0);
            top = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-1dotbar-top"),0,0,0,0);
            spriteWidth = Core.atlas.find("informatis-1dotbar").width;
        }
        else if(h == 1){
            bar = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-barSS"), 14, 14, 19, 19);
            top = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-barSS-top"), 14, 14, 19, 19);
            spriteWidth = Core.atlas.find("informatis-barSS").width;
        }
        else if(h == 2){
            bar = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-barSSS"), 25, 25, 17, 17);
            top = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-barSSS-top"), 25, 25, 17, 17);
            spriteWidth = Core.atlas.find("informatis-barSSS").width;
        }
        else if(h == 3){
            bar = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-barSSSS"), 25, 25, 17, 17);
            top = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-barSSSS-top"), 25, 25, 17, 17);
            spriteWidth = Core.atlas.find("informatis-barSSSS").width;
        }
        else if(h == 4){
            bar = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-barSSSSS"), 27, 27, 16, 16);
            top = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-barSSSSS-top"), 27, 27, 16, 16);
            spriteWidth = Core.atlas.find("informatis-barSSSSS").width;
        }
        else if(h == 5){
            bar = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-barSSSSSS"), 32, 32, 16, 16);
            top = (NinePatchDrawable) SUtils.getDrawable(Core.atlas.find("informatis-barSSSSSS-top"), 32, 32, 16, 16);
            spriteWidth = Core.atlas.find("informatis-barSSSSSS").width;
        }
        return this;
    }

    @Override
    public void draw(){
        if(fraction == null) return;

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

        Draw.colorl(0.1f);
        bar.draw(x, y, width, height);

        Draw.color(Tmp.c1.set(color).mul(Pal.lightishGray), blinkColor, blink);
        float topWidth = width * value;
        if(topWidth > spriteWidth){
            top.draw(x, y, topWidth, height);
        } else if(ScissorStack.push(scissor.set(x, y, topWidth, height))){
            top.draw(x, y, spriteWidth, height);
            ScissorStack.pop();
        }

        Draw.color(color, blinkColor, blink);
        float topWidthReal = width * Math.min(value, computed);
        if(topWidthReal > spriteWidth){
            top.draw(x, y, topWidthReal, height);
        } else if(ScissorStack.push(scissor.set(x, y, topWidthReal, height))){
            top.draw(x, y, spriteWidth, height);
            ScissorStack.pop();
        }

        Fonts.outline.draw(name, x + width / 2f, y + height * 0.75f, Color.white, 1, false, Align.center);
    }
}
