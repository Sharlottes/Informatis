package UnitInfo.core;

import UnitInfo.ui.SBar;
import arc.Core;
import arc.func.Func;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.style.TransformDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.scene.utils.Elem;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.ai.types.FormationAI;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.entities.units.WeaponMount;
import mindustry.game.SpawnGroup;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LiquidTurret;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.power.ConditionalConsumePower;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.consumers.ConsumeType;

import java.util.Objects;

import static arc.Core.scene;
import static arc.Core.settings;
import static mindustry.Vars.*;

public class HudUi {
    Seq<Element> bars = new Seq<>();
    Table weapon = new Table();
    Table core = new Table();
    Table wave = new Table();
    Table waveTable;

    @Nullable UnitType type;
    @Nullable Unit unit;
    Element image;

    float heat;
    float heat2;
    float scrollPos;
    int maxwave;
    int coreamount;

    public Unit getUnit(){
        Seq<Unit> units = Groups.unit.intersect(Core.input.mouseWorldX(), Core.input.mouseWorldY(), 4, 4);
        if(units.size <= 0) return player.unit();
        Unit unit = units.peek();
        if(unit == null) return player.unit();
        else return unit;
    }

    public void addBars(){
        bars.clear();
        bars.add(
            new SBar(
                () -> Core.bundle.format("shar-stat.health", Mathf.round(getUnit().health, 1)),
                () -> Pal.health,
                () -> Mathf.clamp(getUnit().health / getUnit().type.health)
            )
        );
        SBar secondBar = new SBar(
                () -> {
                    if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild) {
                        float value = Mathf.clamp(((Turret.TurretBuild)((BlockUnitUnit)getUnit()).tile()).reload / ((Turret)((BlockUnitUnit)getUnit()).tile().block).reloadTime) * 100f;
                        return Core.bundle.format("shar-stat.reload", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2)));
                    }
                    return Core.bundle.format("shar-stat.shield", Mathf.round(getUnit().shield,1));
                },
                () ->{
                    if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild) {
                        return Pal.accent.cpy().lerp(Color.orange, Mathf.clamp(((Turret.TurretBuild)((BlockUnitUnit)getUnit()).tile()).reload / ((Turret)((BlockUnitUnit)getUnit()).tile().block).reloadTime));
                    }
                    return Pal.surge;
                },
                () -> {
                    if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild) {
                        return Mathf.clamp(((Turret.TurretBuild)((BlockUnitUnit)getUnit()).tile()).reload / ((Turret)((BlockUnitUnit)getUnit()).tile().block).reloadTime);
                    }
                    float max1 = ((ShieldRegenFieldAbility)content.units().copy().filter(ut -> ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility) != null).sort(ut -> ((ShieldRegenFieldAbility)ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max).peek().abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max;
                    float max2 = 0f;
                    if(getUnit().type.abilities.find(abil -> abil instanceof ForceFieldAbility) != null) max2 = ((ForceFieldAbility) getUnit().type.abilities.find(abil -> abil instanceof ForceFieldAbility)).max;
                    return Mathf.clamp(getUnit().shield / Math.max(max1, max2));
                }
        );
        bars.add(secondBar);

        bars.add(new Stack(){{
            add(new Table(t -> {
                t.defaults().width(23 * 8f);
                t.defaults().height(4f * 8f);
                t.top();
                t.add(new SBar(
                        () -> {
                            if(getUnit() instanceof BlockUnitUnit){
                                if(((BlockUnitUnit)getUnit()).tile() instanceof ItemTurret.ItemTurretBuild) {
                                    return Core.bundle.format("shar-stat.itemAmmo", ((ItemTurret.ItemTurretBuild) ((BlockUnitUnit)getUnit()).tile()).totalAmmo, ((ItemTurret)((BlockUnitUnit)getUnit()).tile().block).maxAmmo);

                                }
                                else if(((BlockUnitUnit)getUnit()).tile() instanceof LiquidTurret.LiquidTurretBuild){
                                    LiquidTurret.LiquidTurretBuild entity = ((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit)getUnit()).tile());
                                    Func<Building, Liquid> current;
                                    current = entity1 -> entity1.liquids == null ? Liquids.water : entity1.liquids.current();

                                    return Core.bundle.format("shar-stat.liquidAmmo", entity == null || entity.liquids == null ? 0 : Mathf.round(entity.liquids.get(current.get(entity)) * 10) / 10.0 + " / " + Mathf.round(entity.block.liquidCapacity));
                                }
                                else if(((BlockUnitUnit)getUnit()).tile() instanceof PowerTurret.PowerTurretBuild){

                                    PowerTurret.PowerTurretBuild entity = ((PowerTurret.PowerTurretBuild)((BlockUnitUnit)getUnit()).tile());
                                    ConsumePower cons = entity.block.consumes.getPower();
                                    double max = (Math.round(cons.usage * 10) / 10.0) * 60;
                                    double v = (Math.round(((ConditionalConsumePower)entity.block.consumes.get(ConsumeType.power)).requestedPower(entity) * 10) / 10.0);
                                    return Core.bundle.format("shar-stat.power", (Math.round(entity.power.status * v * 10) / 10.0) * 60, max);
                                }
                            }

                            return Core.bundle.format("shar-stat.itemCapacity", getUnit().stack.amount, getUnit().type.itemCapacity);
                        },
                        () -> {
                            if(getUnit() instanceof BlockUnitUnit){
                                if(((BlockUnitUnit)getUnit()).tile() instanceof ItemTurret.ItemTurretBuild) {
                                    if(((ItemTurret.ItemTurretBuild)((BlockUnitUnit) getUnit()).tile()).hasAmmo()) return ((ItemTurret) ((BlockUnitUnit) getUnit()).tile().block).ammoTypes.findKey(((ItemTurret.ItemTurretBuild)((BlockUnitUnit) getUnit()).tile()).peekAmmo(), true).color;
                                    else return Pal.ammo;
                                }
                                else if(((BlockUnitUnit)getUnit()).tile() instanceof LiquidTurret.LiquidTurretBuild){
                                    LiquidTurret.LiquidTurretBuild entity = ((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit)getUnit()).tile());
                                    Func<Building, Liquid> current;
                                    current = entity1 -> entity1.liquids == null ? Liquids.water : entity1.liquids.current();

                                    return current.get(entity).color;
                                }
                                else if(((BlockUnitUnit)getUnit()).tile() instanceof PowerTurret.PowerTurretBuild){
                                    return Pal.powerBar;
                                }
                            }
                            else if(getUnit().stack.item == null || getUnit().stack.amount <= 0) return Pal.items;

                            return getUnit().stack.item.color.cpy().lerp(Color.white, 0.15f);
                        },
                        () -> {
                            if(getUnit() instanceof BlockUnitUnit) {
                                if(((BlockUnitUnit)getUnit()).tile() instanceof ItemTurret.ItemTurretBuild) {
                                    return ((ItemTurret.ItemTurretBuild) ((BlockUnitUnit) getUnit()).tile()).totalAmmo / (((ItemTurret) ((BlockUnitUnit) getUnit()).tile().block).maxAmmo * 1f);
                                }
                                else if(((BlockUnitUnit)getUnit()).tile() instanceof LiquidTurret.LiquidTurretBuild){
                                    LiquidTurret.LiquidTurretBuild entity = ((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit)getUnit()).tile());
                                    Func<Building, Liquid> current;
                                    current = entity1 -> entity1.liquids == null ? Liquids.water : entity1.liquids.current();

                                    return entity == null || entity.liquids == null ? 0f : entity.liquids.get(current.get(entity)) / entity.block.liquidCapacity;
                                }
                                else if(((BlockUnitUnit)getUnit()).tile() instanceof PowerTurret.PowerTurretBuild){
                                    PowerTurret.PowerTurretBuild entity = ((PowerTurret.PowerTurretBuild)((BlockUnitUnit)getUnit()).tile());
                                    ConsumePower cons = entity.block.consumes.getPower();

                                    double max = (Math.round(cons.usage * 10) / 10.0) * 60;
                                    double v = (Math.round(((ConditionalConsumePower)entity.block.consumes.get(ConsumeType.power)).requestedPower(entity) * 10) / 10.0);
                                    return (float) (((Math.round(entity.power.status * v * 10) / 10.0) * 60) / max);
                                }
                            }
                            return Mathf.clamp(getUnit().stack.amount / (getUnit().type.itemCapacity * 1f));
                        }
                )).growX().left();
            }));
            add(new Table()
            {{
                left();
                update(() -> {

                    if(!Core.settings.getBool("unitui") || !(getUnit() instanceof BlockUnitUnit) || (
                            !(((BlockUnitUnit)getUnit()).tile() instanceof ItemTurret.ItemTurretBuild)
                            && !(((BlockUnitUnit)getUnit()).tile() instanceof LiquidTurret.LiquidTurretBuild)
                            && !(((BlockUnitUnit)getUnit()).tile() instanceof PowerTurret.PowerTurretBuild)
                        )){
                        clearChildren();
                        image = null;
                        return;
                    }

                    if(getUnit() instanceof BlockUnitUnit){
                        Element imaget = new Element();
                        if(((BlockUnitUnit)getUnit()).tile() instanceof ItemTurret.ItemTurretBuild){
                            MultiReqImage itemReq = new MultiReqImage();
                            for(Item item : ((ItemTurret) ((BlockUnitUnit) getUnit()).tile().block).ammoTypes.keys())
                                itemReq.add(new ReqImage(item.icon(Cicon.tiny), () -> ((ItemTurret.ItemTurretBuild)((BlockUnitUnit) getUnit()).tile()).hasAmmo()));
                            imaget = itemReq;

                            if(((ItemTurret.ItemTurretBuild)((BlockUnitUnit) getUnit()).tile()).hasAmmo())
                                imaget = new Image(((ItemTurret) ((BlockUnitUnit) getUnit()).tile().block).ammoTypes.findKey(((ItemTurret.ItemTurretBuild)((BlockUnitUnit) getUnit()).tile()).peekAmmo(), true).icon(Cicon.small));

                        }
                        else if(((BlockUnitUnit)getUnit()).tile() instanceof LiquidTurret.LiquidTurretBuild){
                            LiquidTurret.LiquidTurretBuild entity = ((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit)getUnit()).tile());
                            Func<Building, Liquid> current;
                            current = entity1 -> entity1.liquids == null ? Liquids.water : entity1.liquids.current();

                            MultiReqImage liquidReq = new MultiReqImage();
                            for(Liquid liquid : ((LiquidTurret) ((BlockUnitUnit) getUnit()).tile().block).ammoTypes.keys())
                                liquidReq.add(new ReqImage(liquid.icon(Cicon.tiny), () -> ((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit) getUnit()).tile()).hasAmmo()));
                            imaget = liquidReq;

                            if(((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit) getUnit()).tile()).hasAmmo())
                                imaget = new Image(current.get(entity).icon(Cicon.small));
                        }
                        else if(((BlockUnitUnit)getUnit()).tile() instanceof PowerTurret.PowerTurretBuild){
                            PowerTurret.PowerTurretBuild entity = ((PowerTurret.PowerTurretBuild)((BlockUnitUnit)getUnit()).tile());
                            ConsumePower cons = entity.block.consumes.getPower();



                            double max = (Math.round(cons.usage * 10) / 10.0) * 60;
                            double v = (Math.round(((ConditionalConsumePower)entity.block.consumes.get(ConsumeType.power)).requestedPower(entity) * 10) / 10.0);
                            float amount = (float) (((Math.round(entity.power.status * v * 10) / 10.0) * 60) / max);
                            //float amount = Mathf.zero(cons.requestedPower(entity)) && entity.power.graph.getPowerProduced() + entity.power.graph.getBatteryStored() > 0f ? 1f : entity.power.status;

                            imaget = new PrograssedReqImage(Icon.power.getRegion(), () -> amount >= 0.99f, amount);
                            if(amount >= 0.999f) imaget = new Image(Icon.power.getRegion());
                        }

                        if(image != null){
                            if(imaget.getClass() != image.getClass() || imaget.getClass() == Image.class){
                                clearChildren();
                                add(imaget).size(Cicon.small.size).padBottom(2 * 8f).padRight(3 * 8f);
                                image = imaget;
                            }
                        }
                        else {
                            add(imaget).size(Cicon.small.size).padBottom(2 * 8f).padRight(3 * 8f);
                            image = imaget;
                        }
                    }
                });
                pack();
            }});
            add(new Table(t -> {
                t.left();

                t.add(new Image(){{
                    update(() -> {
                        if(!Core.settings.getBool("unitui")) return;
                        setDrawable(getUnit().stack.item == null || getUnit().stack.amount <= 0 ? Core.atlas.find("clear") : getUnit().stack.item.icon(Cicon.small));
                    });
                }

                    @Override
                    public void draw() {
                        if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof ItemTurret.ItemTurretBuild) return;
                        super.draw();
                    }
                }).size(30f).scaling(Scaling.bounded).padBottom(4 * 8f).padRight(6 * 8f);
                t.pack();
            }));
        }});
        bars.add(new SBar(
                () -> {
                    if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit) getUnit()).tile() instanceof Turret.TurretBuild){
                        Turret.TurretBuild entity = ((Turret.TurretBuild)((BlockUnitUnit) getUnit()).tile());
                        float value = Mathf.clamp(heat2 / ((Turret)entity.block).chargeTime) * 100f;
                        return Core.bundle.format("shar-stat.charge", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2)));
                    }
                    return Core.bundle.format("shar-stat.commandUnits", Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit()), getUnit().type().commandLimit);
                },
                () -> {
                    if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit) getUnit()).tile() instanceof Turret.TurretBuild){
                        Turret.TurretBuild entity = ((Turret.TurretBuild)((BlockUnitUnit) getUnit()).tile());
                        return Pal.surge.cpy().lerp(Pal.accent, heat2 / ((Turret)entity.block).chargeTime);
                    }
                    return Pal.powerBar.cpy().lerp(Pal.surge.cpy().mul(Pal.lighterOrange), Mathf.absin(Time.time, 7f / (1f + Mathf.clamp(Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit()) / (getUnit().type().commandLimit * 1f))), 1f));
                },
                () -> {
                    if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit) getUnit()).tile() instanceof Turret.TurretBuild){
                        Turret.TurretBuild entity = ((Turret.TurretBuild)((BlockUnitUnit) getUnit()).tile());
                        return heat2 / ((Turret)entity.block).chargeTime;
                    }
                    return Mathf.clamp(Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit()) / (getUnit().type().commandLimit * 1f));
                }

        ));
        bars.add(new SBar(
                () -> Core.bundle.format("shar-stat.payloadCapacity", Mathf.round(Mathf.sqrt(((Payloadc)getUnit()).payloadUsed())) + "²", Mathf.round(Mathf.sqrt(getUnit().type().payloadCapacity)) + "²"),
                () -> Pal.items,
                () -> Mathf.clamp(((Payloadc)getUnit()).payloadUsed() / getUnit().type().payloadCapacity),
                () -> getUnit() instanceof Payloadc
        ));
        bars.add(new Stack(){{
            add(new Table(t -> {
                t.defaults().width(23 * 8f);
                t.defaults().height(4f * 8f);
                t.top();
                t.add(new SBar(
                        () -> Core.bundle.format("shar-stat.ammos", getUnit().ammo, getUnit().type.ammoCapacity),
                        () -> getUnit().dead() || getUnit() instanceof BlockUnitc ? Pal.ammo : getUnit().type.ammoType.color,
                        () -> getUnit().ammof(),
                        () -> Vars.state.rules.unitAmmo
                )).growX().left();
            }));
            add(new Table(t -> {
                t.left();
                t.add(new Image(){{
                    update(() -> {
                        if(!Core.settings.getBool("unitui")) return;

                        if(!Vars.state.rules.unitAmmo){
                            setDrawable(Core.atlas.find("clear"));
                            return;
                        }
                        TextureRegion region = Items.copper.icon(Cicon.small);
                        if( getUnit().type != null){
                            if(getUnit().type.ammoType == AmmoTypes.thorium) region = Items.thorium.icon(Cicon.small);
                            if(getUnit().type.ammoType == AmmoTypes.power || getUnit().type.ammoType == AmmoTypes.powerLow || getUnit().type.ammoType == AmmoTypes.powerHigh) region = Icon.powerSmall.getRegion();
                        }
                        setDrawable(region);
                    });
                }}).size(30f).scaling(Scaling.bounded).padBottom(4 * 8f).padRight(6 * 8f);
                t.pack();
            }));
        }});
    }

    public void addWeapon(){
        weapon = new Table(tx -> {
            tx.defaults().minSize(12 * 8f);
            tx.left();

            if(settings.getBool("commandedunitui") && Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit()) != 0)
                tx.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t1 -> t1.table(tt -> {
                    tt.defaults().width(24/3f * 8f);
                    tt.defaults().minHeight(12/3f * 8f);
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
                                                t.add(new Stack(){{
                                                    add(new Table(tt ->
                                                        tt.add(new Image(){{
                                                            update(() -> {
                                                                if(!Core.settings.getBool("weaponui")) return;
                                                                setDrawable(unit.stack.item == null || unit.stack.amount <= 0 ? Core.atlas.find("clear") : unit.stack.item.icon(Cicon.small));
                                                            });
                                                        }}).size(2.5f * 8f).scaling(Scaling.bounded).padBottom(4 * 8f).padLeft(2 * 8f)
                                                    ));
                                                    Table table = new Table(tt -> {
                                                        Label label = new Label(() -> unit.stack.item == null || unit.stack.amount <= 0 ? "" : unit.stack.amount + "");

                                                        tt.add(label).padBottom(1 * 8f).padLeft(2 * 8f);
                                                        tt.pack();
                                                    });
                                                    add(table);
                                                }});
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
                })){
                    @Override
                    protected void drawBackground(float x, float y) {
                        if(getBackground() == null) return;
                        Color color = this.color;
                        Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                        getBackground().draw(x, y, width, height);
                    }
                }).padRight(24 * 8f);
            tx.row();
            if(settings.getBool("weaponui") && type != null && type.weapons.size != 0) tx.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, tt -> {

                tt.defaults().width(24/3f * 8f);
                tt.defaults().minHeight(12/3f * 8f);
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
                                o.add(new Image(region){
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
                                                getDrawable().draw(x + imageX, y + imageY, originX - imageX, originY - imageY,
                                                        imageWidth, imageHeight, scaleX, scaleY, rotation);
                                                return;
                                            }
                                        }

                                        float recoil = -((mount.reload) / weapon.reload * weapon.recoil);
                                        y += recoil;
                                        if(getDrawable() != null) getDrawable().draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
                                    }
                                }).size(6 * 8f).scaling(Scaling.bounded);
                            }));

                            add(new Table(h -> {
                                h.add(new Stack(){{
                                    add(new Table(e -> {
                                        e.defaults().growX().height(9).width(31.5f).padTop(9*2f);
                                        Bar reloadBar = new Bar(
                                                () -> "",
                                                () -> Pal.accent.cpy().lerp(Color.orange, mount.reload / weapon.reload),
                                                () -> mount.reload / weapon.reload);
                                        e.add(reloadBar);
                                        e.pack();
                                    }));
                                }}).padLeft(8f);
                                h.pack();
                            }));
                        }}).left();
                    }).left();
                    tt.center();
                }
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Color color = this.color;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                }
            }).padRight(24 * 8f);
            tx.setColor(tx.color.cpy().a(1f));
        });
    }
    public void addTable(){
        ui.hudGroup.addChild(new Table(table -> {
            table.left();
            addBars();
            table.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.defaults().width(25 * 8f);

                t.table(Tex.underline2, tt -> {
                    Stack stack = new Stack(){{
                        add(new Table(ttt -> ttt.add(new Image(){{
                            update(() -> {
                                if(!Core.settings.getBool("unitui")) return;
                                TextureRegion region = Core.atlas.find("clear");
                                if(getUnit() instanceof BlockUnitUnit && getUnit().type != null) region = ((BlockUnitUnit)getUnit()).tile().block.icon(Cicon.large);
                                else if(getUnit() != null && getUnit().type != null) region = getUnit().type.icon(Cicon.large);
                                setDrawable(region);
                            });
                        }})));
                        add(new Table(ttt -> {
                            ttt.top().left();
                            ttt.add(new Stack(){{
                                add(new Table(temp -> {
                                    temp.left();
                                    temp.add(new Image(Icon.defense)).center();
                                }){
                                    @Override
                                    public void draw() {
                                        if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild) return;
                                        super.draw();
                                    }
                                });
                                add(new Table(temp -> {
                                    temp.left();
                                    Label label = new Label(() -> (int)(getUnit().type == null ? 0 : getUnit().type.armor) + "");
                                    label.setColor(Pal.surge);
                                    label.setSize(0.6f);
                                    temp.add(label).center().padLeft(getUnit().type == null || getUnit().type.armor < 10 ? -4f : 0f);
                                    temp.pack();
                                }){
                                    @Override
                                    public void draw() {
                                        if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild) return;
                                        super.draw();
                                    }
                                });
                            }}).growX().left().padLeft(5 * 8f);
                        }));
                    }};

                    Label label = new Label(() ->{
                        String name = "";
                        if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild) name = "[accent]" + ((BlockUnitUnit)getUnit()).tile().block.localizedName + "[]";
                        else if(getUnit() != null && getUnit().type != null) name = "[accent]" + getUnit().type.localizedName + "[]";

                        return name;
                    });

                    TextButton button = Elem.newButton("?", Styles.clearPartialt, () -> {
                        if(getUnit().type != null && getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild) ui.content.show(((BlockUnitUnit)getUnit()).tile().block);
                        else if(getUnit().type != null) ui.content.show(getUnit().type);
                    });

                    tt.top();
                    tt.table(ttt -> { //unit icon/armor
                        ttt.add(stack);
                    }).left();
                    tt.table(ttt -> {  //unit name
                        ttt.defaults().width(12 * 8f);
                        ttt.add(label).padLeft(24f);
                    }).center();
                    tt.table(ttt -> { //unit info
                        ttt.defaults().size(5 * 8f);
                        ttt.add(button).padLeft(-24f);
                    }).right();
                });
                t.row();
                t.table(tt -> {
                    tt.defaults().width(23 * 8f).height(4f * 8f).top();
                    for(Element bar : bars){
                        tt.add(bar).growX().left();
                        tt.row();
                    }
                });
                t.setColor(t.color.cpy().a(1f));
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Color color = this.color;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                }
            }).padRight(24 * 8f);
            table.row();
            Unit unittemp = getUnit();
            table.update(() -> {
                if(!Core.settings.getBool("unitui")) return;
                if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit) getUnit()).tile() instanceof Turret.TurretBuild){
                    Turret.TurretBuild entity = ((Turret.TurretBuild)((BlockUnitUnit) getUnit()).tile());
                    if(entity.charging) heat2 += Time.delta;
                    else heat2 = 0f;
                }
                heat += Time.delta;
                if (heat >= 6 && unittemp != getUnit()) {
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
            table.visibility = () -> Core.settings.getBool("unitui") && (
                    ui.hudfrag.shown && !ui.minimapfrag.shown()
                            && (!Vars.mobile ||
                            !(getUnit().isBuilding() || Vars.control.input.block != null || !Vars.control.input.selectRequests.isEmpty()
                                    && !(Vars.control.input.lastSchematic != null && !Vars.control.input.selectRequests.isEmpty()))));
        }));
    }

    public void addCore(){
        core = new Table(tx -> {
            tx.left();
            tx.add(new Table(tt -> {
                tt.defaults().maxWidth(24/3f * 3f).left().top();

                int row = 0;
                if(Vars.player.unit() == null) return;
                coreamount = Vars.player.unit().team().cores().size;
                for(int r = 0; r < coreamount; r++){
                    CoreBlock.CoreBuild core = Vars.player.unit().team().cores().get(r);
                    TextureRegion region = core.block.icon(Cicon.full);

                    if(coreamount > 1 && r % 4 == 0) {
                        tt.row();
                        row++;
                    }
                    else if(r % 4 == 0){
                        tt.row();
                        row++;
                    }
                    tt.table(coretable -> {
                        coretable.center();
                        coretable.add(new Stack(){{
                            add(new Table(o -> {
                                o.left();
                                o.add(new Image(region)).size(6 * 8f).scaling(Scaling.bounded);
                            }));

                            add(new Table(h -> {
                                h.add(new Stack(){{
                                    add(new Table(e -> {
                                        e.defaults().growX().height(9).width(6f * 8f).padTop(6 * 8f);
                                        Bar healthBar = new Bar(
                                                () -> "",
                                                () -> Pal.health,
                                                core::healthf);
                                        e.add(healthBar);
                                        e.pack();
                                    }));
                                }});
                                h.pack();
                            }));
                        }}).center();
                        coretable.row();
                        coretable.center();
                        coretable.label(() -> "(" + (int)core.x / 8 + ", " + (int)core.y / 8 + ")");
                    }).left();
                    tt.center();
                }
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Color color = this.color;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("coreuiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                }
            }).padLeft(6 * 8f);
            tx.setColor(tx.color.cpy().a(1f));
        });
    }

    public void addCoreTable(){
        ScrollPane pane = new ScrollPane(new Image(Core.atlas.find("clear")), Styles.smallPane);
        pane.setScrollingDisabled(true, false);
        pane.setScrollYForce(scrollPos);
        pane.update(() -> {
            if(pane.hasScroll()){
                Element result = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if(result == null || !result.isDescendantOf(pane)){
                    Core.scene.setScrollFocus(null);
                }
            }

            scrollPos = pane.getScrollY();

            if(coreamount == Vars.player.unit().team().cores().size || !Core.settings.getBool("coreui")) return;
            pane.clearChildren();
            pane.removeChild(core);
            addCore();
            pane.setWidget(core);
        });
        pane.setOverscroll(false, false);

        ui.hudGroup.addChild(new Table(table -> {
            table.top().right();

            table.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.update(() -> {
                    if(coreamount == Vars.player.unit().team().cores().size || !Core.settings.getBool("coreui")) return;
                    t.clearChildren();
                    t.add(pane).maxHeight(Scl.scl(24 * 8f));
                });
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Color color = this.color;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("coreuiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                }
            }).padRight(24 * 8f);


            table.fillParent = true;
            table.visibility = () -> Core.settings.getBool("coreui") && (
                    ui.hudfrag.shown && !ui.minimapfrag.shown()
                            && (!Vars.mobile ||
                            !(Vars.control.input.block != null || !Vars.control.input.selectRequests.isEmpty()
                                    && !(Vars.control.input.lastSchematic != null && !Vars.control.input.selectRequests.isEmpty()))));
        }));
    }

    public void getWave(Table table){
        int winWave = state.isCampaign() && state.rules.winWave > 0 ? state.rules.winWave : Integer.MAX_VALUE;
        maxwave = settings.getInt("wavemax");

        for(int i = state.wave - 1; i <= Math.min(state.wave + maxwave, winWave - 2); i++){
            final int j = i;
            if(state.rules.spawns.find(g -> g.getSpawned(j) > 0) != null) table.table(Tex.underline, t -> {
                t.add(new Table(tt -> {
                    tt.left();
                    tt.add(new Label(() -> "[#" + Pal.accent.toString() + "]" + j + "[]"));
                })).width(32f);

                t.table(tx -> {
                    int row = 0;
                    for(SpawnGroup group : state.rules.spawns){
                        if(group.getSpawned(j) <= 0) continue;
                        row ++;
                        tx.add(new Table(tt -> {
                            tt.right();
                            tt.add(new Stack(){{
                                add(new Table(ttt -> {
                                    ttt.add(new Image(group.type.icon(Cicon.large)));
                                }));

                                add(new Table(ttt -> {
                                    ttt.bottom().left();
                                    ttt.add(new Label(() -> group.getSpawned(j) + ""));
                                    ttt.pack();
                                }));
                            }});

                        })).width(Cicon.large.size + 8f);
                        if(row % 4 == 0) tx.row();
                    /*
                    if(group.effect == StatusEffects.boss && group.getSpawned(i) > 0){
                        int diff = (i + 2) - state.wave;

                        //increments at which to warn about incoming guardian
                        if(diff == 1 || diff == 2 || diff == 5 || diff == 10){
                            showToast(Icon.warning, Core.bundle.format("wave.guardianwarn" + (diff == 1 ? ".one" : ""), diff));
                        }

                        break outer;
                    }
                    */
                    }
                });
            });
            table.row();
        }
    }
    public void addWave(){
        wave = new Table(tx -> {
            tx.left();
            tx.add(new Table(tt -> {
                tt.defaults().left().top().minSize(0f);
                tt.table(this::getWave).left();

                tt.update(() -> {
                    if(maxwave == settings.getInt("wavemax") || !Core.settings.getBool("waveui")) return;
                    tt.clearChildren();
                    getWave(tt);
                });
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Color color = this.color;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("waveuiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                }
            }).padLeft(6 * 8f);
            tx.setColor(tx.color.cpy().a(1f));
        });
    }

    public void addWaveTable(){
        waveTable = new Table(table -> {
            table.name = "wave";
            table.top().left();

            ScrollPane pane = new ScrollPane(new Image(Core.atlas.find("clear")), Styles.smallPane);
            pane.setScrollingDisabled(true, false);
            pane.setScrollYForce(scrollPos);
            pane.update(() -> {
                if(pane.hasScroll()){
                    Element result = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                    if(result == null || !result.isDescendantOf(pane)){
                        Core.scene.setScrollFocus(null);
                    }
                }

                scrollPos = pane.getScrollY();

                if(maxwave == settings.getInt("wavemax") || !Core.settings.getBool("waveui")) return;
                pane.clearChildren();
                addWave();
                pane.setWidget(wave);
            });
            pane.setOverscroll(false, false);

            table.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.update(() -> {
                    if(Vars.state.isMenu() || Vars.state.isEditor()) {
                        waveTable = null;
                        t.clearChildren();
                    }
                    if(maxwave == settings.getInt("wavemax") || !Core.settings.getBool("waveui")) return;
                    t.clearChildren();
                    if(t.getChildren().size < 1) t.add(pane).maxHeight(Scl.scl(24 * 8f));

                    if(t.getChildren().size > 1) {
                        while(t.getChildren().size == 1) t.getChildren().pop();
                    }
                });
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Color color = this.color;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("waveuiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                }
            }).padLeft(56 * 8f);


            table.fillParent = true;
            table.visibility = () ->Core.settings.getBool("waveui") && (
                    ui.hudfrag.shown && !ui.minimapfrag.shown()
                            && (!Vars.mobile ||
                            !(Vars.control.input.block != null || !Vars.control.input.selectRequests.isEmpty()
                                    && !(Vars.control.input.lastSchematic != null && !Vars.control.input.selectRequests.isEmpty()))));
        });
        ui.hudGroup.addChild(waveTable);
    }
}
