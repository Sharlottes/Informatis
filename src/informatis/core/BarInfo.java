package informatis.core;

import arc.graphics.*;
import arc.graphics.g2d.TextureRegion;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.ui.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;

import java.lang.reflect.*;

import static informatis.SUtils.*;
import static arc.Core.*;
import static mindustry.Vars.*;
import static informatis.SVars.*;
import static informatis.ui.SIcons.*;

public class BarInfo {
    public static Seq<BarData> data = new Seq<>();

    public static <T extends Teamc> void getInfo(T target) throws IllegalAccessException, NoSuchFieldException {
        data.clear();

        if(target instanceof Healthc){
            Healthc healthc = (Healthc) target;
            float pro = healthc.health();
            data.add(new BarData(bundle.format("shar-stat.health", formatNumber(pro)), Pal.health, pro, health));
        }

        if(target instanceof Unit unit){
            float max = ((ShieldRegenFieldAbility) content.units().copy().max(ut -> {
                ShieldRegenFieldAbility ability = (ShieldRegenFieldAbility) ut.abilities.find(ab -> ab instanceof ShieldRegenFieldAbility);
                if(ability == null) return 0;
                return ability.max;
            }).abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max;
            //float commands = Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target);

            data.add(new BarData(bundle.format("shar-stat.shield", formatNumber(unit.shield())), Pal.surge, unit.shield() / max, shield));
            data.add(new BarData(bundle.format("shar-stat.capacity", unit.stack.item.localizedName, formatNumber(unit.stack.amount), formatNumber(unit.type.itemCapacity)), unit.stack.amount > 0 && unit.stack().item != null ? unit.stack.item.color.cpy().lerp(Color.white, 0.15f) : Color.white, unit.stack.amount / (unit.type.itemCapacity * 1f), item));
            //data.add(new BarData(bundle.format("shar-stat.commandUnits", formatNumber(commands), formatNumber(unit.type().commandLimit)), Pal.powerBar.cpy().lerp(Pal.surge.cpy().mul(Pal.lighterOrange), Mathf.absin(Time.time, 7f / (1f + Mathf.clamp(commands / (unit.type().commandLimit * 1f))), 1f)), commands / (unit.type().commandLimit * 1f)));
            if(target instanceof Payloadc pay) data.add(new BarData(bundle.format("shar-stat.payloadCapacity", formatNumber(Mathf.round(Mathf.sqrt(pay.payloadUsed()))), formatNumber(Mathf.round(Mathf.sqrt(unit.type().payloadCapacity)))), Pal.items, pay.payloadUsed() / unit.type().payloadCapacity));
            if(state.rules.unitAmmo) data.add(new BarData(bundle.format("shar-stat.ammos", formatNumber(unit.ammo()), formatNumber(unit.type().ammoCapacity)), unit.type().ammoType.color(), unit.ammof()));
        }

        else if(target instanceof Building build){
            if(build.block.hasLiquids) data.add(new BarData(bundle.format("shar-stat.capacity", build.liquids.currentAmount() < 0.01f ? build.liquids.current().localizedName : bundle.get("bar.liquid"), formatNumber(build.liquids.currentAmount()), formatNumber(build.block.liquidCapacity)), build.liquids.current().color, build.liquids.currentAmount() / build.block.liquidCapacity, liquid));

            if(build.block.hasPower && build.block.consumesPower){
                ConsumePower cons = build.block.consPower;
                data.add(new BarData(bundle.format("shar-stat.power", formatNumber(build.power.status * 60f * (cons.buffered ? cons.capacity : cons.usage)), formatNumber(60f * (cons.buffered ? cons.capacity : cons.usage))), Pal.powerBar, Mathf.zero(cons.requestedPower(build)) && build.power.graph.getPowerProduced() + build.power.graph.getBatteryStored() > 0f ? 1f : build.power.status, power));
            }
            if(build.block.hasItems) {
                float value;
                if (target instanceof CoreBlock.CoreBuild cb) value = cb.storageCapacity * content.items().count(UnlockableContent::unlockedNow);
                else if(target instanceof StorageBlock.StorageBuild sb && !sb.canPickup() && sb.linkedCore instanceof CoreBlock.CoreBuild cb) value = cb.storageCapacity * content.items().count(UnlockableContent::unlockedNow);
                else value = build.block.itemCapacity;
                data.add(new BarData(bundle.format("shar-stat.capacity", bundle.get("category.items"), formatNumber(build.items.total()), value), Pal.items, build.items.total() / value, item));
            }
        }

        if(target instanceof ReloadTurret.ReloadTurretBuild || target instanceof MassDriver.MassDriverBuild){
            float pro;
            if(target instanceof ReloadTurret.ReloadTurretBuild turret) pro = turret.reloadCounter / ((Turret)turret.block).reload;
            else {
                MassDriver.MassDriverBuild mass = (MassDriver.MassDriverBuild) target;
                pro = mass.reloadCounter;
            }
            data.add(new BarData(bundle.format("shar-stat.reload", formatNumber(pro * 100f)), Pal.accent.cpy().lerp(Color.orange, pro), pro, reload));
        }

        if(target instanceof ForceProjector.ForceBuild force){
            ForceProjector forceBlock = (ForceProjector) force.block;
            float max = forceBlock.shieldHealth + forceBlock.phaseShieldBoost * force.phaseHeat;
            data.add(new BarData(bundle.format("shar-stat.shield", formatNumber(max-force.buildup), formatNumber(max)), Pal.shield, (max-force.buildup)/max, shield));
        }

        if(target instanceof MendProjector.MendBuild || target instanceof OverdriveProjector.OverdriveBuild || target instanceof ConstructBlock.ConstructBuild || target instanceof Reconstructor.ReconstructorBuild || target instanceof UnitFactory.UnitFactoryBuild || target instanceof Drill.DrillBuild || target instanceof GenericCrafter.GenericCrafterBuild) {
            float pro;
            if(target instanceof MendProjector.MendBuild mend){
                pro = (float) mend.sense(LAccess.progress);
                Tmp.c1.set(Pal.heal);
            }
            else if(target instanceof OverdriveProjector.OverdriveBuild over){
                OverdriveProjector block = (OverdriveProjector)over.block;
                Field ohno = OverdriveProjector.OverdriveBuild.class.getDeclaredField("charge");
                ohno.setAccessible(true);
                pro = (float) ohno.get(over)/((OverdriveProjector)over.block).reload;
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

                if(factory.unit() == null) data.add(new BarData("[lightgray]" + Iconc.cancel, Pal.power, 0f));
                else {
                    float value = factory.team.data().countType(factory.unit());
                    data.add(new BarData(bundle.format("bar.unitcap", Fonts.getUnicodeStr(factory.unit().name), formatNumber(value), formatNumber(Units.getCap(factory.team))), Pal.power, value / Units.getCap(factory.team)));
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
            else {
                GenericCrafter.GenericCrafterBuild crafter = (GenericCrafter.GenericCrafterBuild) target;
                GenericCrafter block = (GenericCrafter) crafter.block;

                pro = (float) crafter.sense(LAccess.progress);
                if(block.outputItem != null) Tmp.c1.set(block.outputItem.item.color);
                else if(block.outputLiquid != null) Tmp.c1.set(block.outputLiquid.liquid.color);
                else Tmp.c1.set(Pal.items);
            }

            data.add(new BarData(bundle.format("shar-stat.progress", formatNumber(pro * 100f)), Tmp.c1, pro));
        }

        if(target instanceof PowerGenerator.GeneratorBuild generator){
            data.add(new BarData(bundle.format("shar-stat.powerIn", formatNumber(generator.getPowerProduction() * generator.timeScale() * 60f)), Pal.powerBar, generator.productionEfficiency, power));
        }

        if(target instanceof PowerNode.PowerNodeBuild || target instanceof PowerTurret.PowerTurretBuild) {
            float value, max;
            if(target instanceof PowerNode.PowerNodeBuild node){
                max = node.power.graph.getLastPowerStored();
                value = node.power.graph.getLastCapacity();

                data.add(new BarData(bundle.format("bar.powerlines", node.power.links.size, ((PowerNode)node.block).maxNodes), Pal.items, (float)node.power.links.size / (float)((PowerNode)node.block).maxNodes));
                data.add(new BarData(bundle.format("shar-stat.powerOut", "-" + formatNumber(node.power.graph.getLastScaledPowerOut() * 60f)), Pal.powerBar, node.power.graph.getLastScaledPowerOut() / node.power.graph.getLastScaledPowerIn(), power));
                data.add(new BarData(bundle.format("shar-stat.powerIn", formatNumber(node.power.graph.getLastScaledPowerIn() * 60f)), Pal.powerBar, node.power.graph.getLastScaledPowerIn() / node.power.graph.getLastScaledPowerOut(), power));
                data.add(new BarData(bundle.format("bar.powerbalance", (node.power.graph.getPowerBalance() >= 0 ? "+" : "") + formatNumber(node.power.graph.getPowerBalance() * 60)), Pal.powerBar, node.power.graph.getLastPowerProduced() / node.power.graph.getLastPowerNeeded(), power));
            }
            else { //TODO: why is this different
                PowerTurret.PowerTurretBuild turret = (PowerTurret.PowerTurretBuild) target;
                max = turret.block.consPower.usage;
                value = turret.power.status * turret.power.graph.getLastScaledPowerIn();
            }

            data.add(new BarData(bundle.format("shar-stat.power", formatNumber(Math.max(value, max) * 60), formatNumber(max * 60)), Pal.power, value / max));
        }

        if(target instanceof ItemTurret.ItemTurretBuild turret) {
            ItemTurret block = (ItemTurret)turret.block;
            data.add(new BarData(bundle.format("shar-stat.capacity", turret.hasAmmo() ? block.ammoTypes.findKey(turret.peekAmmo(), true).localizedName : bundle.get("stat.ammo"), formatNumber(turret.totalAmmo), formatNumber(block.maxAmmo)), turret.hasAmmo() ? block.ammoTypes.findKey(turret.peekAmmo(), true).color : Pal.ammo, turret.totalAmmo / (float)block.maxAmmo, ammo));
        }

        if(target instanceof LiquidTurret.LiquidTurretBuild turret){
            data.add(new BarData(bundle.format("shar-stat.capacity", turret.liquids.currentAmount() < 0.01f ? turret.liquids.current().localizedName : bundle.get("stat.ammo"), formatNumber(turret.liquids.get(turret.liquids.current())), formatNumber(turret.block.liquidCapacity)), turret.liquids.current().color, turret.liquids.get(turret.liquids.current()) / turret.block.liquidCapacity, liquid));
        }

        if(target instanceof AttributeCrafter.AttributeCrafterBuild || target instanceof ThermalGenerator.ThermalGeneratorBuild || (target instanceof SolidPump.SolidPumpBuild crafter && ((SolidPump)crafter.block).attribute != null)) {
            float display, pro;
            if (target instanceof AttributeCrafter.AttributeCrafterBuild crafter) {
                AttributeCrafter block = (AttributeCrafter) crafter.block;
                display = (block.baseEfficiency + Math.min(block.maxBoost, block.boostScale * block.sumAttribute(block.attribute, crafter.tileX(), crafter.tileY()))) * 100f;
                pro = block.boostScale * crafter.attrsum / block.maxBoost;
            }
            else if (target instanceof ThermalGenerator.ThermalGeneratorBuild thermal) {
                ThermalGenerator block = (ThermalGenerator) thermal.block;
                float max = content.blocks().max(b -> b instanceof Floor f && f.attributes != null ? f.attributes.get(block.attribute) : 0).asFloor().attributes.get(block.attribute);
                display = block.sumAttribute(block.attribute, thermal.tileX(), thermal.tileY()) * 100;
                pro = block.sumAttribute(block.attribute, thermal.tileX(), thermal.tileY()) / block.size / block.size / max;
            }
            else {
                SolidPump.SolidPumpBuild crafter = (SolidPump.SolidPumpBuild) target;
                SolidPump block = (SolidPump) crafter.block;
                float fraction = Math.max(crafter.validTiles + crafter.boost + (block.attribute == null ? 0 : block.attribute.env()), 0);
                float max = content.blocks().max(b -> b instanceof Floor f && f.attributes != null ? f.attributes.get(block.attribute) : 0).asFloor().attributes.get(block.attribute);
                display = Math.max(block.sumAttribute(block.attribute, crafter.tileX(), crafter.tileY()) / block.size / block.size + block.baseEfficiency, 0f) * 100 * block.percentSolid(crafter.tileX(), crafter.tileY());
                pro = fraction / max;
            }

            data.add(new BarData(bundle.format("shar-stat.attr", Mathf.round(display)), Pal.ammo, pro));
        }
    }

    public static class BarData {
        public String name;
        public Color color;
        public float number;
        public TextureRegion icon = clear;

        BarData(String name, Color color, float number) {
            this.name = name;
            this.color = color;
            this.number = number;
        }

        BarData(String name, Color color, float number, TextureRegion icon) {
            this(name, color, number);
            this.icon = icon;
        }
    }
}
