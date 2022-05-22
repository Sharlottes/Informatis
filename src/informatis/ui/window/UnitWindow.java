package informatis.ui.window;

import arc.math.Mathf;
import informatis.core.*;
import informatis.ui.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.Bits;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.type.Weapon;
import mindustry.ui.Styles;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.payloads.Payload;

import static informatis.SVars.*;
import static informatis.SUtils.*;
import static mindustry.Vars.*;

class UnitWindow extends Window {
    final Seq<Color> lastColors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);
    final Rect scissor = new Rect();
    Seq<Table> bars = new Seq<>(); //temp
    Vec2 scrollPos = new Vec2(0, 0);

    public UnitWindow() {
        super(Icon.units, "unit");
        for(int i = 0; i < 6; i++) addBar();
    }

    //TODO: add new UnitInfoDisplay(), new WeaponDisplay();
    @Override
    protected void build(Table table) {
        scrollPos = new Vec2(0, 0);
        table.top().background(Styles.black8);
        table.table(tt -> {
            tt.center();
            Image image = new Image() {
                @Override
                public void draw() {
                    super.draw();

                    int offset = 8;
                    Draw.color(locked?Pal.accent:Pal.gray);
                    Draw.alpha(parentAlpha);
                    Lines.stroke(Scl.scl(3f));
                    Lines.rect(x-offset/2f, y-offset/2f, width+offset, height+offset);
                    Draw.reset();
                }
            };
            image.update(()->{
                TextureRegion region = clear;
                if (target instanceof Unit u && u.type != null) region = u.type.uiIcon;
                else if (target instanceof Building b) {
                    if (target instanceof ConstructBlock.ConstructBuild cb) region = cb.current.uiIcon;
                    else if (b.block != null) region = b.block.uiIcon;
                }
                image.setDrawable(region);
            });
            image.clicked(()->{
                if(target==getTarget()) locked = !locked;
                target = getTarget();
            });

            tt.add(image).size(iconMed).padRight(12f);
            tt.label(() -> {
                if (target instanceof Unit u && u.type != null) return u.type.localizedName;
                if (target instanceof Building b && b.block != null) {
                    if (target instanceof ConstructBlock.ConstructBuild cb) return cb.current.localizedName;
                    return b.block.localizedName;
                }
                return "";
            }).color(Pal.accent);
        }).tooltip((to -> {
            to.background(Styles.black6);
            to.label(() -> target instanceof Unit u && u.isPlayer() ? u.getPlayer().name() : "AI").row();
            to.label(() -> target == null ? "(" + 0 + ", " + 0 + ")" : "(" + Strings.fixed(target.x() / tilesize, 2) + ", " + Strings.fixed(target.y() / tilesize, 2) + ")").row();
            to.label(() -> target instanceof Unit u ? "[accent]"+ Strings.fixed(u.armor, 0) + "[] Armor" : "");
        })).margin(12f).row();
        table.image().height(4f).color((target==null?player.unit():target).team().color).growX().row();
        table.add(new OverScrollPane(new Table(bars -> {
            bars.top();
            bars.table().update(t->{
                t.clear();
                this.bars.clear();
                for(int i = 0; i < 6; i++) addBar();
                for (Table bar : this.bars) {
                    t.add(bar).growX().row();
                }
            }).growX();
        }), Styles.noBarPane, scrollPos).disableScroll(true, false)).growX().padTop(12f);
    }

    void addBar() {
        int index = this.bars.size;
        bars.add(new Table(bar -> {
            bar.add(new SBar(
                    () -> {
                        BarInfo.BarData data = index >= BarInfo.data.size ? null : BarInfo.data.get(index);
                        return data == null ? "[lightgray]<Empty>[]" : data.name;
                    },
                    () -> {
                        BarInfo.BarData data = index >= BarInfo.data.size ? null : BarInfo.data.get(index);
                        if (index >= lastColors.size) lastColors.size = index+1;
                        if (data == null) return lastColors.get(index);
                        if (data.color != Color.clear) lastColors.set(index, data.color);
                        return lastColors.get(index);
                    },
                    () -> {
                        BarInfo.BarData data = index >= BarInfo.data.size ? null : BarInfo.data.get(index);
                        return data == null ? 0 : data.number;
                    }
            )).height(4 * 8f).growX();
            bar.add(new Image(){
                @Override
                public void draw() {
                    validate();

                    BarInfo.BarData data = index >= BarInfo.data.size ? null : BarInfo.data.get(index);
                    Draw.color(Color.white);
                    Draw.alpha(parentAlpha * color.a);
                    if(data == null) {
                        new TextureRegionDrawable(clear).draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
                        return;
                    }
                    TextureRegionDrawable region = new TextureRegionDrawable(data.icon);
                    region.draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
                    Draw.color(data.color);
                    if(ScissorStack.push(scissor.set(x, y, imageWidth * scaleX, imageHeight * scaleY * data.number))){
                        region.draw(x, y, imageWidth * scaleX, imageHeight * scaleY);
                        ScissorStack.pop();
                    }
                }
            }).size(iconMed * 0.75f).padLeft(8f);
        }));
    }

    static class WeaponDisplay extends Table {
        WeaponDisplay() {
            table().update(tt -> {
                tt.clear();
                if(getTarget() instanceof Unit u && u.type != null && u.hasWeapons()) {
                    for(int r = 0; r < u.type.weapons.size; r++){
                        Weapon weapon = u.type.weapons.get(r);
                        WeaponMount mount = u.mounts[r];
                        int finalR = r;
                        tt.table(ttt -> {
                            ttt.left();
                            if((1 + finalR) % 4 == 0) ttt.row();
                            ttt.stack(
                                new Table(o -> {
                                    o.left();
                                    o.add(new Image(!weapon.name.equals("") && weapon.outlineRegion.found() ? weapon.outlineRegion : u.type.uiIcon){
                                        @Override
                                        public void draw(){
                                            validate();
                                            float x = this.x;
                                            float y = this.y;
                                            float scaleX = this.scaleX;
                                            float scaleY = this.scaleY;
                                            Draw.color(color);
                                            Draw.alpha(parentAlpha * color.a);

                                            if(getDrawable() instanceof TransformDrawable){
                                                float rotation = getRotation();
                                                if(scaleX != 1 || scaleY != 1 || rotation != 0){
                                                    getDrawable().draw(x + imageX, y + imageY, originX - imageX, originY - imageY, imageWidth, imageHeight, scaleX, scaleY, rotation);
                                                    return;
                                                }
                                            }
                                            y -= (mount.reload) / weapon.reload * weapon.recoil;
                                            if(getDrawable() != null)
                                                getDrawable().draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
                                        }
                                    }).size(iconLarge);
                                }),
                                new Table(h -> {
                                    h.defaults().growX().height(9f).width(iconLarge).padTop(18f);
                                    h.add(new SBar(
                                            () -> "",
                                            () -> Pal.accent.cpy().lerp(Color.orange, mount.reload / weapon.reload),
                                            () -> mount.reload / weapon.reload).rect().init());
                                    h.pack();
                                })
                            );
                        }).pad(4);
                    }
                }
            });
        }
    }
    static class UnitInfoDisplay extends Table {
        UnitInfoDisplay() {
            top();
            float[] count = new float[]{-1};
            table().update(t -> {
                if(getTarget() instanceof Payloadc payload){
                    if(count[0] != payload.payloadUsed()){
                        t.clear();
                        t.top().left();

                        float pad = 0;
                        float items = payload.payloads().size;
                        if(8 * 2 * items + pad * items > 275f){
                            pad = (275f - (8 * 2) * items) / items;
                        }
                        int i = 0;
                        for(Payload p : payload.payloads()){
                            t.image(p.icon()).size(8 * 2).padRight(pad);
                            if(++i % 12 == 0) t.row();
                        }

                        count[0] = payload.payloadUsed();
                    }
                }else{
                    count[0] = -1;
                    t.clear();
                }
            }).growX().visible(() -> getTarget() instanceof Payloadc p && p.payloadUsed() > 0).colspan(2).row();

            Bits statuses = new Bits();
            table().update(t -> {
                t.left();
                if(getTarget() instanceof Statusc st){
                    Bits applied = st.statusBits();
                    if(!statuses.equals(applied)){
                        t.clear();

                        if(applied != null){
                            for(StatusEffect effect : Vars.content.statusEffects()){
                                if(applied.get(effect.id) && !effect.isHidden()){
                                    t.image(effect.uiIcon).size(iconSmall).get()
                                        .addListener(new Tooltip(l -> l.label(() -> effect.localizedName + " [lightgray]" + UI.formatTime(st.getDuration(effect))).style(Styles.outlineLabel)));
                                }
                            }
                            statuses.set(applied);
                        }
                    }
                }
            }).left();
        }
    }
}
