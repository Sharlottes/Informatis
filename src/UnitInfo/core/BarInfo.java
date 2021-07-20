package UnitInfo.core;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import mindustry.ai.types.FormationAI;
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.consumers.ConsumePower;


import static arc.Core.bundle;
import static mindustry.Vars.content;
import static mindustry.Vars.state;

public class BarInfo {
    Seq<String> strings = new Seq<>(new String[]{"","","","","",""});
    Seq<Float> numbers = new Seq<>(new Float[]{0f,0f,0f,0f,0f,0f});
    Seq<Color> colors = new Seq<>(new Color[]{Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear});

    public <T extends Teamc> Seq<String> returnStrings(T target){
        getInfo(target);
        return strings;
    }
    public <T extends Teamc> Seq<Color> returnColors(T target){
        getInfo(target);
        return colors;
    }
    public <T extends Teamc> Seq<Float> returnNumbers(T target){
        getInfo(target);
        return numbers;
    }

    public <T extends Teamc> void getInfo(T target){
        for(int i = 0; i < 6; i++) { //init
            strings.set(i, "[lightgray]<Empty>[]");
            colors.set(i, Color.clear);
            numbers.set(i, 0f);
        }
        
        
        
        if(target instanceof Healthc) {
            strings.set(0, Core.bundle.format("shar-stat.health", Strings.fixed(((Healthc)target).health(), 1)));
            colors.set(0, Pal.health);
            numbers.set(0, ((Healthc) target).healthf());
        }

        
        
        if(target instanceof Turret.TurretBuild){
            float value = Mathf.clamp(((Turret.TurretBuild)target).reload / ((Turret)((Turret.TurretBuild)target).block).reloadTime) * 100f;
            strings.set(1, Core.bundle.format("shar-stat.reload", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2))));
            colors.set(1, Pal.accent.cpy().lerp(Color.orange, Mathf.clamp(((Turret.TurretBuild)target).reload / ((Turret)((Turret.TurretBuild)target).block).reloadTime)));
            numbers.set(1, ((Turret.TurretBuild)target).reload / ((Turret)((Turret.TurretBuild)target).block).reloadTime);
        }
        else if(target instanceof Unit && ((Unit) target).type != null){
            float max1 = ((ShieldRegenFieldAbility)content.units().copy().filter(ut -> ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility) != null).sort(ut -> ((ShieldRegenFieldAbility)ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max).peek().abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max;
            float max2 = 0f;
            if(((Unit)target).type().abilities.find(abil -> abil instanceof ForceFieldAbility) != null) max2 = ((ForceFieldAbility) ((Unit)target).type().abilities.find(abil -> abil instanceof ForceFieldAbility)).max;
            strings.set(1, Core.bundle.format("shar-stat.shield", Strings.fixed(((Shieldc)target).shield(),1)));
            colors.set(1, Pal.surge);
            numbers.set(1, ((Unit)target).shield() / Math.max(max1, max2));
        }
        else if(target instanceof ConstructBlock.ConstructBuild){
            ConstructBlock.ConstructBuild construct = (ConstructBlock.ConstructBuild) target;
            strings.set(1, Core.bundle.format("shar-stat.progress", Strings.fixed(construct.progress * 100, 1)));
            colors.set(1, Pal.darkerMetal);
            numbers.set(1, construct.progress);
        }

        
        if(target instanceof ItemTurret.ItemTurretBuild) {
            ItemTurret.ItemTurretBuild turretBuild = (ItemTurret.ItemTurretBuild) target;
            strings.set(2, bundle.format("shar-stat.itemAmmo", turretBuild.totalAmmo, ((ItemTurret)turretBuild.block).maxAmmo));
            colors.set(2, turretBuild.hasAmmo() ? ((ItemTurret)turretBuild.block).ammoTypes.findKey(turretBuild.peekAmmo(), true).color : Pal.ammo);
            numbers.set(2, turretBuild.totalAmmo / (((ItemTurret)turretBuild.block).maxAmmo * 1f));
        }
        else if(target instanceof LiquidTurret.LiquidTurretBuild){
            LiquidTurret.LiquidTurretBuild turretBuild = (LiquidTurret.LiquidTurretBuild)target;
            strings.set(2, bundle.format("shar-stat.liquidAmmo", Strings.fixed(turretBuild.liquids.get(turretBuild.liquids.current()), 1)  + " / " + Strings.fixed(turretBuild.block.liquidCapacity, 1)));
            colors.set(2, turretBuild.liquids.current().color);
            numbers.set(2, turretBuild.liquids.get(turretBuild.liquids.current()) / turretBuild.block.liquidCapacity);
        }
        else if(target instanceof PowerTurret.PowerTurretBuild){
            PowerTurret.PowerTurretBuild entity = (PowerTurret.PowerTurretBuild)target;
            float max = entity.block.consumes.getPower().usage;
            float v = entity.power.status * entity.power.graph.getLastScaledPowerIn();
            strings.set(2, bundle.format("shar-stat.power", (int)(Math.min(v,max) * 60), (int)(max * 60)));
            colors.set(2, Pal.powerBar);
            numbers.set(2, v/max);
        }
        else if(target instanceof Building && ((Building)target).block.hasItems) {
            if(target instanceof CoreBlock.CoreBuild){
                CoreBlock.CoreBuild core = (CoreBlock.CoreBuild)target;
                strings.set(2, bundle.format("shar-stat.itemCapacity", UI.formatAmount(((Building) target).items.total()), UI.formatAmount((long) (core.storageCapacity * content.items().count(UnlockableContent::unlockedNow) * 1f))));
                numbers.set(2, ((Building)target).items.total() / (core.storageCapacity * content.items().count(UnlockableContent::unlockedNow) * 1f));
            }
            else if(target instanceof StorageBlock.StorageBuild && !((StorageBlock.StorageBuild)target).canPickup()){
                for(int i = 0; i < 4; i++) {
                    Building build = ((Building) target).nearby(i);
                    if(build instanceof CoreBlock.CoreBuild){
                        strings.set(2, bundle.format("shar-stat.itemCapacity", UI.formatAmount(((Building) target).items.total()), UI.formatAmount((long) (((CoreBlock.CoreBuild) build).storageCapacity * content.items().count(UnlockableContent::unlockedNow) * 1f))));
                        numbers.set(2, ((Building)target).items.total() / (((CoreBlock.CoreBuild) build).storageCapacity * content.items().count(UnlockableContent::unlockedNow) * 1f));
                        break;
                    }
                }
            }
            else {
                strings.set(2, bundle.format("shar-stat.itemCapacity", UI.formatAmount(((Building)target).items.total()), UI.formatAmount(((Building)target).block.itemCapacity)));
                numbers.set(2, ((Building)target).items.total() / (((Building)target).block.itemCapacity * 1f));
            }
            colors.set(2, Pal.items);
        }
        else if(target instanceof Unit && ((Unit)target).type() != null) {
            strings.set(2, bundle.format("shar-stat.itemCapacity", UI.formatAmount(((Unit)target).stack().amount), UI.formatAmount(((Unit)target).type().itemCapacity)));
            colors.set(2, ((Unit)target).stack().item.color.cpy().lerp(Color.white, 0.15f));
            numbers.set(2, ((Unit)target).stack().amount / (((Unit)target).type().itemCapacity * 1f));
        }

        
        
        if(target instanceof Turret.TurretBuild){
            Turret turret = (Turret)((Turret.TurretBuild)target).block;
            if(turret.chargeTime > 0f) {
                float value = Mathf.clamp(Main.hud.heat2 / turret.chargeTime) * 100f;
                strings.set(3, Core.bundle.format("shar-stat.charge", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2))));
                colors.set(3, Pal.surge.cpy().lerp(Pal.accent, Main.hud.heat2 / turret.chargeTime));
                numbers.set(3, Main.hud.heat2 / turret.chargeTime);
            }
        }
        else if(target instanceof Unit && ((Unit) target).type != null) {
            strings.set(3, Core.bundle.format("shar-stat.commandUnits", Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target), ((Unit)target).type().commandLimit));
            colors.set(3, Pal.powerBar.cpy().lerp(Pal.surge.cpy().mul(Pal.lighterOrange), Mathf.absin(Time.time, 7f / (1f + Mathf.clamp(Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target) / (((Unit)target).type().commandLimit * 1f))), 1f)));
            numbers.set(3, Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target) / (((Unit)target).type().commandLimit * 1f));
        }



        if(target instanceof Unit && target instanceof Payloadc && ((Unit) target).type != null){
            strings.set(4, Core.bundle.format("shar-stat.payloadCapacity", Mathf.round(Mathf.sqrt(((Payloadc)target).payloadUsed())), Mathf.round(Mathf.sqrt(((Unit)target).type().payloadCapacity))));
            colors.set(4, Pal.items);
            numbers.set(4, ((Payloadc)target).payloadUsed() / ((Unit)target).type().payloadCapacity);
        }
        else if(target instanceof Building && ((Building) target).block.hasLiquids){
            Building build = (Building) target;
            strings.set(4, Core.bundle.format("shar-stat.liquidCapacity", Strings.fixed(build.liquids.currentAmount(),1), Strings.fixed(build.block.liquidCapacity,1)));
            colors.set(4, build.liquids.current().color);
            numbers.set(4, build.liquids.currentAmount()/build.block.liquidCapacity);
        }



        if(target instanceof Unit && state.rules.unitAmmo && ((Unit) target).type != null){
            strings.set(5, Core.bundle.format("shar-stat.ammos", ((Unit)target).ammo(), ((Unit)target).type().ammoCapacity));
            colors.set(5, ((Unit)target).type().ammoType.color);
            numbers.set(5, ((Unit)target).ammof());
        }
        else if(target instanceof Building && ((Building) target).block.hasPower && ((Building) target).block.consumes.hasPower()){
            Building build = (Building) target;
            ConsumePower cons = build.block.consumes.getPower();
            if(cons.buffered) strings.set(5, Core.bundle.format("shar-stat.powerCapacity", Strings.fixed(build.power.status * cons.capacity * 60f, 1), Strings.fixed(cons.capacity * 60f, 1)));
            else strings.set(5, Core.bundle.format("shar-stat.powerUsage", Strings.fixed(build.power.status * cons.usage * 60f, 1), Strings.fixed(cons.usage * 60f, 1)));
            colors.set(5,Pal.powerBar);
            numbers.set(5, Mathf.zero(cons.requestedPower(build)) && build.power.graph.getPowerProduced() + build.power.graph.getBatteryStored() > 0f ? 1f : build.power.status);
        }
    }
}
