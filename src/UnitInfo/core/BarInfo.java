package UnitInfo.core;

import UnitInfo.*;
import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.core.*;
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
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.power.ThermalGenerator;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;

import java.lang.reflect.*;

import static UnitInfo.SUtils.floatFormat;
import static arc.Core.*;
import static mindustry.Vars.*;

public class BarInfo {
    static Seq<String> strings = Seq.with("","","","","","");
    static FloatSeq numbers = FloatSeq.with(0f,0f,0f,0f,0f,0f);
    static Seq<Color> colors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);
    static Field linkedCore; // Versions below 130 don't have this public

    static {
        if(Version.build <= 129) {
            try {
                linkedCore = StorageBlock.StorageBuild.class.getDeclaredField("linkedCore");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            linkedCore.setAccessible(true);
        }
    }

    public static <T extends Teamc> void getInfo(T target) throws IllegalAccessException, NoSuchFieldException {
        for(int i = 0; i < 6; i++) { //init
            strings.set(i, "[lightgray]<Empty>[]");
            colors.set(i, Color.clear);
            numbers.set(i, 0f);
        }
        
        
        if(target instanceof Healthc healthc){
            strings.set(0, bundle.format("shar-stat.health", floatFormat(healthc.health())));
            colors.set(0, Pal.health);
            numbers.set(0, healthc.healthf());
        }


        if(target instanceof Turret.TurretBuild turret){
            strings.set(1, bundle.format("shar-stat.reload", floatFormat((turret.reload / ((Turret)turret.block).reloadTime) * 100f)));
            colors.set(1, Pal.accent.cpy().lerp(Color.orange, Mathf.clamp((turret.reload / ((Turret)turret.block).reloadTime))));
            numbers.set(1, turret.reload / ((Turret)turret.block).reloadTime);
        }
        else if(target instanceof MassDriver.MassDriverBuild mass){
            strings.set(1, bundle.format("shar-stat.reload", floatFormat(mass.reload * 100f)));
            colors.set(1, Pal.accent.cpy().lerp(Color.orange, mass.reload));
            numbers.set(1, mass.reload);
        }
        else if(target instanceof Unit unit && unit.type != null){
            float max1 = ((ShieldRegenFieldAbility)content.units().copy().filter(ut -> ut.abilities.contains(abil -> abil instanceof ShieldRegenFieldAbility)).sort(ut -> ((ShieldRegenFieldAbility)ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max).peek().abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max;
            float max2 = 0f;
            ForceFieldAbility ffa;
            if((ffa = (ForceFieldAbility) unit.type().abilities.find(abil -> abil instanceof ForceFieldAbility)) != null) max2 = ffa.max;
            strings.set(1, bundle.format("shar-stat.shield", floatFormat(unit.shield())));
            colors.set(1, Pal.surge);
            numbers.set(1, unit.shield() / Math.max(max1, max2));
        }
        else if(target instanceof ForceProjector.ForceBuild force){
            ForceProjector forceBlock = (ForceProjector) force.block;
            float max = forceBlock.shieldHealth + forceBlock.phaseShieldBoost * force.phaseHeat;
            strings.set(1, bundle.format("shar-stat.shield", floatFormat(max-force.buildup), floatFormat(max)));
            colors.set(1, Pal.shield);
            numbers.set(1, (max-force.buildup)/max);
        }
        else if(target instanceof ConstructBlock.ConstructBuild build){
            strings.set(1, bundle.format("shar-stat.progress", floatFormat(build.progress * 100)));
            colors.set(1, Pal.darkerMetal);
            numbers.set(1, build.progress);
        }
        else if(target instanceof UnitFactory.UnitFactoryBuild build){
            strings.set(1, bundle.format("shar-stat.progress", floatFormat(build.fraction() * 100f)));
            colors.set(1, Pal.darkerMetal);
            numbers.set(1, build.fraction());
        }
        else if(target instanceof Reconstructor.ReconstructorBuild reconstruct){
            strings.set(1, bundle.format("shar-stat.progress", floatFormat(reconstruct.fraction() * 100)));
            colors.set(1, Pal.darkerMetal);
            numbers.set(1, reconstruct.fraction());
        }
        else if(target instanceof MendProjector.MendBuild mend){
            strings.set(1, bundle.format("shar-stat.progress", floatFormat((float) mend.sense(LAccess.progress) * 100f)));
            colors.set(1, Pal.heal);
            numbers.set(1, (float) mend.sense(LAccess.progress));
        }
        else if(target instanceof OverdriveProjector.OverdriveBuild over){
            Field ohno = OverdriveProjector.OverdriveBuild.class.getDeclaredField("charge");
            ohno.setAccessible(true);
            float charge = (float) ohno.get(over);
            strings.set(1, bundle.format("shar-stat.progress", floatFormat(Mathf.clamp(charge/((OverdriveProjector)over.block).reload) * 100f)));
            colors.set(1, Color.valueOf("feb380"));
            numbers.set(1, Mathf.clamp(charge/((OverdriveProjector)over.block).reload));
        }
        else if(target instanceof Drill.DrillBuild drill){
            strings.set(1, bundle.format("shar-stat.progress", floatFormat((float) drill.sense(LAccess.progress) * 100f)));
            colors.set(1, drill.dominantItem == null ? Pal.items : drill.dominantItem.color);
            numbers.set(1, (float) drill.sense(LAccess.progress));
        }
        else if(target instanceof GenericCrafter.GenericCrafterBuild crafter){
            GenericCrafter block = (GenericCrafter) crafter.block;
            if(block.outputItem != null) Tmp.c1.set(block.outputItem.item.color);
            else if(block.outputLiquid != null) Tmp.c1.set(block.outputLiquid.liquid.color);
            else Tmp.c1.set(Pal.items);
            strings.set(1, bundle.format("shar-stat.progress", floatFormat((float) crafter.sense(LAccess.progress) * 100f)));
            colors.set(1, Tmp.c1);
            numbers.set(1, (float) crafter.sense(LAccess.progress));
        }
        else if(target instanceof PowerNode.PowerNodeBuild node){
            strings.set(1, bundle.format("bar.powerstored", floatFormat(node.power.graph.getLastPowerStored()), floatFormat(node.power.graph.getLastCapacity())));
            colors.set(1, Pal.powerBar);
            numbers.set(1, node.power.graph.getLastPowerStored() / node.power.graph.getLastCapacity());
        }
        else if(target instanceof PowerGenerator.GeneratorBuild generator){
            strings.set(1, bundle.format("bar.poweroutput", floatFormat(generator.getPowerProduction() * generator.timeScale() * 60f)));
            colors.set(1, Pal.powerBar);
            numbers.set(1, generator.productionEfficiency);
        }

        if(target instanceof ItemTurret.ItemTurretBuild turret) {
            ItemTurret block = (ItemTurret)turret.block;
            strings.set(2, bundle.format("shar-stat.itemAmmo", floatFormat(turret.totalAmmo), floatFormat(block.maxAmmo)));
            colors.set(2, turret.hasAmmo() ? block.ammoTypes.findKey(turret.peekAmmo(), true).color : Pal.ammo);
            numbers.set(2, turret.totalAmmo / (float)block.maxAmmo);
        }
        else if(target instanceof LiquidTurret.LiquidTurretBuild turret){
            strings.set(2, bundle.format("shar-stat.liquidAmmo", floatFormat(turret.liquids.get(turret.liquids.current())), floatFormat(turret.block.liquidCapacity)));
            colors.set(2, turret.liquids.current().color);
            numbers.set(2, turret.liquids.get(turret.liquids.current()) / turret.block.liquidCapacity);
        }
        else if(target instanceof PowerTurret.PowerTurretBuild turret){
            float max = turret.block.consumes.getPower().usage;
            float v = turret.power.status * turret.power.graph.getLastScaledPowerIn();
            strings.set(2, bundle.format("shar-stat.power", floatFormat(Math.min(v,max) * 60), floatFormat(max * 60)));
            colors.set(2, Pal.powerBar);
            numbers.set(2, v/max);
        }
        else if(target instanceof Building b && b.block.hasItems) {
            if(target instanceof CoreBlock.CoreBuild cb){
                strings.set(2, bundle.format("shar-stat.itemCapacity", floatFormat(b.items.total()), floatFormat(cb.storageCapacity * content.items().count(UnlockableContent::unlockedNow))));
                numbers.set(2, cb.items.total() / (cb.storageCapacity * content.items().count(UnlockableContent::unlockedNow) * 1f));
            }
            else if(target instanceof StorageBlock.StorageBuild sb && !sb.canPickup() && sb.linkedCore instanceof CoreBlock.CoreBuild cb){
                strings.set(2, bundle.format("shar-stat.itemCapacity", floatFormat(sb.items.total()), floatFormat(cb.storageCapacity * content.items().count(UnlockableContent::unlockedNow))));
                numbers.set(2, sb.items.total() / (cb.storageCapacity * content.items().count(UnlockableContent::unlockedNow) * 1f));
            }
            else {
                strings.set(2, bundle.format("shar-stat.itemCapacity", floatFormat(b.items.total()), floatFormat(b.block.itemCapacity)));
                numbers.set(2, b.items.total() / (float) b.block.itemCapacity);
            }
            colors.set(2, Pal.items);
        }
        else if(target instanceof Unit unit && unit.type != null) {
            strings.set(2, bundle.format("shar-stat.itemCapacity", floatFormat(unit.stack.amount), floatFormat(unit.type.itemCapacity)));
            if(unit.stack.amount > 0 && unit.stack().item != null) colors.set(2, unit.stack.item.color.cpy().lerp(Color.white, 0.15f));
            numbers.set(2, unit.stack.amount / (unit.type.itemCapacity * 1f));
        }
        else if(target instanceof PowerNode.PowerNodeBuild node){
            strings.set(2, bundle.format("bar.powerlines", node.power.links.size, ((PowerNode)node.block).maxNodes));
            colors.set(2, Pal.items);
            numbers.set(2, (float)node.power.links.size / (float)((PowerNode)node.block).maxNodes);
        }



        if(target instanceof Unit unit && unit.type != null) {
            strings.set(3, bundle.format("shar-stat.commandUnits", floatFormat(Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target)), floatFormat(unit.type().commandLimit)));
            colors.set(3, Pal.powerBar.cpy().lerp(Pal.surge.cpy().mul(Pal.lighterOrange), Mathf.absin(Time.time, 7f / (1f + Mathf.clamp(Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target) / (unit.type().commandLimit * 1f))), 1f)));
            numbers.set(3, Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target) / (unit.type().commandLimit * 1f));
        }
        else if(target instanceof UnitFactory.UnitFactoryBuild factory){
            strings.set(3, factory.unit() == null ? "[lightgray]" + Iconc.cancel :
                    bundle.format("bar.unitcap", Fonts.getUnicodeStr(factory.unit().name), floatFormat(factory.team.data().countType(factory.unit())), floatFormat(Units.getCap(factory.team))));
            colors.set(3, Pal.power);
            numbers.set(3, factory.unit() == null ? 0f : (float)factory.team.data().countType(factory.unit()) / Units.getCap(factory.team));
        }
        else if(target instanceof Reconstructor.ReconstructorBuild reconstruct){
            strings.set(3, reconstruct.unit() == null ? "[lightgray]" + Iconc.cancel :
                    bundle.format("bar.unitcap", Fonts.getUnicodeStr(reconstruct.unit().name), floatFormat(reconstruct.team.data().countType(reconstruct.unit())), floatFormat(Units.getCap(reconstruct.team))));
            colors.set(3, Pal.power);
            numbers.set(3, reconstruct.unit() == null ? 0f : (float)reconstruct.team.data().countType(reconstruct.unit()) / Units.getCap(reconstruct.team));

        }
        else if(target instanceof Drill.DrillBuild e){
            strings.set(3, bundle.format("bar.drillspeed", floatFormat(e.lastDrillSpeed * 60 * e.timeScale)));
            colors.set(3, Pal.ammo);
            numbers.set(3, e.warmup);
        }
        else if(target instanceof AttributeCrafter.AttributeCrafterBuild crafter){
            AttributeCrafter block = (AttributeCrafter) crafter.block;
            strings.set(3, bundle.format("shar-stat.attr", (int)((block.baseEfficiency + Math.min(block.maxBoost, block.boostScale * block.sumAttribute(block.attribute, crafter.tileX(), crafter.tileY()))) * 100f)));
            colors.set(3, Pal.ammo);
            numbers.set(3, block.boostScale * crafter.attrsum / block.maxBoost);
        }
        else if(target instanceof SolidPump.SolidPumpBuild crafter){
            SolidPump block = (SolidPump) crafter.block;
            float fraction = Math.max(crafter.validTiles + crafter.boost + (block.attribute == null ? 0 : block.attribute.env()), 0);
            float max = content.blocks().max(b -> b instanceof Floor f && f.attributes != null ? f.attributes.get(block.attribute) : 0).asFloor().attributes.get(block.attribute);
            int h = (int)(Math.max(block.sumAttribute(block.attribute, crafter.tileX(), crafter.tileY()) / block.size / block.size + block.baseEfficiency, 0f) * 100 * block.percentSolid(crafter.tileX(), crafter.tileY()));
            strings.set(3, bundle.format("shar-stat.attr", h));
            colors.set(3, Pal.ammo);
            numbers.set(3, fraction / max);
        }
        else if(target instanceof ThermalGenerator.ThermalGeneratorBuild thermal){
            ThermalGenerator block = (ThermalGenerator) thermal.block;
            float max = content.blocks().max(b -> b instanceof Floor f && f.attributes != null ? f.attributes.get(block.attribute) : 0).asFloor().attributes.get(block.attribute);
            strings.set(3, bundle.format("shar-stat.attr", block.sumAttribute(block.attribute, thermal.tileX(), thermal.tileY()) * 100));
            colors.set(3, Pal.ammo);
            numbers.set(3, block.sumAttribute(block.attribute, thermal.tileX(), thermal.tileY()) / block.size / block.size / max);
        }
        else if(target instanceof PowerNode.PowerNodeBuild node){
            strings.set(3, bundle.format("bar.powerbalance", (node.power.graph.getPowerBalance() >= 0 ? "+" : "") + floatFormat(node.power.graph.getPowerBalance() * 60)));
            colors.set(3, Pal.powerBar);
            numbers.set(3, node.power.graph.getLastPowerProduced() / node.power.graph.getLastPowerNeeded());
        }
        else if(target instanceof OverdriveProjector.OverdriveBuild over){
            OverdriveProjector block = (OverdriveProjector)over.block;
            strings.set(3, bundle.format("bar.boost", (int)(over.realBoost() * 100)));
            colors.set(3, Pal.accent);
            numbers.set(3, over.realBoost() / (block.hasBoost ? block.speedBoost + block.speedBoostPhase : block.speedBoost));
        }


        if(target instanceof Unit unit && target instanceof Payloadc pay && unit.type != null){
            strings.set(4, bundle.format("shar-stat.payloadCapacity", floatFormat(Mathf.round(Mathf.sqrt(pay.payloadUsed()))), floatFormat(Mathf.round(Mathf.sqrt(unit.type().payloadCapacity)))));
            colors.set(4, Pal.items);
            numbers.set(4, pay.payloadUsed() / unit.type().payloadCapacity);
        }
        else if(target instanceof Building build){
            if(target instanceof PowerNode.PowerNodeBuild node){
                strings.set(4, bundle.format("shar-stat.powerOut", floatFormat(node.power.graph.getLastScaledPowerOut() * 60f)));
                colors.set(4, Pal.powerBar);
                numbers.set(4, node.power.graph.getLastScaledPowerOut() / node.power.graph.getLastScaledPowerIn());
            }
            else if(build.block.hasLiquids) {
                strings.set(4, bundle.format("shar-stat.liquidCapacity", floatFormat(build.liquids.currentAmount()), floatFormat(build.block.liquidCapacity)));
                colors.set(4, build.liquids.current().color);
                numbers.set(4, build.liquids.currentAmount() / build.block.liquidCapacity);
            }
        }


        if(target instanceof Unit unit && state.rules.unitAmmo && unit.type != null){
            strings.set(5, bundle.format("shar-stat.ammos", floatFormat(unit.ammo()), floatFormat(unit.type().ammoCapacity)));
            colors.set(5, unit.type().ammoType.color());
            numbers.set(5, unit.ammof());
        }
        else if(target instanceof Building build && build.block.hasPower){
            if(target instanceof PowerNode.PowerNodeBuild node){
                strings.set(5, bundle.format("shar-stat.powerIn", floatFormat(node.power.graph.getLastScaledPowerIn() * 60f)));
                colors.set(5, Pal.powerBar);
                numbers.set(5, node.power.graph.getLastScaledPowerIn() / node.power.graph.getLastScaledPowerOut());
            }
            else if(build.block.consumes.hasPower()){
                ConsumePower cons = build.block.consumes.getPower();
                if(cons.buffered) strings.set(5, bundle.format("shar-stat.powerCapacity", floatFormat(build.power.status * cons.capacity * 60f), floatFormat(cons.capacity * 60f)));
                else strings.set(5, bundle.format("shar-stat.powerUsage", floatFormat(build.power.status * cons.usage * 60f), floatFormat(cons.usage * 60f)));
                colors.set(5, Pal.powerBar);
                numbers.set(5, Mathf.zero(cons.requestedPower(build)) && build.power.graph.getPowerProduced() + build.power.graph.getBatteryStored() > 0f ? 1f : build.power.status);
            }
        }
    }
}
