package UnitInfo.ui;

import UnitInfo.SVars;
import UnitInfo.core.BarInfo;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.geom.Rect;
import arc.scene.Element;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.Bits;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
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

import static UnitInfo.SVars.clear;
import static UnitInfo.SVars.modUiScale;
import static arc.Core.*;
import static mindustry.Vars.*;

public class UnitDisplay extends Table {
    static float weaponScrollPos;
    static Seq<Element> bars = new Seq<>();
    static Seq<Color> lastColors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);
    static final Rect scissor = new Rect();

    public UnitDisplay() {
        fillParent = true;
        visibility = () -> 0 == SVars.hud.uiIndex;

        left().defaults().width(Scl.scl(modUiScale) * 35 * 8f).height(Scl.scl(modUiScale) * 35 * 8f);
        bars.clear();
        for(int i = 0; i < 6; i++) bars.add(addBar(i));
        Table table1 = new Table(Tex.button, t -> {
            t.table(Tex.underline2, tt -> {
                tt.setWidth(Scl.scl(modUiScale) * 35 * 8f);
                Stack stack = new Stack(){{
                    add(new Table(ttt -> {
                        Prov<TextureRegionDrawable> reg = () -> {
                            TextureRegion region = clear;
                            if(getTarget() instanceof Unit u && u.type != null) region = u.type.uiIcon;
                            else if(getTarget() instanceof Building b) {
                                if(getTarget() instanceof ConstructBlock.ConstructBuild cb) region = cb.current.uiIcon;
                                else if(b.block != null) region = b.block.uiIcon;
                            }
                            return new TextureRegionDrawable(region);
                        };
                        Drawable img = reg.get();
                        ImageButton imagebt = new ImageButton(img, img);

                        imagebt.hovered(()->{
                            Time.run(60*2, ()->{
                                if(imagebt.isOver()) lockTarget();
                            });
                        });
                        imagebt.clicked(()->{
                            if(getTarget() instanceof Unit u && u.type != null) ui.content.show(u.type);
                            else if(getTarget() instanceof Building b && b.block != null) ui.content.show(b.block);
                        });

                        ttt.add(imagebt).update((i) -> {
                            i.getStyle().imageUp = reg.get().tint(Tmp.c1.set(SVars.hud.locked ? Color.red.shiftHue(2*60%Time.delta) : Color.white));
                            i.getStyle().imageDown = reg.get().tint(Tmp.c1.mul(Color.darkGray));
                            i.layout();
                        }).size(Scl.scl(modUiScale) * 4 * 8f);
                    }));

                    add(new Table(ttt -> {
                        ttt.stack(
                                new Table(temp -> {
                                    temp.image(new ScaledNinePatchDrawable(new NinePatch(Icon.defenseSmall.getRegion()), modUiScale));
                                    temp.visibility = () -> getTarget() instanceof Unit;
                                }),
                                new Table(temp -> {
                                    Label label = new Label(() -> (getTarget() instanceof Unit u && u.type != null ? (int) u.type.armor + "" : ""));
                                    label.setColor(Pal.surge);
                                    label.setFontScale(Scl.scl(modUiScale) * 0.5f);
                                    temp.add(label).center();
                                    temp.pack();
                                })
                        ).padLeft(Scl.scl(modUiScale) * 2 * 8f).padBottom(Scl.scl(modUiScale) * 2 * 8f);
                    }));
                }};

                Label label = new Label(() -> {
                    String name = "";
                    if(getTarget() instanceof Unit u && u.type != null)
                        name = u.type.localizedName;
                    if(getTarget() instanceof Building b && b.block != null) {
                        if(getTarget() instanceof ConstructBlock.ConstructBuild cb) name = cb.current.localizedName;
                        else name = b.block.localizedName;
                    }
                    return "[accent]" + (name.length() > 13 ? name.substring(0, 13) + "..." : name) + "[]";
                });
                label.setFontScale(Scl.scl(modUiScale) * 0.75f);

                tt.top();
                tt.add(stack);
                tt.add(label);

                tt.addListener(new Tooltip(tool -> {
                    tool.background(Tex.button).table(to -> {
                        Teamc target = getTarget();
                        to.table(Tex.underline2, tool2 -> {
                            Label targetName = new Label(()->{
                                if(target instanceof Unit u) return u.type.localizedName;
                                else if(target instanceof Building b) return b.block.localizedName;
                                else return "";
                            });
                            targetName.setFontScale(Scl.scl(modUiScale));
                            tool2.add(targetName);
                        }).row();

                        Label ownerName = new Label(()->target instanceof Unit u && u.isPlayer() ? u.getPlayer().name() : "AI");
                        ownerName.setFontScale(Scl.scl(modUiScale));
                        to.add(ownerName).row();

                        Label targetPos = new Label(()->target == null ? "(" + 0 + ", " + 0 + ")" : "(" + Strings.fixed(target.x() / tilesize, 2) + ", " + Strings.fixed(target.y() / tilesize, 2) + ")");
                        targetPos.setFontScale(Scl.scl(modUiScale));
                        to.add(targetPos).row();
                    });

                    tool.update(() -> {
                        NinePatchDrawable patch = (NinePatchDrawable)Tex.button;
                        tool.setBackground(patch.tint(Tmp.c1.set(patch.getPatch().getColor()).a(settings.getInt("uiopacity") / 100f)));
                    });
                }));
                tt.update(() -> tt.setBackground(((NinePatchDrawable)Tex.underline2).tint(getTarget().isNull() ? Color.gray : getTarget().team().color)));
            });
            t.row();
            ScrollPane pane = t.pane(Styles.nonePane, new Table(tt -> {
                for(Element bar : bars){
                    bar.setScale(Scl.scl(modUiScale));
                    tt.add(bar).growX().left();
                    tt.row();
                }
                tt.row();
                tt.add(new WeaponDisplay());
            }).left()).get();
            pane.update(() -> {
                Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                if(pane.hasScroll() && (result == null || !result.isDescendantOf(pane)))
                    scene.setScrollFocus(null);
                weaponScrollPos = pane.getScrollY();
            });

            pane.setOverscroll(false, false);
            pane.setScrollingDisabled(true, false);
            pane.setScrollYForce(weaponScrollPos);

            t.update(() -> {
                NinePatchDrawable patch = (NinePatchDrawable)Tex.button;
                t.setBackground(patch.tint(Tmp.c1.set(patch.getPatch().getColor()).a(settings.getInt("uiopacity") / 100f)));
            });
        });
        table(t -> t.stack(new UnitInfoDisplay().marginBottom(80f), table1).padRight(Scl.scl(modUiScale) * 8 * 8f));

        update(() -> {
            try {
                BarInfo.getInfo(getTarget());
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        });
    }

    public static Teamc getTarget() {
        return SVars.hud == null ? null : SVars.hud.getTarget();
    }

    public void lockTarget() {
        SVars.hud.locked = !SVars.hud.locked;
        SVars.hud.lockedTarget = SVars.hud.locked ? getTarget() : null;
    }

    public void setEvent() {
        if((input.keyDown(KeyCode.shiftRight) || input.keyDown(KeyCode.shiftLeft))) {
            if(input.keyTap(KeyCode.r)) lockTarget();
        }
    }

    public TextureRegion getRegions(int i){
        Teamc target = getTarget();
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

    public Element addBar(int i){
        return new Stack(){{
            add(new Table(t -> {
                t.add(new SBar(
                        () -> BarInfo.strings.get(i),
                        () -> {
                            if (BarInfo.colors.get(i) != Color.clear) lastColors.set(i, BarInfo.colors.get(i));
                            return lastColors.get(i);
                        },
                        () -> BarInfo.numbers.get(i)
                )).width(Scl.scl(modUiScale) * 24 * 8f).height(Scl.scl(modUiScale) * 4 * 8f).growX().left();
            }));
            add(new Table(t -> {
                t.right();
                t.add(new Image(){
                    @Override
                    public void draw() {
                        validate();

                        float x = this.x;
                        float y = this.y;
                        float scaleX = this.scaleX;
                        float scaleY = this.scaleY;
                        Draw.color(Color.white);
                        Draw.alpha(parentAlpha * color.a);

                        TextureRegionDrawable region = new TextureRegionDrawable(getRegions(i));
                        float rotation = getRotation();
                        if(scaleX != 1 || scaleY != 1 || rotation != 0){
                            region.draw(x + imageX, y + imageY, originX - imageX, originY - imageY,
                                    imageWidth, imageHeight, scaleX, scaleY, rotation);
                            return;
                        }
                        region.draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);

                        Draw.color(BarInfo.colors.get(i));
                        if(ScissorStack.push(scissor.set(x, y, imageWidth * scaleX, imageHeight * scaleY * BarInfo.numbers.get(i)))){
                            region.draw(x, y, imageWidth * scaleX, imageHeight * scaleY);
                            ScissorStack.pop();
                        }
                        Draw.reset();
                    }
                }).size(iconMed * Scl.scl(modUiScale) * 0.75f);
            }));
        }};
    }


    static class WeaponDisplay extends Table {
        public WeaponDisplay() {
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
                                    }).size(Scl.scl(modUiScale) * iconLarge);
                                }),
                                new Table(h -> {
                                    h.defaults().growX().height(Scl.scl(modUiScale) * 9f).width(Scl.scl(modUiScale) * iconLarge).padTop(Scl.scl(modUiScale) * 18f);
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
        public UnitInfoDisplay() {
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
                            for(StatusEffect effect : content.statusEffects()){
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
