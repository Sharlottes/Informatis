package UnitInfo.core;

import UnitInfo.ui.SBar;
import arc.Core;
import arc.Events;
import arc.func.Func;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Angles;
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
import mindustry.content.StatusEffects;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.entities.units.WeaponMount;
import mindustry.game.EventType;
import mindustry.game.SpawnGroup;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.input.DesktopInput;
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

import static arc.Core.scene;
import static arc.Core.settings;
import static mindustry.Vars.*;

public class HudUi {
    Seq<Element> bars = new Seq<>();
    Table weapon = new Table();
    Table mainTable = new Table();
    Table baseTable = new Table();
    Table unitTable = new Table();
    Table waveTable = new Table();
    Table coreTable = new Table();

    @Nullable UnitType type;
    @Nullable Unit unit;
    Element image;

    int uiIndex = 0;

    float heat;
    float heat2;
    float coreScrollPos;
    float waveScrollPos;
    int maxwave;
    int coreamount;
    float unitFade;
    float a;

    Unit unit2;

    boolean panFix = false;

    public Unit getUnit(){
        Seq<Unit> units = Groups.unit.intersect(Core.input.mouseWorldX(), Core.input.mouseWorldY(), 4, 4);
        if(units.size <= 0) return player.unit();
        Unit unit = units.peek();
        if(unit == null) return player.unit();
        else return unit;
    }

    public void addTable(){
        mainTable = new Table(table -> {
            table.left();

            Label label = new Label("");
            label.setColor(Pal.stat);
            label.update(() -> {
                a = Mathf.lerpDelta(a, 0f, 0.025f);
                label.color.a = a;
            });
            label.setStyle(Styles.outlineLabel);
            label.getStyle().background = Styles.black8;

            Table labelTable = new Table(t -> t.add(label).scaling(Scaling.fit).left().padRight(40 * 8f));

            table.table(t -> {
                Button[] buttons = {null, null, null};
                buttons[0] = t.button(Icon.units, Styles.clearToggleTransi, () -> {
                    uiIndex = 0;
                    buttons[0].setChecked(true);
                    buttons[1].setChecked(false);
                    buttons[2].setChecked(false);
                    label.setText(Core.bundle.get("hud.unit"));
                    addCoreTable();
                    addWaveTable();
                    addBars();
                    addWeapon();
                    addUnitTable();
                    table.removeChild(baseTable);
                    labelTable.setPosition(buttons[uiIndex].x, buttons[uiIndex].y);
                    baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
                    a = 1f;
                }).size(5*8f).get();
                t.row();
                buttons[1] = t.button(Icon.fileText, Styles.clearToggleTransi, () -> {
                    uiIndex = 1;
                    buttons[0].setChecked(false);
                    buttons[1].setChecked(true);
                    buttons[2].setChecked(false);
                    label.setText(Core.bundle.get("hud.wave"));
                    addCoreTable();
                    addWaveTable();
                    addBars();
                    addWeapon();
                    addUnitTable();
                    table.removeChild(baseTable);
                    labelTable.setPosition(buttons[uiIndex].x, buttons[uiIndex].y);
                    baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
                    a = 1f;
                }).size(5*8f).get();
                t.row();
                buttons[2] = t.button(Icon.commandRally, Styles.clearToggleTransi, () -> {
                    uiIndex = 2;
                    buttons[0].setChecked(false);
                    buttons[1].setChecked(false);
                    buttons[2].setChecked(true);
                    label.setText(Core.bundle.get("hud.core"));
                    addCoreTable();
                    addWaveTable();
                    addBars();
                    addWeapon();
                    addUnitTable();
                    table.removeChild(baseTable);
                    labelTable.setPosition(buttons[uiIndex].x, buttons[uiIndex].y);
                    baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
                    a = 1f;
                }).size(5*8f).get();
            });
            baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();

            table.fillParent = true;
            table.visibility = () -> (
                    ui.hudfrag.shown && !ui.minimapfrag.shown()
                            && (!Vars.mobile ||
                            !(getUnit().isBuilding() || Vars.control.input.block != null || !Vars.control.input.selectRequests.isEmpty()
                                    && !(Vars.control.input.lastSchematic != null && !Vars.control.input.selectRequests.isEmpty()))));
        });
        ui.hudGroup.addChild(mainTable);
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
                t.defaults().width(Scl.scl(23 * 8f * (settings.getInt("uiscaling") / 100f)));
                t.defaults().height(Scl.scl(4f * 8f * (settings.getInt("uiscaling") / 100f)));
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
                    if(!(getUnit() instanceof BlockUnitUnit) || (
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
                                imaget = new Image(((ItemTurret) ((BlockUnitUnit) getUnit()).tile().block).ammoTypes.findKey(((ItemTurret.ItemTurretBuild)((BlockUnitUnit) getUnit()).tile()).peekAmmo(), true).icon(Cicon.small)).setScaling(Scaling.fit);

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
                                imaget = new Image(current.get(entity).icon(Cicon.small)).setScaling(Scaling.fit);
                        }
                        else if(((BlockUnitUnit)getUnit()).tile() instanceof PowerTurret.PowerTurretBuild){
                            PowerTurret.PowerTurretBuild entity = ((PowerTurret.PowerTurretBuild)((BlockUnitUnit)getUnit()).tile());
                            ConsumePower cons = entity.block.consumes.getPower();



                            double max = (Math.round(cons.usage * 10) / 10.0) * 60;
                            double v = (Math.round(((ConditionalConsumePower)entity.block.consumes.get(ConsumeType.power)).requestedPower(entity) * 10) / 10.0);
                            float amount = (float) (((Math.round(entity.power.status * v * 10) / 10.0) * 60) / max);
                            //float amount = Mathf.zero(cons.requestedPower(entity)) && entity.power.graph.getPowerProduced() + entity.power.graph.getBatteryStored() > 0f ? 1f : entity.power.status;

                            imaget = new PrograssedReqImage(Icon.power.getRegion(), () -> amount >= 0.99f, amount);
                            if(amount >= 0.999f) imaget = new Image(Icon.power.getRegion()).setScaling(Scaling.fit);
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
                        setDrawable(getUnit().stack.item == null || getUnit().stack.amount <= 0 ? Core.atlas.find("clear") : getUnit().stack.item.icon(Cicon.small));
                    });
                }

                    @Override
                    public void draw() {
                        if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof ItemTurret.ItemTurretBuild) return;
                        super.draw();
                    }
                }.setScaling(Scaling.fit)).size(Scl.scl(30f * (settings.getInt("uiscaling") / 100f))).scaling(Scaling.fit).padBottom(Scl.scl(4 * 8f * (settings.getInt("uiscaling") / 100f))).padRight(Scl.scl(6 * 8f * (settings.getInt("uiscaling") / 100f)));
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
                t.defaults().width(Scl.scl(23 * 8f * (settings.getInt("uiscaling") / 100f)));
                t.defaults().height(Scl.scl(4f * 8f * (settings.getInt("uiscaling") / 100f)));
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
                }}.setScaling(Scaling.fit)).size(Scl.scl(30f * (settings.getInt("uiscaling") / 100f))).scaling(Scaling.fit).padBottom(Scl.scl(4 * 8f * (settings.getInt("uiscaling") / 100f))).padRight(Scl.scl(6 * 8f * (settings.getInt("uiscaling") / 100f)));
                t.pack();
            }));
        }});
    }

    public void addWeapon(){
        weapon = new Table(tx -> {
            tx.defaults().minSize(Scl.scl(12 * 8f * (settings.getInt("uiscaling") / 100f)));
            tx.left();

            if(settings.getBool("commandedunitui") && Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit()) != 0)
                tx.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t1 -> t1.table(tt -> {
                    tt.defaults().width(Scl.scl(24/3f * 8f * (settings.getInt("uiscaling") / 100f)));
                    tt.defaults().minHeight(Scl.scl(12/3f * 8f * (settings.getInt("uiscaling") / 100f)));
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
                                    o.image(region).size(Scl.scl(30 * (settings.getInt("uiscaling") / 100f))).scaling(Scaling.fit);
                                }));

                                add(new Table(h -> {
                                    h.add(new Stack(){{
                                        add(new Table(e -> {
                                            e.defaults().growX().height(Scl.scl(9 * (settings.getInt("uiscaling") / 100f))).width(Scl.scl(42f * (settings.getInt("uiscaling") / 100f))).padRight(Scl.scl(2*8 * (settings.getInt("uiscaling") / 100f))).padTop(Scl.scl(8*2f * (settings.getInt("uiscaling") / 100f)));
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
                                                t.defaults().growX().height(Scl.scl(9 * (settings.getInt("uiscaling") / 100f))).width(Scl.scl(42f * (settings.getInt("uiscaling") / 100f))).padRight(Scl.scl(2*8 * (settings.getInt("uiscaling") / 100f))).padTop(Scl.scl(8*5f * (settings.getInt("uiscaling") / 100f)));
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
                                                        }}.setScaling(Scaling.fit)).size(Scl.scl(2.5f * 8f * (settings.getInt("uiscaling") / 100f))).scaling(Scaling.fit).padBottom(Scl.scl(4 * 8f * (settings.getInt("uiscaling") / 100f))).padLeft(Scl.scl(2 * 8f * (settings.getInt("uiscaling") / 100f)))
                                                    ));
                                                    Table table = new Table(tt -> {
                                                        Label label = new Label(() -> unit.stack.item == null || unit.stack.amount <= 0 ? "" : unit.stack.amount + "");
                                                        label.setFontScale(Scl.scl() * (settings.getInt("uiscaling") / 100f));
                                                        tt.add(label).padBottom(Scl.scl(1 * 8f * (settings.getInt("uiscaling") / 100f))).padLeft(Scl.scl(2 * 8f * (settings.getInt("uiscaling") / 100f)));
                                                        tt.pack();
                                                    });
                                                    add(table);
                                                }});
                                                t.pack();
                                            }));
                                        }})));
                                    }}).padTop(Scl.scl(2*8 * (settings.getInt("uiscaling") / 100f))).padRight(Scl.scl(2*8 * (settings.getInt("uiscaling") / 100f)));
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
                }).padRight(Scl.scl(24 * 8f * (settings.getInt("uiscaling") / 100f)));
            tx.row();
            if(settings.getBool("weaponui") && type != null && type.weapons.size != 0) tx.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, tt -> {

                tt.defaults().width(Scl.scl(24/3f * 8f * (settings.getInt("uiscaling") / 100f)));
                tt.defaults().minHeight(Scl.scl(12/3f * 8f * (settings.getInt("uiscaling") / 100f)));
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
                                }.setScaling(Scaling.fit)).size(Scl.scl(6 * 8f * (settings.getInt("uiscaling") / 100f))).scaling(Scaling.fit);
                            }));

                            add(new Table(h -> {
                                h.add(new Stack(){{
                                    add(new Table(e -> {
                                        e.defaults().growX().height(Scl.scl(9 * (settings.getInt("uiscaling") / 100f))).width(Scl.scl(31.5f * (settings.getInt("uiscaling") / 100f))).padTop(Scl.scl(9*2f * (settings.getInt("uiscaling") / 100f)));
                                        Bar reloadBar = new Bar(
                                                () -> "",
                                                () -> Pal.accent.cpy().lerp(Color.orange, mount.reload / weapon.reload),
                                                () -> mount.reload / weapon.reload);
                                        e.add(reloadBar);
                                        e.pack();
                                    }));
                                }}).padLeft(Scl.scl(8f * (settings.getInt("uiscaling") / 100f)));
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
            }).padRight(Scl.scl(24 * 8f * (settings.getInt("uiscaling") / 100f)));
            tx.setColor(tx.color.cpy().a(1f));
        });
    }


    float hh;
    public void addUnitTable(){
        if(uiIndex != 0) return;
        unitTable = new Table(table -> {
            table.left();
            addBars();
            table.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.defaults().width(Scl.scl(25 * 8f * (settings.getInt("uiscaling") / 100f))).scaling(Scaling.bounded);

                t.table(Tex.underline2, tt -> {
                    Stack stack = new Stack(){{
                        add(new Table(ttt -> ttt.add(new Image(){{
                            update(() -> {
                                TextureRegion region = Core.atlas.find("clear");
                                if(getUnit() instanceof BlockUnitUnit && getUnit().type != null) region = ((BlockUnitUnit)getUnit()).tile().block.icon(Cicon.large);
                                else if(getUnit() != null && getUnit().type != null) region = getUnit().type.icon(Cicon.large);
                                setDrawable(region);
                            });
                        }}.setScaling(Scaling.fit)).size(Scl.scl(4f * 8f * (settings.getInt("uiscaling") / 100f)))));
                        add(new Table(ttt -> {
                            ttt.top().left();
                            ttt.add(new Stack(){{
                                add(new Table(temp -> {
                                    temp.left();
                                    temp.add(new Image(Icon.defense).setScaling(Scaling.fit)).center();
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
                                    label.setFontScale(Scl.scl() * (settings.getInt("uiscaling") / 100f));
                                    temp.add(label).center().padLeft(getUnit().type == null || getUnit().type.armor < Scl.scl(10 * (settings.getInt("uiscaling") / 100f)) ? Scl.scl(-4f * (settings.getInt("uiscaling") / 100f)) : Scl.scl(0f * (settings.getInt("uiscaling") / 100f)));
                                    temp.pack();
                                }){
                                    @Override
                                    public void draw() {
                                        if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild) return;
                                        super.draw();
                                    }
                                });
                            }}).growX().left().padLeft(Scl.scl(5 * 8f * (settings.getInt("uiscaling") / 100f)));
                        }));
                    }};

                    Label label = new Label(() -> {
                        String name = "";
                        if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild) name = "[accent]" + ((BlockUnitUnit)getUnit()).tile().block.localizedName + "[]";
                        else if(getUnit() != null && getUnit().type != null) name = "[accent]" + getUnit().type.localizedName + "[]";

                        return name;
                    });

                    label.setFontScale(Scl.scl() * (settings.getInt("uiscaling") / 100f));
                    TextButton button = Elem.newButton("?", Styles.clearPartialt, () -> {
                        if(getUnit().type != null && getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild) ui.content.show(((BlockUnitUnit)getUnit()).tile().block);
                        else if(getUnit().type != null) ui.content.show(getUnit().type);
                    });

                    tt.top();
                    tt.table(ttt -> { //unit icon/armor
                        ttt.add(stack);
                    }).left();
                    tt.table(ttt -> {  //unit name
                        ttt.defaults().width(Scl.scl(12 * 8f * (settings.getInt("uiscaling") / 100f)));
                        ttt.add(label).padLeft(Scl.scl(24f * (settings.getInt("uiscaling") / 100f)));
                    }).center();
                    tt.table(ttt -> { //unit info
                        ttt.defaults().size(Scl.scl(5 * 8f * (settings.getInt("uiscaling") / 100f)));
                        ttt.add(button).padLeft(Scl.scl(-24f * (settings.getInt("uiscaling") / 100f)));
                    }).right();
                });
                t.row();
                t.table(tt -> {
                    tt.defaults().width(Scl.scl(23 * 8f * (settings.getInt("uiscaling") / 100f))).height(Scl.scl(4f * 8f * (settings.getInt("uiscaling") / 100f))).top();
                    for(Element bar : bars){
                        bar.setWidth(bar.getWidth() * (settings.getInt("uiscaling") / 100f));
                        bar.setHeight(bar.getHeight() * (settings.getInt("uiscaling") / 100f));
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
            }).padRight(Scl.scl(24 * 8f * (settings.getInt("uiscaling") / 100f)));
            table.row();
            table.update(() -> {
                if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit) getUnit()).tile() instanceof Turret.TurretBuild){
                    Turret.TurretBuild entity = ((Turret.TurretBuild)((BlockUnitUnit) getUnit()).tile());
                    if(entity.charging) heat2 += Time.delta;
                    else heat2 = 0f;
                }
                heat += Time.delta;
                if (heat >= 6) {
                    heat = 0f;
                    type = getUnit().type;
                    unit = getUnit();

                    table.removeChild(weapon);
                    addWeapon();
                    table.row();

                    table.add(weapon);
                }
            });

            table.fillParent = true;
            table.visibility = () -> uiIndex == 0;
        });
    }

    public void setCore(Table table){
        table.add(new Table(t -> {
            if(Vars.player.unit() == null) return;
            coreamount = Vars.player.unit().team().cores().size;
            for(int r = 0; r < coreamount; r++){
                CoreBlock.CoreBuild core = Vars.player.unit().team().cores().get(r);
                TextureRegion region = core.block.icon(Cicon.full);

                if(coreamount > 1 && r % 3 == 0) t.row();
                else if(r % 3 == 0) t.row();

                t.table(tt -> {
                    tt.left();
                    tt.add(new Stack(){{
                        add(new Table(tt -> {
                            tt.add(new Stack(){{
                                add(new Table(s -> {
                                    s.left();
                                    s.add(new Image(region).setScaling(Scaling.fit)).size(Scl.scl(6 * 8f * (settings.getInt("uiscaling") / 100f))).scaling(Scaling.fit);
                                }));

                                add(new Table(s -> {
                                    s.add(new Stack(){{
                                        add(new Table(e -> {
                                            e.defaults().growX().height(Scl.scl(9 * (settings.getInt("uiscaling") / 100f))).width(Scl.scl(6f * 8f * (settings.getInt("uiscaling") / 100f))).padTop(Scl.scl(6 * 8f * (settings.getInt("coreuiscaling") / 100f)));
                                            Bar healthBar = new Bar(
                                                    () -> "",
                                                    () -> Pal.health,
                                                    core::healthf);
                                            e.add(healthBar);
                                            e.pack();
                                        }));
                                    }});
                                    s.pack();
                                }));
                            }});
                            tt.row();
                            Label label = new Label(() -> "(" + (int)core.x / 8 + ", " + (int)core.y / 8 + ")");
                            label.setFontScale(Scl.scl() * (settings.getInt("uiscaling") / 100f));
                            tt.add(label);
                        }));

                        add(new Table(tt -> {
                            tt.center();
                            TextButton button = new TextButton("?", Styles.clearPartialt);
                            button.changed(() -> {
                                if(!settings.getBool("panfix")) {
                                    if(control.input instanceof DesktopInput) ((DesktopInput) control.input).panning = true;
                                    Core.camera.position.set(core.x, core.y);
                                }
                                else panFix = !panFix;
                            });
                            tt.update(() -> {
                                if(!settings.getBool("panfix")) return;
                                button.setChecked(panFix);
                                if(control.input instanceof DesktopInput) ((DesktopInput) control.input).panning = true;
                                Core.camera.position.set(core.x, core.y);
                            });
                            tt.add(button).size(Scl.scl(3 * 8f * (settings.getInt("uiscaling") / 100f))).center().padBottom(2 * 6f);
                            tt.pack();
                        }));
                    }});
                }).left();
            }
        }));
    }
    int coreAmount;
    public void addCoreTable(){
        if(uiIndex != 2) return;
        ScrollPane corePane = new ScrollPane(new Table(tx -> tx.table(this::setCore).left()), Styles.smallPane);
        corePane.setScrollingDisabled(true, false);
        corePane.setScrollYForce(coreScrollPos);
        corePane.update(() -> {
            if(corePane.hasScroll()){
                Element result = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if(result == null || !result.isDescendantOf(corePane)){
                    Core.scene.setScrollFocus(null);
                }
            }

            coreScrollPos = corePane.getScrollY();
            if(Vars.player != null && coreAmount != Vars.player.team().cores().size) {
                    corePane.setWidget(new Table(tx -> tx.table(this::setCore).left()));
                    coreAmount = Vars.player.team().cores().size;
            };
        });
        corePane.setOverscroll(false, false);

        coreTable = new Table(table -> {
            table.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.defaults().minWidth(Scl.scl(25 * 8f * (settings.getInt("uiscaling") / 100f))).scaling(Scaling.fit).left();
                t.add(corePane).maxHeight(Scl.scl(32 * 8f * (settings.getInt("uiscaling") / 100f)));
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                    Draw.reset();
                }
            }).padRight(Scl.scl(39 * 8f * (settings.getInt("uiscaling") / 100f)));

            table.fillParent = true;
            table.visibility = () -> uiIndex == 2;
        });
    }

    public void setWave(Table table){
        int winWave = state.isCampaign() && state.rules.winWave > 0 ? state.rules.winWave : Integer.MAX_VALUE;
        maxwave = settings.getInt("wavemax");
        for(int i = state.wave - 1; i <= Math.min(state.wave + maxwave, winWave - 2); i++){
            final int j = i;
            if(state.rules.spawns.find(g -> g.getSpawned(j) > 0) != null) table.table(Tex.underline, t -> {
                t.add(new Table(tt -> {
                    tt.left();
                    Label label = new Label(() -> "[#" + Pal.accent.toString() + "]" + (j + 1) + "[]");
                    label.setFontScale(Scl.scl() * (settings.getInt("uiscaling") / 100f));
                    tt.add(label);
                })).width(Scl.scl(32f * (settings.getInt("uiscaling") / 100f)));

                t.table(tx -> {
                    int row = 0;
                    for(SpawnGroup group : state.rules.spawns){
                        if(group.getSpawned(j) <= 0) continue;
                        row ++;
                        tx.add(new Table(tt -> {
                            tt.right();
                            tt.add(new Stack(){{
                                add(new Table(ttt -> {
                                    ttt.center();
                                    ttt.add(new Image(group.type.icon(Cicon.large)).setScaling(Scaling.fit));
                                    ttt.pack();
                                }));

                                add(new Table(ttt -> {
                                    ttt.bottom().left();
                                    Label label = new Label(() -> group.getSpawned(j) + "");
                                    label.setFontScale(Scl.scl() * (settings.getInt("uiscaling") / 100f));
                                    ttt.add(label);
                                    ttt.pack();
                                }));

                                add(new Table(ttt -> {
                                    ttt.top().right();
                                    Image image = new Image(Icon.warning.getRegion()).setScaling(Scaling.fit);
                                    image.update(() -> image.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f))));
                                    ttt.add(image).size(Scl.scl(12f * (settings.getInt("uiscaling") / 100f)));
                                    ttt.visible(() -> group.effect == StatusEffects.boss && group.getSpawned(j) > 0);
                                    ttt.pack();
                                }));
                            }});

                        })).width(Scl.scl((Cicon.large.size + 8f) * (settings.getInt("uiscaling") / 100f)));
                        if(row % 4 == 0) tx.row();
                    }
                });
            });
            table.row();
        }
    }

    public void addWaveTable(){
        if(uiIndex != 1) return;
        ScrollPane wavePane = new ScrollPane(new Image(Core.atlas.find("clear")).setScaling(Scaling.fit), Styles.smallPane);
        wavePane.setScrollingDisabled(true, false);
        wavePane.setScrollYForce(waveScrollPos);
        wavePane.update(() -> {
            if(wavePane.hasScroll()){
                Element result = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if(result == null || !result.isDescendantOf(wavePane)){
                    Core.scene.setScrollFocus(null);
                }
            }
            waveScrollPos = wavePane.getScrollY();
            wavePane.setWidget(new Table(tx -> tx.table(this::setWave).left()));
        });

        wavePane.setOverscroll(false, false);
        waveTable = new Table(table -> {
            table.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.defaults().minWidth(Scl.scl(25 * 8f * (settings.getInt("uiscaling") / 100f))).scaling(Scaling.fit).left();
                t.add(wavePane).maxHeight(Scl.scl(32 * 8f * (settings.getInt("uiscaling") / 100f)));
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                    Draw.reset();
                }
            }).padRight(Scl.scl(39 * 8f * (settings.getInt("uiscaling") / 100f)));

            table.fillParent = true;
            table.visibility = () -> uiIndex == 1;
        });
    }
}
