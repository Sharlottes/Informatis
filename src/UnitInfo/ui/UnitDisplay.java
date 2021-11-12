package UnitInfo.ui;

import UnitInfo.SVars;
import UnitInfo.core.BarInfo;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.Element;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.Elem;
import arc.struct.Bits;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
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

public class UnitDisplay extends Table {
    static ImageButton lockButton;
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
                        ttt.image(() -> {
                            TextureRegion region = clear;
                            if(getTarget() instanceof Unit u && u.type != null) region = u.type.uiIcon;
                            else if(getTarget() instanceof Building b) {
                                if(getTarget() instanceof ConstructBlock.ConstructBuild cb) region = cb.current.uiIcon;
                                else if(b.block != null) region = b.block.uiIcon;
                            }
                            return region;
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

                TextButton button = Elem.newButton("?", Styles.clearPartialt, () -> {
                    if(getTarget() instanceof Unit u && u.type != null)
                        ui.content.show(u.type);
                    if(getTarget() instanceof Building b && b.block != null) {
                        ui.content.show(b.block);
                    }
                });
                button.visibility = () -> getTarget() != null;
                button.update(() -> lockButton.getStyle().imageUp = Icon.lock.tint(SVars.hud.locked ? Pal.accent : Color.white));
                button.getLabel().setFontScale(Scl.scl(modUiScale));

                lockButton = Elem.newImageButton(Styles.clearPartiali, Icon.lock.tint(SVars.hud.locked ? Pal.accent : Color.white), 3 * 8f * Scl.scl(modUiScale), () -> {
                    SVars.hud.locked = !SVars.hud.locked;
                    SVars.hud.lockedTarget = SVars.hud.locked ? getTarget() : null;
                });
                lockButton.visibility = () -> !getTarget().isNull();

                tt.top();
                tt.add(stack);
                tt.add(label);
                tt.add(button).size(Scl.scl(modUiScale) * 3 * 8f);
                tt.add(lockButton);

                tt.addListener(new Tooltip(tool -> tool.background(Tex.button).table(to -> {
                    to.table(Tex.underline2, tool2 -> {
                        Label label2 = new Label(()->{
                            if(getTarget() instanceof Unit u){
                                if(u.isPlayer()) return u.getPlayer().name;
                                if(u.type != null) return u.type.localizedName;
                            }
                            else if(getTarget() instanceof Building b) return b.block.localizedName;
                            return "";
                        });
                        label2.setFontScale(Scl.scl(modUiScale));
                        tool2.add(label2);
                    });
                    to.row();
                    Label label2 = new Label(()->getTarget() == null ? "(" + 0 + ", " + 0 + ")" : "(" + Strings.fixed(getTarget().x() / tilesize, 2) + ", " + Strings.fixed(getTarget().y() / tilesize, 2) + ")");
                    label2.setFontScale(Scl.scl(modUiScale));
                    to.add(label2);
                })));
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
        table(t -> t.stack(table1, new UnitInfoDisplay()).padRight(Scl.scl(modUiScale) * 8 * 8f));

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

    public void setEvent() {
        if((input.keyDown(KeyCode.shiftRight) || input.keyDown(KeyCode.shiftLeft))) {
            if(input.keyTap(KeyCode.r) && lockButton != null) lockButton.change();
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
            table(table1 -> {
                table1.left().top();

                table1.table().update(t -> {
                    t.clear();
                    if(getTarget() instanceof Unit u && u.item() != null) {
                        if(state.rules.damageExplosions)  {
                            float power = u.item().charge * Mathf.pow(u.stack().amount, 1.11f) * 160f;
                            int powerAmount = (int)Mathf.clamp(power / 700, 0, 8);
                            int powerLength = 5 + Mathf.clamp((int)(Mathf.pow(power, 0.98f) / 500), 1, 18);
                            float powerDamage = 3 + Mathf.pow(power, 0.35f);

                            if(powerAmount > 0) {
                                t.stack(
                                        new Table(tt -> {
                                            tt.image(Icon.power.getRegion()).size(8 * 3f * Scl.scl(modUiScale));
                                        }),
                                        new Table(tt -> {
                                            tt.right().top();
                                            Label label = new Label(()->powerAmount + "");
                                            label.setFontScale(0.75f * Scl.scl(modUiScale));
                                            tt.add(label).padBottom(4f).padLeft(4f);
                                            tt.pack();
                                        })
                                ).pad(4).visible(() -> state.rules.damageExplosions&&powerAmount > 0);
                            }

                            if(u.item().flammability > 1) {
                                float flammability = u.item().flammability * u.stack().amount / 1.9f;
                                int fireAmount = (int)Mathf.clamp(flammability / 4, 0, 30);
                                t.stack(
                                        new Table(tt -> {
                                            tt.image(StatusEffects.burning.uiIcon).size(8 * 3f * Scl.scl(modUiScale));
                                        }),
                                        new Table(tt -> {
                                            tt.right().top();
                                            Label label = new Label(()->fireAmount+"");
                                            label.setFontScale(0.75f * Scl.scl(modUiScale));
                                            tt.add(label).padBottom(4f).padLeft(4f);
                                            tt.pack();
                                        })
                                ).pad(4).visible(() -> state.rules.damageExplosions&&u.item().flammability > 1);
                            }
                        }

                        float explosiveness = 2f + u.item().explosiveness * u.stack().amount * 1.53f;
                        float explosivenessMax = 2f + u.item().explosiveness * u.stack().amount * 1.53f;
                        int exploAmount = explosiveness <= 2 ? 0 : Mathf.clamp((int)(explosiveness / 11), 1, 25);
                        int exploAmountMax = explosivenessMax <= 2 ? 0 : Mathf.clamp((int)(explosivenessMax / 11), 1, 25);
                        float exploRadiusMin = Mathf.clamp(u.bounds() / 2f + explosiveness, 0, 50f) * (1f / exploAmount);
                        float exploRadiusMax = Mathf.clamp(u.bounds() / 2f + explosiveness, 0, 50f);
                        float exploDamage = explosiveness / 2f;

                        if(exploAmount > 0){
                            t.stack(
                                    new Table(tt -> {
                                        tt.image(Icon.modeAttack.getRegion()).size(8 * 3f * Scl.scl(modUiScale));
                                    }),
                                    new Table(tt -> {
                                        tt.right().top();
                                        Label label = new Label(()->""+ Strings.fixed(exploDamage * exploAmount, 1));
                                        label.setFontScale(0.75f * Scl.scl(modUiScale));
                                        label.setColor(Tmp.c1.set(Color.white).lerp(Pal.health, (exploAmount*1f)/exploAmountMax));
                                        tt.add(label).padBottom(4f).padLeft(8f);
                                        tt.pack();
                                    })
                            ).pad(4).visible(() -> exploAmount>0);
                        }
                    }
                }).growX().visible(() -> getTarget() instanceof Unit);
                table1.row();

                float[] count = new float[]{-1};
                table1.table().update(t -> {
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
                }).growX().visible(() -> getTarget() instanceof Payloadc p && p.payloadUsed() > 0).colspan(2);
                table1.row();

                Bits statuses = new Bits();
                table1.table().update(t -> {
                    t.left();
                    if(getTarget() instanceof Statusc st){
                        Bits applied = st.statusBits();
                        if(!statuses.equals(applied)){
                            t.clear();

                            if(applied != null){
                                for(StatusEffect effect : content.statusEffects()){
                                    if(applied.get(effect.id) && !effect.isHidden()){
                                        t.image(effect.uiIcon).size(iconSmall).get().addListener(new Tooltip(l -> l.label(() ->
                                                effect.localizedName + " [lightgray]" + UI.formatTime(st.getDuration(effect))).style(Styles.outlineLabel)));
                                    }
                                }
                                statuses.set(applied);
                            }
                        }
                    }
                }).left();
            }).get();
        }
    }
}
