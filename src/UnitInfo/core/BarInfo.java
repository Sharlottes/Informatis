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
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.storage.*;


import static arc.Core.bundle;
import static mindustry.Vars.content;
import static mindustry.Vars.state;

public class BarInfo {
    Seq<String> strings = new Seq<>();
    Seq<Color> colors = new Seq<>();
    Seq<Float> numbers = new Seq<>();

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
            strings.add("[lightgray]<Empty>[]");
            colors.add(Color.clear);
            numbers.add(0f);
        }
        
        
        
        if(target instanceof Healthc) {
            strings.set(0, Core.bundle.format("shar-stat.health", Strings.fixed(((Healthc)target).health(), 1)));
            colors.set(0, Pal.health);
            numbers.set(0, ((Healthc) target).health());
        }

        
        
        if(target instanceof BlockUnitUnit && ((BlockUnitUnit)target).tile() instanceof Turret.TurretBuild) {
            Turret.TurretBuild turretBuild = ((Turret.TurretBuild)((BlockUnitUnit)target).tile());
            float value = Mathf.clamp(turretBuild.reload / ((Turret)turretBuild.block).reloadTime) * 100f;
            strings.set(1, Core.bundle.format("shar-stat.reload", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2))));
            colors.set(1, Pal.accent.cpy().lerp(Color.orange, Mathf.clamp(turretBuild.reload / ((Turret)turretBuild.block).reloadTime)));
            numbers.set(1, turretBuild.reload / ((Turret)turretBuild.block).reloadTime);
        }
        else if(target instanceof Turret.TurretBuild){
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

        
        if(target instanceof ItemTurret.ItemTurretBuild || (target instanceof BlockUnitUnit && ((BlockUnitUnit)target).tile() instanceof ItemTurret.ItemTurretBuild)) {
            ItemTurret.ItemTurretBuild turretBuild = target instanceof ItemTurret.ItemTurretBuild ? (ItemTurret.ItemTurretBuild) target : (ItemTurret.ItemTurretBuild)((BlockUnitUnit)target).tile();
            strings.set(2, bundle.format("shar-stat.itemAmmo", turretBuild.totalAmmo, ((ItemTurret)turretBuild.block).maxAmmo));
            colors.set(2, turretBuild.hasAmmo() ? ((ItemTurret)turretBuild.block).ammoTypes.findKey(turretBuild.peekAmmo(), true).color : Pal.ammo);
            numbers.set(2, turretBuild.totalAmmo / (((ItemTurret)turretBuild.block).maxAmmo * 1f));
        }
        else if(target instanceof LiquidTurret.LiquidTurretBuild || (target instanceof BlockUnitUnit && ((BlockUnitUnit)target).tile() instanceof LiquidTurret.LiquidTurretBuild)){
            LiquidTurret.LiquidTurretBuild turretBuild = target instanceof LiquidTurret.LiquidTurretBuild ? (LiquidTurret.LiquidTurretBuild)target : ((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit)target).tile());
            strings.set(2, bundle.format("shar-stat.liquidAmmo", Strings.fixed(turretBuild.liquids.get(turretBuild.liquids.current()), 1)  + " / " + Strings.fixed(turretBuild.block.liquidCapacity, 1)));
            colors.set(2, turretBuild.liquids.current().color);
            numbers.set(2, turretBuild.liquids.get(turretBuild.liquids.current()) / turretBuild.block.liquidCapacity);
        }
        else if(target instanceof PowerTurret.PowerTurretBuild || (target instanceof BlockUnitUnit && ((BlockUnitUnit)target).tile() instanceof PowerTurret.PowerTurretBuild)){
            PowerTurret.PowerTurretBuild entity = target instanceof PowerTurret.PowerTurretBuild ? (PowerTurret.PowerTurretBuild)target : ((PowerTurret.PowerTurretBuild)((BlockUnitUnit)target).tile());
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

        
        
        if(target instanceof Turret.TurretBuild || (target instanceof BlockUnitUnit && ((BlockUnitUnit) target).tile() instanceof Turret.TurretBuild)){
            Turret turret = target instanceof Turret.TurretBuild ? (Turret)((Turret.TurretBuild)target).block : (Turret)(((BlockUnitUnit) target).tile()).block;
            if(turret.chargeTime > 0f) return;
            float value = Mathf.clamp(Main.hud.heat2 / turret.chargeTime) * 100f;
            strings.set(3, Core.bundle.format("shar-stat.charge", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2))));
            colors.set(3, Pal.surge.cpy().lerp(Pal.accent, Main.hud.heat2 / turret.chargeTime));
            numbers.set(3, Main.hud.heat2 / turret.chargeTime);
        }
        else if(target instanceof Unit && !(target instanceof BlockUnitUnit) && ((Unit) target).type != null) {
            strings.set(3, Core.bundle.format("shar-stat.commandUnits", Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target), ((Unit)target).type().commandLimit));
            colors.set(3, Pal.powerBar.cpy().lerp(Pal.surge.cpy().mul(Pal.lighterOrange), Mathf.absin(Time.time, 7f / (1f + Mathf.clamp(Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target) / (((Unit)target).type().commandLimit * 1f))), 1f)));
            numbers.set(3, Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == target) / (((Unit)target).type().commandLimit * 1f));
        }



        if(target instanceof Unit && target instanceof Payloadc && ((Unit) target).type != null){
            strings.set(4, Core.bundle.format("shar-stat.payloadCapacity", Mathf.round(Mathf.sqrt(((Payloadc)target).payloadUsed())), Mathf.round(Mathf.sqrt(((Unit)target).type().payloadCapacity))));
            colors.set(4, Pal.items);
            numbers.set(4, ((Payloadc)target).payloadUsed() / ((Unit)target).type().payloadCapacity);
        }



        if(target instanceof Unit && state.rules.unitAmmo&& ((Unit) target).type != null){
            strings.set(5, Core.bundle.format("shar-stat.ammos", ((Unit)target).ammo(), ((Unit)target).type().ammoCapacity));
            colors.set(5, ((Unit)target).type().ammoType.color);
            numbers.set(5, ((Unit)target).ammof());
        }

    }
}
