package UnitInfo.ui;
import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.scene.ui.layout.Scl;
import arc.util.pooling.*;
import mindustry.gen.*;
import mindustry.ui.Fonts;

import static arc.Core.settings;

public class SBar extends Element{
    private static Rect scissor = new Rect();

    private Floatp fraction;
    private String name = "";
    private float value, lastValue, blink;
    private Color blinkColor = new Color();
    private boolean valid = true;

    public SBar(String name, Color color, Floatp fraction){
        this.fraction = fraction;
        this.name = Core.bundle.get(name, name);
        this.blinkColor.set(color);
        lastValue = value = fraction.get();
        setColor(color);
    }

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

    public SBar(Prov<String> name, Prov<Color> color, Floatp fraction, Boolp valid){
        this.fraction = fraction;
        try{
            lastValue = value = Mathf.clamp(fraction.get());
        }catch(Exception e){ //getting the fraction may involve referring to invalid data
            lastValue = value = 0f;
        }
        update(() -> {
            try{
                this.valid = valid.get();
                this.name = name.get();
                this.blinkColor.set(color.get());
                setColor(color.get());
            }catch(Exception e){ //getting the fraction may involve referring to invalid data
                this.name = "";
            }
        });
    }


    public SBar(){

    }

    public void reset(float value){
        this.value = lastValue = blink = value;
    }

    public void set(Prov<String> name, Floatp fraction, Color color){
        this.fraction = fraction;
        this.lastValue = fraction.get();
        this.blinkColor.set(color);
        setColor(color);
        update(() -> this.name = name.get());
    }

    public SBar blink(Color color){
        blinkColor.set(color);
        return this;
    }

    public Drawable drawable(String name){
        Drawable out = null;

        TextureAtlas.AtlasRegion region = Core.atlas.find(name);

        int[] splits = {10,10,9,9};
        NinePatch patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
        int[] pads = region.pads;
        if(pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
        out = new ScaledNinePatchDrawable(patch, 1f);

        return out;
    }

    @Override
    public void draw(){
        if(fraction == null || !valid) return;

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
        value = Mathf.lerpDelta(value, computed, 0.15f);

        NinePatchDrawable bar = (NinePatchDrawable) drawable("unitinfo-barS");
        Draw.colorl(0.1f);
        bar.draw(x, y, width, height);
        Draw.color(color, blinkColor, blink);

        NinePatchDrawable top = (NinePatchDrawable) drawable("unitinfo-barS-top");
        float topWidth = width * value;

        if(topWidth > Core.atlas.find("unitinfo-bar-top").width){
            top.draw(x, y, topWidth, height);
        }else{
            if(ScissorStack.push(scissor.set(x, y, topWidth, height))){
                top.draw(x, y, Core.atlas.find("unitinfo-bar-top").width, height);
                ScissorStack.pop();
            }
        }

        Draw.color();

        Font font = Fonts.outline;
        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        font.getData().setScale(Scl.scl() * (settings.getInt("uiscaling") / 100f));
        lay.setText(font, name);
        font.setColor(Color.white);
        font.draw(name, x + width / 2f - lay.width / 2f, y + height / 2f + lay.height / 2f + 1);
        font.getData().setScale(Scl.scl());

        Pools.free(lay);
    }
}
