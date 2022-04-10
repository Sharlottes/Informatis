package UnitInfo.ui.windows;

import UnitInfo.SVars;
import UnitInfo.core.BarInfo;
import UnitInfo.ui.SBar;
import UnitInfo.ui.SIcons;
import UnitInfo.ui.Updatable;
import UnitInfo.ui.windows.WindowTable;
import arc.Core;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.scene.Element;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.Bits;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.ai.formations.FormationPattern;
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

import static UnitInfo.SVars.clear;
import static UnitInfo.SVars.modUiScale;
import static arc.Core.*;
import static mindustry.Vars.*;

public class UnitDisplay extends WindowTable implements Updatable {
    static Seq<Color> lastColors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);
    static final Rect scissor = new Rect();
    float scrollPos;

    public UnitDisplay() {
        super("Unit Display", Icon.units, t -> {});
    }

    @Override
    public void build() {
        top();
        topBar();

        //TODO: add new UnitInfoDisplay(), new WeaponDisplay();
        table(Styles.black8, t -> {
            t.table(Tex.underline2, tt -> {
                tt.stack(
                    new Table(ttt -> {
                        Prov<TextureRegionDrawable> reg = () -> {
                            TextureRegion region = clear;
                            Teamc target = getTarget();
                            if (target instanceof Unit u && u.type != null) region = u.type.uiIcon;
                            else if (target instanceof Building b) {
                                if (target instanceof ConstructBlock.ConstructBuild cb)
                                    region = cb.current.uiIcon;
                                else if (b.block != null) region = b.block.uiIcon;
                            }
                            return new TextureRegionDrawable(region);
                        };
                        Drawable img = reg.get();
                        ImageButton imagebt = new ImageButton(img, img);

                        imagebt.hovered(() -> {
                            Time.run(60 * 2, () -> {
                                if (imagebt.isOver()) lockTarget();
                            });
                        });
                        imagebt.clicked(() -> {
                            Teamc target = getTarget();
                            if (target instanceof Unit u && u.type != null) ui.content.show(u.type);
                            else if (target instanceof Building b && b.block != null) ui.content.show(b.block);
                        });
                        ttt.add(imagebt).update((i) -> {
                            i.getStyle().imageUp = reg.get().tint(Tmp.c1.set(SVars.hud.locked ? Color.red.cpy().shiftHue(2 * Time.time) : Color.white));
                            i.getStyle().imageDown = reg.get().tint(Tmp.c1.mul(Color.darkGray));
                            i.layout();
                        }).size(4 * 8f).get().parent = null;
                    }),
                    new Table(ttt -> {
                        ttt.stack(
                            new Table(temp -> {
                                temp.image(new ScaledNinePatchDrawable(new NinePatch(Icon.defenseSmall.getRegion()), 1));
                                temp.visibility = () -> getTarget() instanceof Unit;
                            }),
                            new Table(temp -> {
                                Label label = new Label(() -> (getTarget() instanceof Unit u && u.type != null ? (int) u.type.armor + "" : ""));
                                label.setColor(Pal.surge);
                                temp.add(label).center();
                                temp.pack();
                            })
                        ).padLeft(2 * 8f).padBottom(2 * 8f).get().parent = null;
                    })
                );

                tt.label(() -> {
                    String name = "";
                    Teamc target = getTarget();
                    if (target instanceof Unit u && u.type != null)
                        name = u.type.localizedName;
                    if (target instanceof Building b && b.block != null) {
                        if (target instanceof ConstructBlock.ConstructBuild cb)
                            name = cb.current.localizedName;
                        else name = b.block.localizedName;
                    }
                    return "[accent]" + (name.length() > 13 ? name.substring(0, 13) + "..." : name) + "[]";
                }).get().parent = null;

                tt.addListener(new Tooltip(to -> {
                    Teamc target = getTarget();

                    to.background(Styles.black6);

                    to.table(Tex.underline2, tool2 -> {
                        tool2.label(() -> {
                            if (target instanceof Unit u) return u.type.localizedName;
                            else if (target instanceof Building b) return b.block.localizedName;
                            else return "";
                        });
                    }).row();
                    to.label(() -> target instanceof Unit u && u.isPlayer() ? u.getPlayer().name() : "AI").row();
                    to.label(() -> target == null
                        ? "(" + 0 + ", " + 0 + ")"
                        : "(" + Strings.fixed(target.x() / tilesize, 2) + ", " + Strings.fixed(target.y() / tilesize, 2) + ")").row();
                }));
                tt.update(() -> tt.setBackground(((NinePatchDrawable) Tex.underline2).tint(getTarget() == null ? Color.gray : getTarget().team().color))).parent = null;
            }).row();
            ScrollPane pane = t.pane(Styles.nonePane, new Table(tt -> {
                for (int i = 0; i < 6; i++) {
                    addBar(tt, i);
                    tt.row();
                }
            }).left()).top().right().grow().get();
            pane.parent = null;
            pane.update(() -> {
                Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                if(pane.hasScroll() && (result == null || !result.isDescendantOf(pane)))
                    scene.setScrollFocus(null);
                scrollPos = pane.getScrollY();
            });

            pane.setOverscroll(false, false);
            pane.setScrollingDisabled(true, false);
            pane.setScrollYForce(scrollPos);
        }).top().right().grow().get().parent = null;
        resizeButton();
    }

    public static Teamc getTarget() {
        return SVars.hud == null ? null : SVars.hud.getTarget();
    }

    public void lockTarget() {
        SVars.hud.locked = !SVars.hud.locked;
        SVars.hud.lockedTarget = SVars.hud.locked ? getTarget() : null;
    }

    public void showMoving() {
        Table table = new Table(Styles.black3).margin(4);
        Vec2 pos = input.mouse();
        table.update(() -> {
            if(Vars.state.isMenu()) table.remove();
            Vec2 vec = Core.camera.project(pos.x, pos.y);
            table.setPosition(vec.x, vec.y, Align.center);
        });

        table.add("hello world").style(Styles.defaultLabel);
        table.pack();
    }

    float angle = 360;
    @Override
    public void update() {
        if((input.keyDown(KeyCode.shiftRight) || input.keyDown(KeyCode.shiftLeft))) {
            if(input.keyTap(KeyCode.f)) {
                showMoving();
            }
            if(input.keyTap(KeyCode.r)) lockTarget();
            if(input.keyTap(KeyCode.r)) {
                player.unit().commandNearby(new FormationPattern() {
                    @Override
                    public Vec3 calculateSlotLocation(Vec3 out, int slot) {
                        angle+=0.3f;
                        float radian = angle / 360 * slot/slots * Mathf.degRad;
                        float sizeScaling = 0.25f;
                        float rotateSpeed = 0.01f;

                        out.set(Tmp.v1.set(this.spacing * (sizeScaling * 5 * Mathf.cos(2 * radian) + sizeScaling * 2 * Mathf.cos(3 * radian)), this.spacing * (sizeScaling * 2 * Mathf.sin(3 * radian) - sizeScaling * 5 * Mathf.sin(2 * radian))).rotateRad(Time.time * rotateSpeed), 0);
                        return out;
                    }
                });
            }
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

    public void addBar(Table table, int i){
        table.add(new SBar(
            () -> BarInfo.strings.get(i),
            () -> {
                if (BarInfo.colors.get(i) != Color.clear) lastColors.set(i, BarInfo.colors.get(i));
                return lastColors.get(i);
            },
            () -> BarInfo.numbers.get(i)
        )).height(4 * 8f).growX().left();
        table.add(new Image(){
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
        }).size(iconMed * 0.75f).left();
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
