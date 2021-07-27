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
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;


import static arc.Core.bundle;
import static mindustry.Vars.content;
import static mindustry.Vars.state;

public class BarInfo {
    static Seq<String> strings = Seq.with("","","","","","");
    static FloatSeq numbers = FloatSeq.with(0f,0f,0f,0f,0f,0f);
    static Seq<Color> colors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);

    public static String format(float number){
        if(number >= 10000) return UI.formatAmount((long)number);
        if(String.valueOf(number).split("[.]")[1].matches("0")) return String.valueOf(number).split("[.]")[0];
        return Strings.fixed(number, 1);
    }

    public static <T extends Teamc> void getInfo(T target){
        for(int i = 0; i < 6; i++) { //init
            strings.set(i, "[lightgray]<Empty>[]");
            colors.set(i, Color.clear);
            numbers.set(i, 0f);
        }
        
        
        if(target instanceof Healthc healthc){
            strings.set(0, Core.bundle.format("shar-stat.health", Strings.fixed(healthc.health(), 1)));
            colors.set(0, Pal.health);
            numbers.set(0, healthc.healthf());
        }

        
        if(target instanceof Turret.TurretBuild turret){
            strings.set(1, Core.bundle.format("shar-stat.reload", Strings.fixed((turret.reload / ((Turret)turret.block).reloadTime) * 100f, 1)));
            colors.set(1, Pal.accent.cpy().lerp(Color.orange, Mathf.clamp((turret.reload / ((Turret)turret.block).reloadTime))));
            numbers.set(1, turret.reload / ((Turret)turret.block).reloadTime);
        }
        else if(target instanceof Unit unit && unit.type != null){
            float max1 = ((ShieldRegenFieldAbility)content.units().copy().filter(ut -> ut.abilities.contains(abil -> abil instanceof ShieldRegenFieldAbility)).sort(ut -> ((ShieldRegenFieldAbility)ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max).peek().abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max;
            float max2 = 0f;
            ForceFieldAbility ffa;
            if((ffa = (ForceFieldAbility) unit.type().abilities.find(abil -> abil instanceof ForceFieldAbility)) != null) max2 = ffa.max;
            strings.set(1, Core.bundle.format("shar-stat.shield", format(unit.shield())));
            colors.set(1, Pal.surge);
            numbers.set(1, unit.shield() / Math.max(max1, max2));
        }
        else if(target instanceof ConstructBlock.ConstructBuild build){
            strings.set(1, Core.bundle.format("shar-stat.progress", Strings.fixed(build.progress * 100, 1)));
            colors.set(1, Pal.darkerMetal);
            numbers.set(1, build.progress);
        }
        else if(target instanceof UnitFactory.UnitFactoryBuild build){
            strings.set(1, Core.bundle.format("shar-stat.progress", Strings.fixed(build.fraction() * 100f, 1)));
            colors.set(1, Pal.darkerMetal);
            numbers.set(1, build.fraction());
        }
        else if(target instanceof Reconstructor.ReconstructorBuild reconstruct){
            strings.set(1, Core.bundle.format("shar-stat.progress", Strings.fixed(reconstruct.fraction() * 100, 1)));
            colors.set(1, Pal.darkerMetal);
            numbers.set(1, reconstruct.fraction());
        }
        else if(target instanceof ForceProjector.ForceBuild force){
            ForceProjector forceBlock = (ForceProjector) force.block;
            float max = forceBlock.shieldHealth + forceBlock.phaseShieldBoost * force.phaseHeat;
            strings.set(1, Core.bundle.format("shar-stat.shield", format(max-force.buildup), format(max)));
            colors.set(1, Pal.shield);
            numbers.set(1, (max-force.buildup)/max);
        }
        else if(target instanceof MendProjector.MendBuild mend){
            strings.set(1, Core.bundle.format("shar-stat.progress", Strings.fixed((float) mend.sense(LAccess.progress), 1)));
            colors.set(1, Pal.heal);
            numbers.set(1, (float) mend.sense(LAccess.progress));
        }
        else if(target instanceof OverdriveProjector.OverdriveBuild over){
            strings.set(1, Core.bundle.format("shar-stat.progress", Strings.fixed((float) over.sense(LAccess.progress), 1)));
            colors.set(1, Pal.heal);
            numbers.set(1, (float) over.sense(LAccess.progress));
        }
        
        if(target instanceof ItemTurret.ItemTurretBuild turret) {
            strings.set(2, bundle.format("shar-stat.itemAmmo", format(turret.totalAmmo), format(((ItemTurret)turret.block).maxAmmo)));
            colors.set(2, turret.hasAmmo() ? ((ItemTurret)turret.block).ammoTypes.findKey(turret.peekAmmo(), true).color : Pal.ammo);
            numbers.set(2, turret.totalAmmo / (((ItemTurret)turret.block).maxAmmo * 1f));
        }
        else if(target instanceof LiquidTurret.LiquidTurretBuild turret){
            strings.set(2, bundle.format("shar-stat.liquidAmmo", format(turret.liquids.get(turret.liquids.current())), format(turret.block.liquidCapacity)));
            colors.set(2, turret.liquids.current().color);
            numbers.set(2, turret.liquids.get(turret.liquids.current()) / turret.block.liquidCapacity);
        }
        else if(target instanceof PowerTurret.PowerTurretBuild turret){
            float max = turret.block.consumes.getPower().usage;
            float v = turret.power.status * turret.power.graph.getLastScaledPowerIn();
            strings.set(2, bundle.format("shar-stat.power", format(Math.min(v,max) * 60), format(max * 60)));
            colors.set(2, Pal.powerBar);
            numbers.set(2, v/max);
        }
        else if(target instanceof Building b && b.block.hasItems) {
            if(target instanceof CoreBlock.CoreBuild cb){
                strings.set(2, bundle.format("shar-stat.itemCapacity", format(((Building) target).items.total()), format(cb.storageCapacity * content.items().count(UnlockableContent::unlockedNow))));
                numbers.set(2, cb.items.total() / (cb.storageCapacity * content.items().count(UnlockableContent::unlockedNow) * 1f));
            }
            else if(target instanceof StorageBlock.StorageBuild sb && !sb.canPickup()){
                for(int i = 0; i < 4; i++) {
                    Building build = i == 0 ? sb.front() : i == 1 ? sb.back() : i == 2 ? sb.left() : sb.right();
                    if(build instanceof CoreBlock.CoreBuild cb){
                        strings.set(2, bundle.format("shar-stat.itemCapacity", format(sb.items.total()), format(cb.storageCapacity * content.items().count(UnlockableContent::unlockedNow))));
                        numbers.set(2, sb.items.total() / (cb.storageCapacity * content.items().count(UnlockableContent::unlockedNow) * 1f));
                        break;
                    }
                }
            }
            else {
                strings.set(2, bundle.format("shar-stat.itemCapacity", format(b.items.total()), format(b.block.itemCapacity)));
                numbers.set(2, b.items.total() / (((Building)target).block.itemCapacity * 1f));
            }
            colors.set(2, Pal.items);
        }
        else if(target instanceof Unit unit && unit.type != null) {
            strings.set(2, bundle.format("shar-stat.itemCapacity", format(unit.stack.amount), format(unit.type.itemCapacity)));
            if(unit.stack.amount > 0 && unit.stack().item != null) colors.set(2, unit.stack.item.color.cpy().lerp(Color.white, 0.15f));
            numbers.set(2, unit.stack.amount / (unit.type.itemCapacity * 1f));
        }


        if(target instanceof Turret.TurretBuild t){
            Turret turret = (Turret)t.block;
            if(turret.chargeTime > 0f) {
                strings.set(3, Core.bundle.format("shar-stat.charge", format((SVars.hud.charge / turret.chargeTime) * 100)));
                colors.set(3, Pal.surge.cpy().lerp(Pal.accent, SVars.hud.charge / turret.chargeTime));
                numbers.set(3, SVars.hud.charge / turret.chargeTime);
            }
        }
        else if(target instanceof Unit unit && unit.type != null) {
            strings.set(3, Core.bundle.format("shar-stat.commandUnits", format(Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target)), format(unit.type().commandLimit)));
            colors.set(3, Pal.powerBar.cpy().lerp(Pal.surge.cpy().mul(Pal.lighterOrange), Mathf.absin(Time.time, 7f / (1f + Mathf.clamp(Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target) / (unit.type().commandLimit * 1f))), 1f)));
            numbers.set(3, Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target) / (unit.type().commandLimit * 1f));
        }
        else if(target instanceof UnitFactory.UnitFactoryBuild factory){
            strings.set(3, factory.unit() == null ? "[lightgray]" + Iconc.cancel :
                    Core.bundle.format("bar.unitcap", Fonts.getUnicodeStr(factory.unit().name), format(factory.team.data().countType(factory.unit())), format(Units.getCap(factory.team))));
            colors.set(3, Pal.power);
            numbers.set(3, factory.unit() == null ? 0f : (float)factory.team.data().countType(factory.unit()) / Units.getCap(factory.team));
        }

        if(target instanceof Unit unit && target instanceof Payloadc pay && unit.type != null){
            strings.set(4, Core.bundle.format("shar-stat.payloadCapacity", format(Mathf.round(Mathf.sqrt(pay.payloadUsed()))), format(Mathf.round(Mathf.sqrt(unit.type().payloadCapacity)))));
            colors.set(4, Pal.items);
            numbers.set(4, pay.payloadUsed() / unit.type().payloadCapacity);
        }
        else if(target instanceof Building build && build.block.hasLiquids){
            strings.set(4, Core.bundle.format("shar-stat.liquidCapacity", format(build.liquids.currentAmount()), format(build.block.liquidCapacity)));
            colors.set(4, build.liquids.current().color);
            numbers.set(4, build.liquids.currentAmount()/build.block.liquidCapacity);
        }


        if(target instanceof Unit unit && state.rules.unitAmmo && unit.type != null){
            strings.set(5, Core.bundle.format("shar-stat.ammos", format(unit.ammo()), format(unit.type().ammoCapacity)));
            colors.set(5, unit.type().ammoType.color);
            numbers.set(5, unit.ammof());
        }
        else if(target instanceof Building build && build.block.hasPower && build.block.consumes.hasPower()){
            ConsumePower cons = build.block.consumes.getPower();
            if(cons.buffered) strings.set(5, Core.bundle.format("shar-stat.powerCapacity", format(build.power.status * cons.capacity * 60f), format(cons.capacity * 60f)));
            else strings.set(5, Core.bundle.format("shar-stat.powerUsage", format(build.power.status * cons.usage * 60f), format(cons.usage * 60f)));
            colors.set(5,Pal.powerBar);
            numbers.set(5, Mathf.zero(cons.requestedPower(build)) && build.power.graph.getPowerProduced() + build.power.graph.getBatteryStored() > 0f ? 1f : build.power.status);
        }
    }
}
