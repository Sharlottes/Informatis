package UnitInfo.ui;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class FreeBar {
    public float value;

    public void draw(Unit unit){
        if(unit.dead()) return;

        float height = 2f;

        if(Float.isNaN(value)) value = 0;
        if(Float.isInfinite(value)) value = 1f;
        value = Mathf.lerpDelta(value, Mathf.clamp(unit.healthf()), 0.15f);

        Draw.z(Layer.flyingUnit + 1);
        Draw.color(0.1f, 0.1f, 0.1f, (settings.getInt("baropacity") / 100f));
        float width = unit.type.hitSize + 4f;

        float x = unit.x;
        float y = unit.y - 8;
        for(int i : Mathf.signs) {
            for(int ii = 0; ii < 2; ii++){
                float shadowx = x + ii * 0.25f;
                float shadowy = y - ii * 0.5f;
                Fill.poly(FloatSeq.with(
                        shadowx - (width / 2 + height), shadowy,
                        shadowx - width / 2, shadowy + i * height,
                        shadowx + width / 2, shadowy + i * height,
                        shadowx + (width / 2 + height), shadowy,
                        shadowx + width / 2, shadowy + i * -height,
                        shadowx - width / 2, shadowy + i * -height));
            }
        }

        {
            Draw.color(Pal.health.cpy().a((settings.getInt("baropacity") / 100f)));
            float topWidth = - width / 2 + width * Mathf.clamp(unit.healthf());
            float moser = topWidth + height;
            if(unit.health <= 0) moser = (width / 2 + height) * (2 * Mathf.clamp(unit.healthf()) - 1);

            for(int i : Mathf.signs) {
                Fill.poly(FloatSeq.with(
                        x - (width / 2 + height), y,
                        x - width / 2, y + i * height,
                        x + topWidth, y + i * height,
                        x + moser, y,
                        x + topWidth, y + i * -height,
                        x - width / 2, y + i * -height));
            }
        }

        if(Vars.state.rules.unitAmmo)
        {
            float topWidth = - width / 2 + width * Mathf.clamp(unit.ammof());
            float moser = topWidth + height;
            if(unit.ammo <= 0) moser = (width / 2 + height) * (2 * Mathf.clamp(unit.ammof()) - 1);

            Draw.color((unit.dead() || unit instanceof BlockUnitc ? Pal.ammo : unit.type.ammoType.color).cpy().a((settings.getInt("baropacity") / 100f)));
            Fill.poly(FloatSeq.with(
                    x - (width / 2 + height), y,
                    x - width / 2, y + height,
                    x + topWidth, y + height,
                    x + moser, y,
                    x + topWidth, y - height,
                    x - width / 2, y - height));
        }

        {
            float max1 = ((ShieldRegenFieldAbility)content.units().copy().filter(ut -> ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility) != null).sort(ut -> ((ShieldRegenFieldAbility)ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max).peek().abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max;
            float max2 = 0f;
            if(unit.type.abilities.find(abil -> abil instanceof ForceFieldAbility) != null) max2 = ((ForceFieldAbility) unit.type.abilities.find(abil -> abil instanceof ForceFieldAbility)).max;
            float max = Mathf.clamp(unit.shield / Math.max(max1, max2));

            float topWidth = - width / 2 + width * max;
            float moser = topWidth + height;
            if(unit.shield <= 0) moser = (width / 2 + height) * (2 * max - 1);

            Draw.color(Pal.surge.cpy().a((settings.getInt("baropacity") / 100f)));
            Fill.poly(FloatSeq.with(
                    x - (width / 2 + height), y,
                    x - width / 2, y - height,
                    x + topWidth, y - height,
                    x + moser, y,
                    x + topWidth, y + height,
                    x - width / 2, y + height));
        }

        Draw.reset();
    }

    public void draw(Building build){
        if(build.dead()
            || (!(build instanceof BaseTurret.BaseTurretBuild) && !(build instanceof Wall.WallBuild))) return;

        float height = 2f;

        if(Float.isNaN(value)) value = 0;
        if(Float.isInfinite(value)) value = 1f;
        value = Mathf.lerpDelta(value, Mathf.clamp(build.healthf()), 0.15f);

        Draw.z(Layer.flyingUnit + 1);
        Draw.color(0.1f, 0.1f, 0.1f, (settings.getInt("baropacity") / 100f));
        float width = build.block.size * 8 / 2f + 4f;

        float x = build.x;
        float y = build.y - 8;
        for(int i : Mathf.signs) {
            for(int ii = 0; ii < 2; ii++){
                float shadowx = x + ii * 0.25f;
                float shadowy = y - ii * 0.5f;
                Fill.poly(FloatSeq.with(
                        shadowx - (width / 2 + height), shadowy,
                        shadowx - width / 2, shadowy + i * height,
                        shadowx + width / 2, shadowy + i * height,
                        shadowx + (width / 2 + height), shadowy,
                        shadowx + width / 2, shadowy + i * -height,
                        shadowx - width / 2, shadowy + i * -height));
            }
        }

        {
            Draw.color(Pal.health.cpy().a((settings.getInt("baropacity") / 100f)));
            float topWidth = - width / 2 + width * Mathf.clamp(build.healthf());
            float moser = topWidth + height;
            if(build.health <= 0) moser = (width / 2 + height) * (2 * Mathf.clamp(build.healthf()) - 1);

            for(int i : Mathf.signs) {
                Fill.poly(FloatSeq.with(
                        x - (width / 2 + height), y,
                        x - width / 2, y + i * height,
                        x + topWidth, y + i * height,
                        x + moser, y,
                        x + topWidth, y + i * -height,
                        x - width / 2, y + i * -height));
            }
        }
        float h = 0;
        Color color = Pal.ammo;
        if(build instanceof ItemTurret.ItemTurretBuild) {
             h = ((ItemTurret.ItemTurretBuild) build).totalAmmo / (((ItemTurret) build.block).maxAmmo * 1f);
             if(((ItemTurret.ItemTurretBuild) build).hasAmmo()) color = ((ItemTurret) build.block).ammoTypes.findKey(((ItemTurret.ItemTurretBuild) build).peekAmmo(), true).color;
        }
        else if(build instanceof LiquidTurret.LiquidTurretBuild){
            LiquidTurret.LiquidTurretBuild entity = (LiquidTurret.LiquidTurretBuild) build;
            Func<Building, Liquid> current;
            current = entity1 -> entity1.liquids == null ? Liquids.water : entity1.liquids.current();

            h = entity.liquids == null ? 0f : entity.liquids.get(current.get(entity)) / entity.block.liquidCapacity;
            color = current.get(entity).color;
        }
        else if(build instanceof PowerTurret.PowerTurretBuild){
            PowerTurret.PowerTurretBuild entity = (PowerTurret.PowerTurretBuild) build;

            h = entity.power.status;
            color = Pal.powerBar;
        }

        if(Core.settings.getBool("range") && build instanceof BaseTurret.BaseTurretBuild) {
            Drawf.dashCircle(build.x, build.y, ((BaseTurret.BaseTurretBuild) build).range(), build.team.color);
        }

        if(build instanceof Turret.TurretBuild) {
            float topWidth = - width / 2 + width * Mathf.clamp(h);
            float moser = topWidth + height;
            if(h <= 0) moser = (width / 2 + height) * (2 * h - 1);

            Draw.color(Tmp.c1.set(color).a(settings.getInt("baropacity") / 100f));
            Fill.poly(FloatSeq.with(
                    x - (width / 2 + height), y,
                    x - width / 2, y + height,
                    x + topWidth, y + height,
                    x + moser, y,
                    x + topWidth, y - height,
                    x - width / 2, y - height));
        }
        Draw.reset();
    }
}
