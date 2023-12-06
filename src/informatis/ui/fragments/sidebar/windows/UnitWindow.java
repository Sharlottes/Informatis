package informatis.ui.fragments.sidebar.windows;

import arc.*;
import arc.func.Floatp;
import arc.func.Func;
import arc.func.Prov;
import arc.input.KeyCode;
import arc.math.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import informatis.SVars;
import informatis.core.VDOM;
import informatis.ui.components.SBar;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.units.*;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.payloads.*;
import mindustry.ctype.*;
import mindustry.entities.Units;
import mindustry.logic.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;

import java.util.Objects;

import static arc.Core.*;
import static informatis.SVars.*;
import static informatis.SUtils.*;
import static informatis.ui.components.SIcons.*;
import static mindustry.Vars.*;

public class UnitWindow extends Window {
    private final VDOM vdom = new VDOM();
    private final VDOM.Status<Teamc> target = new VDOM.Status<>();
    private final UnitWindowBody unitWindowBody = new UnitWindowBody();

    public UnitWindow() {
        super(Icon.units, "unit");
        height = 300;
        width = 300;

        vdom.addBuilder(unitWindowBody, target);

        Events.run(EventType.Trigger.update, () -> {
            if(!unitWindowBody.locked) target.setStatus(getTarget());
        });
    }

    @Override
    protected void buildBody(Table table) {
        table.top();
        table.add(unitWindowBody).grow();
    }

    public boolean isLocked() {
        return unitWindowBody.locked;
    }

    public static class BarData  {
        public final Prov<String> name;
        public final Floatp fraction;
        public final Prov<Color> color;
        public final TextureRegion icon;

        public BarData(String name, Color color, float fraction) {
            this(name, color, fraction, clear);
        }

        public BarData(String name, Color color, float fraction, TextureRegion icon) {
            this((x) -> name,  (x) -> color, () -> fraction, icon);
        }

        public BarData(Prov<String> name, Prov<Color> color, Floatp fraction) {
            this(name, color, fraction, clear);
        }

        public BarData(Func<Float, String> name, Func<Float, Color> color, Floatp fraction) {
            this(name, color, fraction, clear);
        }

        public BarData(Func<Float, String> name, Func<Float, Color> color, Floatp fraction, TextureRegion icon) {
            this(() -> name.get(fraction.get()), () -> color.get(fraction.get()), fraction, icon);
        }

        public BarData(Prov<String> name, Prov<Color> color, Floatp fraction, TextureRegion icon) {
            this.name = name;
            this.color = color;
            this.fraction = fraction;
            this.icon = icon;
        }
    }

    static class UnitWindowBody extends Table implements VDOM.IRebuildable {
        public boolean locked;
        private final Seq<BarData> barDataSeq = new Seq<>();

        public UnitWindowBody() {
            super();
            fillParent = true;
            Events.run(EventType.Trigger.update, () -> {
                if((input.keyDown(KeyCode.shiftRight) || input.keyDown(KeyCode.shiftLeft))) {
                    if(input.keyTap(KeyCode.r)) {
                        locked = !locked;
                    }
                }
            });
        }

        @Override
        public void rebuild(Object[] statuses) {
            Teamc target = (Teamc) statuses[0];
            barDataSeq.clear();
            getInfo(target, barDataSeq);

            clearChildren();
            add(buildContent(target)).grow();
        }

        private Table buildContent(Teamc target) {
            return new Table(Styles.black8, table -> {
                table.margin(12);
                table.top().defaults().growX().padBottom(4);

                table.table(title -> {
                        title.left().defaults().expandY();
                        title.add(new ProfileImage(target)).size(iconXLarge).padRight(24);
                        title.table(rtitle -> {
                            rtitle.left().defaults().growX();
                            rtitle.table(rttitle -> {
                                rttitle.left();
                                rttitle.add(getTargetName((target))).labelAlign(Align.left).get().setFontScale(1.25f);
                                rttitle.table(weaponsTable -> {
                                    weaponsTable.right();
                                    if (target instanceof Unit u && u.hasWeapons()) {
                                        for (int r = 0; r < u.mounts.length; r++) {
                                            WeaponMount mount = u.mounts[r];
                                            weaponsTable.add(
                                                new WeaponImage(
                                                    mount,
                                                    Core.atlas.isFound(mount.weapon.region) ? mount.weapon.region : u.type != null ? u.type.uiIcon : clear,
                                                    () -> mount.reload / mount.weapon.reload
                                                )
                                            ).pad(0, 4, 0, 4).size(8 * 2.5f);
                                        }
                                    }
                                });
                                rttitle.table(payStatusTable -> {
                                    payStatusTable.top().right();
                                    if (target instanceof Payloadc payloader) {
                                        payStatusTable.table(t -> {
                                            t.top().right();
                                            for (Payload payload : payloader.payloads()) {
                                                Image image = new Image(payload.icon());
                                                image.clicked(() -> ui.content.show(payload.content()));
                                                image.hovered(() -> image.setColor(Tmp.c1.set(image.color).lerp(Color.lightGray, Mathf.clamp(Time.delta))));
                                                image.exited(() -> image.setColor(Tmp.c1.set(image.color).lerp(Color.white, Mathf.clamp(Time.delta))));
                                                t.add(image).size(iconSmall).tooltip(l -> l.label(() -> payload.content().localizedName).style(Styles.outlineLabel));
                                            }
                                        }).right();
                                        payStatusTable.row();
                                    }
                                    if (target instanceof Statusc st &&  st.statusBits() != null) {
                                        Bits applied = st.statusBits();

                                        payStatusTable.table(t -> {
                                            t.top().right();

                                            for (StatusEffect effect : Vars.content.statusEffects()) {
                                                if (Objects.requireNonNull(applied).get(effect.id) && !effect.isHidden()) {
                                                    Image image = new Image(effect.uiIcon);
                                                    image.clicked(() -> ui.content.show(effect));
                                                    image.hovered(() -> image.setColor(Tmp.c1.set(image.color).lerp(Color.lightGray, Mathf.clamp(Time.delta))));
                                                    image.exited(() -> image.setColor(Tmp.c1.set(image.color).lerp(Color.white, Mathf.clamp(Time.delta))));
                                                    t.add(image).size(iconSmall).tooltip(l -> l.label(() -> effect.localizedName + " [lightgray]" + UI.formatTime(st.getDuration(effect))).style(Styles.outlineLabel));
                                                }
                                            }
                                        }).right();
                                        payStatusTable.row();
                                    }
                                }).growX();
                            });
                            rtitle.row();
                            rtitle.table(rbtitle -> {
                                rbtitle.left().defaults().growY();
                                rbtitle.add(
                                    target instanceof Unitc u
                                        ? u.isPlayer()
                                            ? u.getPlayer().name
                                            : "AI"
                                        : target instanceof ControlBlock cb
                                            ? cb.unit().isPlayer()
                                                ? cb.unit().getPlayer().name
                                                : "AI"
                                        : "AI"
                                ).ellipsis(true);
                                rbtitle.button(Icon.linkSmall, Styles.cleari, () -> moveCamera(target)).tooltip("move camera").pad(0, 8, 0, 8);
                                rbtitle.label(() -> target.tileX() + ", " + target.tileY()).growX().labelAlign(Align.right);
                            }).padTop(8);
                        }).growX();
                    });
                table.row();

                table.image().color((target == null ? player.unit() : target).team().color).height(4f);
                table.row();

                table.table(bars -> {
                    for(BarData barData : barDataSeq) {
                        bars.table(bar -> {
                            SBar sbar = new SBar(barData);
                            BarIconImage barIcon = new BarIconImage(barData);
                            bar.add(sbar).height(4 * 8f).growX();
                            bar.add(barIcon).size(iconMed * 0.75f).padLeft(8f);
                        }).growX();
                        bars.row();
                    }
                });
            });
        }

        public void getInfo(Teamc target, Seq<BarData> data) {
            if(target instanceof Healthc healthc){
                data.add(new BarData(
                        () -> bundle.format("shar-stat.health", formatNumber(healthc.health())),
                        () -> Pal.health,
                        healthc::healthf,
                        health
                ));
            }

            if(target instanceof Unit unit){
                float max = SVars.maxShieldAmongUnits;

                data.add(new BarData(
                        () -> bundle.format("shar-stat.shield", formatNumber(unit.shield())),
                        () -> Pal.surge,
                        () -> unit.shield() / max,
                        shield
                ));

                data.add(new BarData(
                        () -> bundle.format("shar-stat.capacity", unit.stack.item.localizedName, formatNumber(unit.stack.amount), formatNumber(unit.type.itemCapacity)),
                        () -> unit.stack.amount > 0 && unit.stack().item != null ? unit.stack.item.color.cpy().lerp(Color.white, 0.15f) : Color.white,
                        () -> unit.stack.amount / (unit.type.itemCapacity * 1f),
                        item
                ));

                if(target instanceof Payloadc pay) {
                    data.add(new BarData(
                            () -> bundle.format("shar-stat.payloadCapacity", formatNumber(Mathf.round(Mathf.sqrt(pay.payloadUsed()))), formatNumber(Mathf.round(Mathf.sqrt(unit.type().payloadCapacity)))),
                            () -> Pal.items,
                            () -> pay.payloadUsed() / unit.type().payloadCapacity
                    ));
                }

                if(state.rules.unitAmmo) {
                    data.add(new BarData(
                            () -> bundle.format("shar-stat.ammos", formatNumber(unit.ammo()), formatNumber(unit.type().ammoCapacity)),
                            () -> unit.type().ammoType.color(),
                            unit::ammof
                    ));
                }
            }
            else if(target instanceof Building build){
                if(build.block.hasLiquids && build.liquids != null) {
                    data.add(new BarData(
                            () -> bundle.format("shar-stat.capacity", build.liquids.currentAmount() < 0.01f ? build.liquids.current().localizedName : bundle.get("bar.liquid"), formatNumber(build.liquids.currentAmount()), formatNumber(build.block.liquidCapacity)),
                            () -> build.liquids.current().color,
                            () -> build.liquids.currentAmount() / build.block.liquidCapacity, liquid
                    ));
                }

                if(build.block.hasPower && build.block.consPower != null && build.power != null){
                    ConsumePower cons = build.block.consPower;
                    data.add(new BarData(
                            () -> bundle.format("shar-stat.power", formatNumber(build.power.status * 60f * (cons.buffered ? cons.capacity : cons.usage)), formatNumber(60f * (cons.buffered ? cons.capacity : cons.usage))),
                            () -> Pal.powerBar,
                            () -> Mathf.zero(cons.requestedPower(build)) && build.power.graph.getPowerProduced() + build.power.graph.getBatteryStored() > 0f ? 1f : build.power.status,
                            power
                    ));
                }
                if(build.block.hasItems && build.items != null) {
                    float value;
                    if (target instanceof CoreBlock.CoreBuild cb) value = cb.storageCapacity * Vars.content.items().count(UnlockableContent::unlockedNow);
                    else if(target instanceof StorageBlock.StorageBuild sb && !sb.canPickup() && sb.linkedCore instanceof CoreBlock.CoreBuild cb) value = cb.storageCapacity * Vars.content.items().count(UnlockableContent::unlockedNow);
                    else value = build.block.itemCapacity;

                    data.add(new BarData(
                            () -> bundle.format("shar-stat.capacity", bundle.get("category.items"), formatNumber(build.items.total()), value),
                            () -> Pal.items,
                            () -> build.items.total() / value,
                            item
                    ));
                }
            }

            if(target instanceof ReloadTurret.ReloadTurretBuild || target instanceof MassDriver.MassDriverBuild) {
                float pro;
                if(target instanceof ReloadTurret.ReloadTurretBuild turret) pro = turret.reloadCounter / ((ReloadTurret)turret.block).reload;
                else pro = ((MassDriver.MassDriverBuild) target).reloadCounter;
                data.add(new BarData(
                        () -> bundle.format("shar-stat.reload", formatNumber(pro * 100f)),
                        () -> Pal.accent.cpy().lerp(Color.orange, pro),
                        () -> pro,
                        reload
                ));
            }

            if(target instanceof ForceProjector.ForceBuild force){
                ForceProjector forceBlock = (ForceProjector) force.block;
                float max = forceBlock.shieldHealth + forceBlock.phaseShieldBoost * force.phaseHeat;
                data.add(new BarData(
                        () -> bundle.format("shar-stat.shield", formatNumber(max-force.buildup), formatNumber(max)),
                        () -> Pal.shield,
                        () -> (max - force.buildup) / max,
                        shield
                ));
            }

            if(target instanceof MendProjector.MendBuild
                    || target instanceof OverdriveProjector.OverdriveBuild
                    || target instanceof ConstructBlock.ConstructBuild
                    || target instanceof Reconstructor.ReconstructorBuild
                    || target instanceof UnitFactory.UnitFactoryBuild
                    || target instanceof Drill.DrillBuild
                    || target instanceof GenericCrafter.GenericCrafterBuild
            ) {
                if(target instanceof MendProjector.MendBuild mend){
                    Tmp.c1.set(Pal.heal);
                }
                else if(target instanceof OverdriveProjector.OverdriveBuild over){
                    OverdriveProjector block = (OverdriveProjector)over.block;
                    Tmp.c1.set(Color.valueOf("feb380"));

                    data.add(new BarData(
                            () -> bundle.format("bar.boost", (int)(over.realBoost() * 100)),
                            () -> Pal.accent,
                            () -> over.realBoost() / (block.hasBoost
                                    ? block.speedBoost + block.speedBoostPhase
                                    : block.speedBoost
                            )
                    ));
                }
                else if(target instanceof ConstructBlock.ConstructBuild construct){
                    Tmp.c1.set(Pal.darkerMetal);
                }
                else if(target instanceof Reconstructor.ReconstructorBuild reconstruct){
                    Tmp.c1.set(Pal.darkerMetal);

                    if(reconstruct.unit() == null) data.add(new BarData("[lightgray]" + Iconc.cancel, Pal.power, 0f));
                    else {
                        float value = reconstruct.team.data().countType(reconstruct.unit());
                        data.add(new BarData(
                                () -> bundle.format("bar.unitcap", Fonts.getUnicodeStr(reconstruct.unit().name), formatNumber(value), formatNumber(Units.getCap(reconstruct.team))),
                                () -> Pal.power,
                                () -> value / Units.getCap(reconstruct.team)
                        ));
                    }

                }
                else if(target instanceof UnitFactory.UnitFactoryBuild factory){
                    Tmp.c1.set(Pal.darkerMetal);

                    UnitType unitType = factory.unit();
                    if( unitType == null) {
                        data.add(new BarData("[lightgray]" + Iconc.cancel, Pal.power, 0f));
                    } else {
                        float value = factory.team.data().countType(factory.unit());
                        data.add(new BarData(
                                () -> bundle.format("bar.unitcap", Fonts.getUnicodeStr(unitType.name), formatNumber(value), formatNumber(Units.getCap(factory.team))),
                                () -> Pal.power,
                                () -> value / Units.getCap(factory.team)
                        ));
                    }
                }
                else if(target instanceof Drill.DrillBuild drill){
                    Tmp.c1.set(drill.dominantItem == null ? Pal.items : drill.dominantItem.color);

                    data.add(new BarData(
                            () -> bundle.format("bar.drillspeed", formatNumber(drill.lastDrillSpeed * 60 * drill.timeScale())),
                            () -> Pal.ammo,
                            () -> drill.warmup
                    ));
                }
                else if(target instanceof GenericCrafter.GenericCrafterBuild crafter) {
                    GenericCrafter block = (GenericCrafter) crafter.block;

                    if(block.outputItem != null) Tmp.c1.set(block.outputItem.item.color);
                    else if(block.outputLiquid != null) Tmp.c1.set(block.outputLiquid.liquid.color);
                    else Tmp.c1.set(Pal.items);
                }

                data.add(new BarData(
                        pro -> bundle.format("shar-stat.progress", formatNumber(pro * 100f)),
                        pro -> Tmp.c1,
                        () -> {
                            float pro = 0;
                            if(target instanceof MendProjector.MendBuild mend){
                                pro = (float) mend.sense(LAccess.progress);
                                Tmp.c1.set(Pal.heal);
                            }
                            else if(target instanceof OverdriveProjector.OverdriveBuild over){
                                OverdriveProjector block = (OverdriveProjector)over.block;
                                pro = (float) Reflect.get(over, "charge")/((OverdriveProjector)over.block).reload;
                                Tmp.c1.set(Color.valueOf("feb380"));

                                data.add(new BarData(bundle.format("bar.boost", (int)(over.realBoost() * 100)), Pal.accent, over.realBoost() / (block.hasBoost ? block.speedBoost + block.speedBoostPhase : block.speedBoost)));
                            }
                            else if(target instanceof ConstructBlock.ConstructBuild construct){
                                pro = construct.progress;
                                Tmp.c1.set(Pal.darkerMetal);
                            }
                            else if(target instanceof UnitFactory.UnitFactoryBuild factory){
                                pro = factory.fraction();
                                Tmp.c1.set(Pal.darkerMetal);

                                UnitType unitType = factory.unit();
                                if(unitType == null) data.add(new BarData("[lightgray]" + Iconc.cancel, Pal.power, 0f));
                                else {
                                    float value = factory.team.data().countType(factory.unit());
                                    data.add(new BarData(bundle.format("bar.unitcap", Fonts.getUnicodeStr(unitType.name), formatNumber(value), formatNumber(Units.getCap(factory.team))), Pal.power, value / Units.getCap(factory.team)));
                                }
                            }
                            else if(target instanceof Reconstructor.ReconstructorBuild reconstruct){
                                pro = reconstruct.fraction();
                                Tmp.c1.set(Pal.darkerMetal);

                                if(reconstruct.unit() == null) data.add(new BarData("[lightgray]" + Iconc.cancel, Pal.power, 0f));
                                else {
                                    float value = reconstruct.team.data().countType(reconstruct.unit());
                                    data.add(new BarData(bundle.format("bar.unitcap", Fonts.getUnicodeStr(reconstruct.unit().name), formatNumber(value), formatNumber(Units.getCap(reconstruct.team))), Pal.power, value / Units.getCap(reconstruct.team)));
                                }

                            }
                            else if(target instanceof Drill.DrillBuild drill){
                                pro = (float) drill.sense(LAccess.progress);
                                Tmp.c1.set(drill.dominantItem == null ? Pal.items : drill.dominantItem.color);

                                data.add(new BarData(bundle.format("bar.drillspeed", formatNumber(drill.lastDrillSpeed * 60 * drill.timeScale())), Pal.ammo, drill.warmup));
                            }
                            else if(target instanceof GenericCrafter.GenericCrafterBuild crafter) {
                                GenericCrafter block = (GenericCrafter) crafter.block;
                                pro = (float) crafter.sense(LAccess.progress);
                                if(block.outputItem != null) Tmp.c1.set(block.outputItem.item.color);
                                else if(block.outputLiquid != null) Tmp.c1.set(block.outputLiquid.liquid.color);
                                else Tmp.c1.set(Pal.items);
                            }
                            return pro;
                        }
                ));
            }

            if(target instanceof PowerGenerator.GeneratorBuild generator){
                data.add(new BarData(
                        () -> bundle.format("shar-stat.powerIn", formatNumber(generator.getPowerProduction() * generator.timeScale() * 60f)),
                        () -> Pal.powerBar,
                        () -> generator.productionEfficiency,
                        power
                ));
            }

            if(target instanceof PowerNode.PowerNodeBuild || target instanceof PowerTurret.PowerTurretBuild) {
                Floatp value, max;
                if(target instanceof PowerNode.PowerNodeBuild node) {
                    if(node.power == null) return;
                    max = () -> node.power.graph.getLastPowerStored();
                    value = () -> node.power.graph.getLastCapacity();

                    data.add(new BarData(
                            () -> bundle.format("bar.powerlines", node.power.links.size, ((PowerNode)node.block).maxNodes),
                            () -> Pal.items,
                            () -> (float)node.power.links.size / (float)((PowerNode)node.block).maxNodes
                    ));
                    data.add(new BarData(
                            () -> bundle.format("shar-stat.powerOut", "-" + formatNumber(node.power.graph.getLastScaledPowerOut() * 60f)),
                            () -> Pal.powerBar,
                            () -> node.power.graph.getLastScaledPowerOut() / node.power.graph.getLastScaledPowerIn(),
                            power
                    ));
                    data.add(new BarData(
                            () -> bundle.format("shar-stat.powerIn", formatNumber(node.power.graph.getLastScaledPowerIn() * 60f)),
                            () -> Pal.powerBar,
                            () -> node.power.graph.getLastScaledPowerIn() / node.power.graph.getLastScaledPowerOut(),
                            power
                    ));
                    data.add(new BarData(
                            () -> bundle.format("bar.powerbalance", (node.power.graph.getPowerBalance() >= 0 ? "+" : "") + formatNumber(node.power.graph.getPowerBalance() * 60)),
                            () -> Pal.powerBar,
                            () -> node.power.graph.getLastPowerProduced() / node.power.graph.getLastPowerNeeded(),
                            power
                    ));
                } else {
                    PowerTurret.PowerTurretBuild powerTurretBuild = (PowerTurret.PowerTurretBuild) target;

                    if(powerTurretBuild.block.consPower == null || powerTurretBuild.power == null) return;

                    max = () -> powerTurretBuild.block.consPower.usage;
                    value = () -> powerTurretBuild.power.status * powerTurretBuild.power.graph.getLastScaledPowerIn();
                }

                data.add(new BarData(
                    () -> bundle.format("shar-stat.power", formatNumber(Math.max(value.get(), max.get()) * 60), formatNumber(max.get() * 60)),
                    () -> Pal.power,
                    () -> value.get() / max.get()
                ));
            }

            if(target instanceof ItemTurret.ItemTurretBuild turret) {
                ItemTurret block = (ItemTurret)turret.block;
                data.add(new BarData(
                        () -> bundle.format("shar-stat.capacity", turret.hasAmmo() ? block.ammoTypes.findKey(turret.peekAmmo(), true).localizedName : bundle.get("stat.ammo"), formatNumber(turret.totalAmmo), formatNumber(block.maxAmmo)),
                        () -> turret.hasAmmo() ? block.ammoTypes.findKey(turret.peekAmmo(), true).color : Pal.ammo,
                        () -> turret.totalAmmo / (float)block.maxAmmo,
                        ammo
                ));
            }

            if(target instanceof LiquidTurret.LiquidTurretBuild turret && turret.liquids != null){
                data.add(new BarData(
                        () -> bundle.format("shar-stat.capacity", turret.liquids.currentAmount() < 0.01f ? turret.liquids.current().localizedName : bundle.get("stat.ammo"), formatNumber(turret.liquids.get(turret.liquids.current())), formatNumber(turret.block.liquidCapacity)),
                        () -> turret.liquids.current().color,
                        () -> turret.liquids.get(turret.liquids.current()) / turret.block.liquidCapacity,
                        liquid
                ));
            }

            if(target instanceof AttributeCrafter.AttributeCrafterBuild || target instanceof ThermalGenerator.ThermalGeneratorBuild || (target instanceof SolidPump.SolidPumpBuild crafter && ((SolidPump)crafter.block).attribute != null)) {
                Floatp display, pro;
                if (target instanceof AttributeCrafter.AttributeCrafterBuild crafter) {
                    AttributeCrafter block = (AttributeCrafter) crafter.block;
                    display = () -> (block.baseEfficiency + Math.min(block.maxBoost, block.boostScale * block.sumAttribute(block.attribute, crafter.tileX(), crafter.tileY()))) * 100f;
                    pro = () -> block.boostScale * crafter.attrsum / block.maxBoost;
                }
                else if (target instanceof ThermalGenerator.ThermalGeneratorBuild thermal) {
                    ThermalGenerator block = (ThermalGenerator) thermal.block;
                    float max = Vars.content.blocks().max(b -> b instanceof Floor f && f.attributes != null ? f.attributes.get(block.attribute) : 0).asFloor().attributes.get(block.attribute);
                    display = () -> block.sumAttribute(block.attribute, thermal.tileX(), thermal.tileY()) * 100;
                    pro = () -> block.sumAttribute(block.attribute, thermal.tileX(), thermal.tileY()) / block.size / block.size / max;
                }
                else {
                    SolidPump.SolidPumpBuild crafter = (SolidPump.SolidPumpBuild) target;
                    SolidPump block = (SolidPump) crafter.block;
                    float fraction = Math.max(crafter.validTiles + crafter.boost + (block.attribute == null ? 0 : block.attribute.env()), 0);
                    float max = Vars.content.blocks().max(b -> b instanceof Floor f && f.attributes != null ? f.attributes.get(block.attribute) : 0).asFloor().attributes.get(block.attribute);
                    display = () -> Math.max(block.sumAttribute(block.attribute, crafter.tileX(), crafter.tileY()) / block.size / block.size + block.baseEfficiency, 0f) * 100 * block.percentSolid(crafter.tileX(), crafter.tileY());
                    pro = () -> fraction / max;
                }

                data.add(new BarData(
                        () -> bundle.format("shar-stat.attr", Mathf.round(display.get())),
                        () -> Pal.ammo,
                        pro
                ));
            }

            if(target instanceof UnitAssembler.UnitAssemblerBuild assemblerBuild) {
                UnitAssembler.AssemblerUnitPlan plan = assemblerBuild.plan();
                UnitType unit = assemblerBuild.unit();

                if(unit == null) data.add(new BarData("[lightgray]" + Iconc.cancel, Pal.power, 0f));
                else data.add(new BarData(
                        () -> bundle.format("shar-stat.progress", Math.round(assemblerBuild.progress * 100 * 100) / 100),
                        () -> Pal.power,
                        () -> assemblerBuild.progress
                ));

                for(PayloadStack stack : plan.requirements) {
                    data.add(new BarData(
                            (pro) -> stack.item.localizedName + ": " + assemblerBuild.blocks.get(stack.item) + " / " + stack.amount,
                            (pro) -> Pal.accent.cpy().lerp(Color.orange, pro),
                            () -> assemblerBuild.blocks.get(stack.item) / (stack.amount * 1f),
                            stack.item.fullIcon
                    ));
                }
            }
        }

        private String getTargetName(Teamc target) {
            String targetName = "";

            if (target instanceof Unit u && u.type != null) {
                targetName = u.type.localizedName;
            }
            else if (target instanceof Building b) {
                if (b instanceof ConstructBlock.ConstructBuild cb) {
                    targetName = cb.current.localizedName;
                } else if (b.block != null) {
                    targetName = b.block.localizedName;
                }
            }

            return targetName;
        }

        private class ProfileImage extends Image {
            public ProfileImage(Teamc target) {
                super(target instanceof Unit u && u.type != null
                        ? u.type.uiIcon
                        : target instanceof Building b
                            ? b instanceof ConstructBlock.ConstructBuild cb
                                ? cb.current.uiIcon
                                : b.block != null
                                    ? b.block.uiIcon
                                    : clear
                            : clear
                );

                clicked(() -> locked = !locked);
            }

            @Override
            public void draw() {
                super.draw();
                int size = 8;

                Draw.color(locked ? Pal.accent : Pal.gray);
                Draw.alpha(parentAlpha);
                Lines.stroke(Scl.scl(3f));
                Lines.rect(x - size / 2f, y - size / 2f, width + size, height + size);
                Draw.reset();
            }
        }
        private static class WeaponImage extends Image {
            private final WeaponMount mount;
            private final Floatp fraction;

            public WeaponImage(WeaponMount mount, TextureRegion region, Floatp fraction) {
                super(region);
                this.mount = mount;
                this.fraction = fraction;

                setSize(iconMed);
                setScaling(Scaling.fit);
            }

            @Override
            public void draw() {
                validate();
                float x = this.x + imageX;
                float y = this.y + imageY - (mount.reload) / mount.weapon.reload * mount.weapon.recoil;
                float width = imageWidth * this.scaleX;
                float height = imageHeight * this.scaleY;
                Draw.color(Color.white);
                Draw.alpha(parentAlpha * color.a);

                getDrawable().draw(x, y, width, height);
                if(ScissorStack.push(Tmp.r1.set(ScissorStack.peek().x + x, ScissorStack.peek().y + y, width, height * fraction.get()))) {
                    Draw.color(Color.gray);
                    getDrawable().draw(x, y, width, height);
                    ScissorStack.pop();
                }
            }
        }
        private static class BarIconImage extends Image {
            public final BarData barData;

            public BarIconImage(BarData barData) {
                super(barData.icon);
                this.barData = barData;
            }

            @Override
            public void draw() {
                validate();
                float x = this.x + imageX;
                float y = this.y + imageY;
                float width = imageWidth * this.scaleX;
                float height = imageHeight * this.scaleY;
                Draw.color(Color.white);
                Draw.alpha(parentAlpha * color.a);

                getDrawable().draw(x, y, width, height);
                if(ScissorStack.push(Tmp.r1.set(ScissorStack.peek().x + x,  ScissorStack.peek().y + y, width, height * barData.fraction.get()))) {
                    Draw.color(barData.color.get());
                    getDrawable().draw(x, y, width, height);
                    ScissorStack.pop();
                }
            }
        }
    }
}

