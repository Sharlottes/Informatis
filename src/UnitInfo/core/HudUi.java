package UnitInfo.core;

import UnitInfo.ui.SBar;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.*;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Scaling;
import mindustry.Vars;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.ui.Bar;
import mindustry.ui.Cicon;

import static arc.Core.scene;
import static mindustry.Vars.content;
import static mindustry.Vars.player;

public class HudUi {
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
        addTable();
    }
    
    public void addTable(){
        Vars.ui.hudGroup.addChild(new Table(table -> {
            Unit unit = getUnit();
            table.update(() -> {
                if(getUnit() != unit) reset(table);
            });
            table.left();
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
                            temp.add(label).center().padLeft(getUnit().type == null ? 0 : getUnit().type.armor < 10 ? 8f : 0f);
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

                    tt.add(new SBar(
                            () -> Core.bundle.format("shar-stat.health", Mathf.round(getUnit().health,1)),
                            () -> Pal.health,
                            () -> Mathf.clamp(getUnit().health / getUnit().type.health)
                    )).growX().left();
                    tt.row();
                    tt.add(new SBar(
                            () -> Core.bundle.format("shar-stat.shield", Mathf.round(getUnit().shield,1)),
                            () -> Pal.surge,
                            () -> {
                                float max1 = ((ShieldRegenFieldAbility)content.units().copy().filter(ut -> ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility) != null).sort(ut -> ((ShieldRegenFieldAbility)ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max).peek().abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max;
                                float max2 = 0f;
                                if(getUnit().type.abilities.find(abil -> abil instanceof ForceFieldAbility) != null) max2 = ((ForceFieldAbility) getUnit().type.abilities.find(abil -> abil instanceof ForceFieldAbility)).max;
                                return Mathf.clamp(getUnit().shield / Math.max(max1, max2));
                            }
                    )).growX().left();
                    tt.row();
                    if(getUnit() instanceof Payloadc) tt.add(new SBar(
                            () -> Core.bundle.format("shar-stat.payloadCapacity", Mathf.round(((Payloadc)getUnit()).payloadUsed()), Mathf.round(unit.type().payloadCapacity)),
                            () -> Pal.items,
                            () -> Mathf.clamp(((Payloadc)getUnit()).payloadUsed() / unit.type().payloadCapacity)
                    )).growX().left();

                });
            });
            table.row();
            UnitType type = getUnit().type;
            table.left();
            try{
                table.table(tx -> {
                    tx.defaults().minSize(24 * 8f);
                    tx.left();
                    tx.table(scene.getStyle(Button.ButtonStyle.class).up, tt -> {
                        tt.defaults().minSize(8 * 8f);
                        tt.left();
                        tt.top();

                        for(int r = 0; r < type.weapons.size; r++){
                            final int i = r;
                            Weapon weapon = type.weapons.get(i);
                            WeaponMount mount = getUnit().mounts[i];
                            TextureRegion region = !weapon.name.equals("") && weapon.outlineRegion.found() ? weapon.outlineRegion : type.icon(Cicon.full);
                            if(type.weapons.size > 1 && i % 3 == 0) tt.row();
                            else if(i % 3 == 0) tt.row();
                            tt.table(weapontable -> {
                                weapontable.left();
                                weapontable.add(new Stack(){{
                                    add(new Table(o -> {
                                        o.left();
                                        o.image(region).size(60).scaling(Scaling.bounded);
                                    }));

                                    add(new Table(h -> {
                                        h.add(new Stack(){{
                                            add(new Table(e -> {
                                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
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
                    });
                });
            }catch(Throwable err){
                Log.info(err);
            }

            table.fillParent = true;
            table.visibility = () ->
                    Vars.ui.hudfrag.shown && !Vars.ui.minimapfrag.shown()
                            && (!Vars.mobile ||
                            !(getUnit().isBuilding() || Vars.control.input.block != null || !Vars.control.input.selectRequests.isEmpty()
                                    && !(Vars.control.input.lastSchematic != null && !Vars.control.input.selectRequests.isEmpty())));
        }));
    }
}
