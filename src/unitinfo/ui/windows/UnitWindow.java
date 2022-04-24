package unitinfo.ui.windows;

import unitinfo.core.*;
import unitinfo.ui.*;
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
import mindustry.world.blocks.defense.ForceProjector;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.power.*;

import static unitinfo.SVars.*;
import static unitinfo.SUtils.*;
import static mindustry.Vars.*;

class UnitWindow extends Window {
    final Seq<Color> lastColors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);
    final Rect scissor = new Rect();
    Vec2 scrollPos;

    public UnitWindow() {
        super(Icon.units, "unit");
    }

    //TODO: add new UnitInfoDisplay(), new WeaponDisplay();
    @Override
    protected void build(Table table) {
        scrollPos = new Vec2(0,0);

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
            for (int i = 0; i < 6; i++) {
                int index = i;
                bars.table(bar -> {
                    bar.add(new SBar(
                        () -> BarInfo.strings.get(index),
                        () -> {
                            if (BarInfo.colors.get(index) != Color.clear) lastColors.set(index, BarInfo.colors.get(index));
                            return lastColors.get(index);
                        },
                        () -> BarInfo.numbers.get(index)
                    )).height(4 * 8f).growX();
                    bar.add(new Image(){
                        @Override
                        public void draw() {
                            validate();

                            Draw.color(Color.white);
                            Draw.alpha(parentAlpha * color.a);
                            TextureRegionDrawable region = new TextureRegionDrawable(getRegions(index));
                            region.draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
                            Draw.color(BarInfo.colors.get(index));
                            if(ScissorStack.push(scissor.set(x, y, imageWidth * scaleX, imageHeight * scaleY * BarInfo.numbers.get(index)))){
                                region.draw(x, y, imageWidth * scaleX, imageHeight * scaleY);
                                ScissorStack.pop();
                            }
                        }
                    }).size(iconMed * 0.75f).padLeft(8f);
                }).growX().row();
            }
        }), Styles.nonePane, scrollPos).disableScroll(true, false)).growX().padTop(12f);
    }

    //do not ask me WHAT THE FUCK IS THIS
    TextureRegion getRegions(int i){
        TextureRegion region = clear;

        if(i == 0){
            if(target instanceof Healthc) region = SIcons.health;
        } else if(i == 1){
            if(target instanceof Turret.TurretBuild ||
                    target instanceof MassDriver.MassDriverBuild){
                region = SIcons.reload;
            } else if((target instanceof Unit unit && unit.type != null) ||
                    target instanceof ForceProjector.ForceBuild){
                region = SIcons.shield;
            } else if(target instanceof PowerNode.PowerNodeBuild ||
                    target instanceof PowerGenerator.GeneratorBuild){
                region = SIcons.power;
            }
        } else if(i == 2){
            if(target instanceof ItemTurret.ItemTurretBuild){
                region = SIcons.ammo;
            } else if(target instanceof LiquidTurret.LiquidTurretBuild){
                region = SIcons.liquid;
            } else if(target instanceof PowerTurret.PowerTurretBuild ||
                    target instanceof PowerNode.PowerNodeBuild){
                region = SIcons.power;
            } else if((target instanceof Building b && b.block.hasItems) ||
                    (target instanceof Unit unit && unit.type != null)){
                region = SIcons.item;
            }
        } else if(i == 3){
            if(target instanceof PowerNode.PowerNodeBuild){
                region = SIcons.power;
            }
        } else if(i == 4){
            if(target instanceof PowerNode.PowerNodeBuild){
                region = SIcons.power;
            } else if(target instanceof Building b && b.block.hasLiquids){
                region = SIcons.liquid;
            }
        } else if(i == 5){
            if(target instanceof Unit unit && state.rules.unitAmmo && unit.type != null){
                region = SIcons.ammo;
            }else if(target instanceof PowerNode.PowerNodeBuild ||
                    (target instanceof Building b && b.block.consumes.hasPower())){
                region = SIcons.power;
            }
        }

        return region;
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
