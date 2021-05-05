package UnitInfo.core;

import UnitInfo.ui.SBar;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.scene.utils.Elem;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Scaling;
import arc.util.Time;
import mindustry.Vars;
import mindustry.ai.types.FormationAI;
import mindustry.content.Items;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.AmmoTypes;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.ui.Bar;
import mindustry.ui.Cicon;

import static arc.Core.scene;
import static mindustry.Vars.content;
import static mindustry.Vars.player;

public class HudUi {
    Seq<Element> bars = new Seq<>();
    Table weapon = new Table();
    @Nullable UnitType type;
    @Nullable Unit unit;

    float heat;

    public Unit getUnit(){
        Seq<Unit> units = Groups.unit.intersect(Core.input.mouseWorldX(), Core.input.mouseWorldY(), 4, 4);
        if(units.size <= 0) return player.unit();
        Unit unit = units.peek();
        if(unit == null) return player.unit();
        else return unit;
    }
    
    public void reset(Table table){
        table.remove();
        table.reset();
        type = getUnit().type;
        unit = getUnit();
        addTable();
        //addWeapon();
    }

    public void addBars(){
        bars.clear();
        bars.add(
            new SBar(
                () -> Core.bundle.format("shar-stat.health", Mathf.round(getUnit().health,1)),
                () -> Pal.health,
                () -> Mathf.clamp(getUnit().health / getUnit().type.health)
            ),
            new SBar(
                () -> Core.bundle.format("shar-stat.shield", Mathf.round(getUnit().shield,1)),
                () -> Pal.surge,
                () -> {
                    float max1 = ((ShieldRegenFieldAbility)content.units().copy().filter(ut -> ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility) != null).sort(ut -> ((ShieldRegenFieldAbility)ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max).peek().abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max;
                    float max2 = 0f;
                    if(getUnit().type.abilities.find(abil -> abil instanceof ForceFieldAbility) != null) max2 = ((ForceFieldAbility) getUnit().type.abilities.find(abil -> abil instanceof ForceFieldAbility)).max;
                    return Mathf.clamp(getUnit().shield / Math.max(max1, max2));
                }
            )
        );
        bars.add(new Stack(){{
            add(new Table(t -> {
                t.defaults().width(23 * 8f);
                t.defaults().height(4f * 8f);
                t.top();
                t.add(new SBar(
                        () -> Core.bundle.format("shar-stat.itemCapacity", getUnit().stack.amount, getUnit().type.itemCapacity),
                        () -> getUnit().stack.item == null || getUnit().stack.amount <= 0 ? Pal.items : getUnit().stack.item.color.cpy().lerp(Color.white, 0.15f),
                        () -> Mathf.clamp(getUnit().stack.amount / (getUnit().type.itemCapacity * 1f))
                )).growX().left();
            }));
            add(new Table(t -> {
                t.left();
                t.add(new Image(){{
                    update(() -> setDrawable(getUnit().stack.item == null || getUnit().stack.amount <= 0 ? Core.atlas.find("clear") : getUnit().stack.item.icon(Cicon.small)));
                }}).size(30f).scaling(Scaling.bounded).padBottom(4 * 8f).padRight(6 * 8f);
                t.pack();
            }));
        }});
        bars.add(new SBar(
                () -> Core.bundle.format("shar-stat.commandUnits", Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit()), getUnit().type().commandLimit),
                () -> Pal.powerBar.cpy().lerp(Pal.surge.cpy().mul(Pal.lighterOrange), Mathf.absin(Time.time, 7f / (1f + Mathf.clamp(Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit()) / (getUnit().type().commandLimit * 1f))), 1f)),
                () -> Mathf.clamp(Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit()) / (getUnit().type().commandLimit * 1f))
        ));
        bars.add(new Stack(){{
            add(new Table(t -> {
                t.defaults().width(23 * 8f);
                t.defaults().height(4f * 8f);
                t.top();
                t.add(new SBar(
                        () -> Vars.state.rules.unitAmmo ? Core.bundle.format("shar-stat.ammos", getUnit().ammo, getUnit().type.ammoCapacity) : Core.bundle.format("shar-stat.infinityAmmos"),
                        () -> player.dead() || player.unit() instanceof BlockUnitc ? Pal.ammo : getUnit().type.ammoType.color,
                        () -> Vars.state.rules.unitAmmo ? getUnit().ammof() : 1f
                )).growX().left();
            }));
            add(new Table(t -> {
                t.left();
                t.add(new Image(){{
                    update(() -> {
                        TextureRegion region = Items.copper.icon(Cicon.small);
                        if(getUnit().type != null){
                            if(getUnit().type.ammoType == AmmoTypes.thorium) region = Items.thorium.icon(Cicon.small);
                            if(getUnit().type.ammoType == AmmoTypes.power || getUnit().type.ammoType == AmmoTypes.powerLow || getUnit().type.ammoType == AmmoTypes.powerHigh) region = Icon.powerSmall.getRegion();
                        }
                        setDrawable(region);
                    });
                }}).size(30f).scaling(Scaling.bounded).padBottom(4 * 8f).padRight(6 * 8f);
                t.pack();
            }));
        }});
        bars.add(new SBar(
                () -> Core.bundle.format("shar-stat.payloadCapacity", Mathf.round(((Payloadc)getUnit()).payloadUsed()), Mathf.round(getUnit().type().payloadCapacity)),
                () -> Pal.items,
                () -> Mathf.clamp(((Payloadc)getUnit()).payloadUsed() / getUnit().type().payloadCapacity)
        ));
    }

    public void addWeapon(){
        weapon = new Table(tx -> {
            tx.defaults().minSize(12 * 8f);
            tx.left();
            tx.table(scene.getStyle(Button.ButtonStyle.class).up, tt -> {
                tt.defaults().minSize(4 * 8f);
                tt.left();
                tt.top();
                int amount = 0;
                if(type != null) amount = type.weapons.size;

                for(int r = 0; r < amount; r++){
                    Weapon weapon = type.weapons.get(r);
                    WeaponMount mount = unit.mounts[r];
                    TextureRegion region = !weapon.name.equals("") && weapon.outlineRegion.found() ? weapon.outlineRegion : type.icon(Cicon.full);
                    if(type.weapons.size > 1 && r % 3 == 0) tt.row();
                    else if(r % 3 == 0) tt.row();
                    tt.table(weapontable -> {
                        weapontable.left();
                        weapontable.add(new Stack(){{
                            add(new Table(o -> {
                                o.left();
                                o.image(region).size(30).scaling(Scaling.bounded);
                            }));

                            add(new Table(h -> {
                                h.add(new Stack(){{
                                    add(new Table(e -> {
                                        e.defaults().growX().height(9).width(21f).padRight(2*8).padTop(8*2f);
                                        Bar reloadBar = new Bar(
                                                () -> "",
                                                () -> Pal.accent.cpy().lerp(Color.orange, mount.reload / weapon.reload),
                                                () -> mount.reload / weapon.reload);
                                        e.add(reloadBar);
                                        e.pack();
                                    }));
                                }}).padTop(2*8).padLeft(2*8);
                                h.pack();
                            }));
                        }}).left();
                    }).left();
                    tt.center();
                }
            }).padRight(24 * 8f);
            tx.row();
            tx.table(scene.getStyle(Button.ButtonStyle.class).up, t1 -> t1.table(tt -> {
                tt.defaults().minSize(4 * 8f);
                tt.left();
                tt.top();
                int amount = 0;
                if(type != null) amount = Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit());
                Seq<Unit> units = new Seq<>();
                units = Groups.unit.copy(units).filter(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit());
                for(int r = 0; r < amount; r++){
                    Unit unit = units.get(r);
                    TextureRegion region = unit.type.icon(Cicon.full);
                    if(type.weapons.size > 1 && r % 3 == 0) tt.row();
                    else if(r % 3 == 0) tt.row();
                    tt.table(unittable -> {
                        unittable.left();
                        unittable.add(new Stack(){{
                            add(new Table(o -> {
                                o.left();
                                o.image(region).size(30).scaling(Scaling.bounded);
                            }));

                            add(new Table(h -> {
                                h.add(new Stack(){{
                                    add(new Table(e -> {
                                        e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                                        e.left();
                                        Bar healthBar = new Bar(
                                                () -> "",
                                                () -> Pal.health,
                                                unit::healthf);
                                        e.add(healthBar).left();
                                        e.pack();
                                    }));
                                    add(new Table(e -> e.add(new Stack(){{
                                        add(new Table(t -> {
                                            t.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*5f);
                                            t.left();
                                            t.add(new Bar(
                                                    () -> "",
                                                    () -> unit.stack.item == null || unit.stack.amount <= 0 ? Pal.items : unit.stack.item.color.cpy().lerp(Color.white, 0.15f),
                                                    () -> Mathf.clamp(unit.stack.amount / (unit.type.itemCapacity * 1f))
                                            )).growX().left();
                                        }));
                                        add(new Table(t -> {
                                            t.left();
                                            t.add(new Image(){{
                                                update(() -> setDrawable(unit.stack.item == null || unit.stack.amount <= 0 ? Core.atlas.find("clear") : unit.stack.item.icon(Cicon.small)));
                                            }}).size(30f).scaling(Scaling.bounded).padBottom(4 * 8f).padLeft(2 * 8f);
                                            t.pack();
                                        }));
                                    }})));
                                }}).padTop(2*8).padRight(2*8);
                                h.pack();
                            }));
                        }}).left();
                    }).left();
                    tt.center();
                }
            })).padRight(24 * 8f);
        });
    }
    public void addTable(){

        Vars.ui.hudGroup.addChild(new Table(table -> {
            table.left();
            addBars();
            table.table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.table(Tex.underline2, tt -> {
                    tt.top();
                    tt.add(new Stack(){{
                        add(new Table(temp -> {
                            temp.left();
                            temp.add(new Image(Icon.defense)).center();
                        }));
                        add(new Table(temp -> {
                            temp.left();
                            Label label = new Label(() -> (int)(getUnit().type == null ? 0 : getUnit().type.armor) + "");
                            label.setColor(Pal.surge);
                            label.setSize(0.6f);
                            temp.add(label).center().padLeft(getUnit().type == null ? 8f : getUnit().type.armor < 10 ? 8f : 0f);
                            temp.pack();
                        }));
                    }}).growX().left().padRight(3 * 8f);
                    tt.add(new Label(() ->{
                        if(getUnit() != null && getUnit().type != null) return "[accent]" + getUnit().type.localizedName + "[]";
                        return "";
                    })).center();
                });
                t.defaults().size(25 * 8f);
                t.row();
                t.table(tt -> {
                    tt.defaults().width(23 * 8f);
                    tt.defaults().height(4f * 8f);
                    tt.top();
                    for(Element bar : bars){
                        tt.add(bar).growX().left();
                        tt.row();
                    }
                });
            }).padRight(24 * 8f);
            table.row();
            Unit unittemp = getUnit();
            table.update(() -> {
                heat += Time.delta;
                if (heat >= 16 && unittemp != getUnit()) {
                    heat = 0f;
                    type = getUnit().type;
                    unit = getUnit();

                    table.removeChild(weapon);
                    table.removeChild(weapon);
                    addWeapon();
                    table.row();

                    table.add(weapon);
                }
            });

            table.fillParent = true;
            table.visibility = () ->
                    Vars.ui.hudfrag.shown && !Vars.ui.minimapfrag.shown()
                            && (!Vars.mobile ||
                            !(getUnit().isBuilding() || Vars.control.input.block != null || !Vars.control.input.selectRequests.isEmpty()
                                    && !(Vars.control.input.lastSchematic != null && !Vars.control.input.selectRequests.isEmpty())));
        }));
    }
}
