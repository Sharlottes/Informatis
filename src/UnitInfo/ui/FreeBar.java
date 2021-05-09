package UnitInfo.ui;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.FloatSeq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

import static arc.Core.settings;
import static mindustry.Vars.content;

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
        Draw.color(Pal.health.cpy().a((settings.getInt("baropacity") / 100f)));
        float topWidth = - width / 2 + width * Mathf.clamp(unit.healthf());
        for(int i : Mathf.signs) {
            Fill.poly(FloatSeq.with(
                    x - (width / 2 + height), y,
                    x - width / 2, y + i * height,
                    x + topWidth, y + i * height,
                    x - (width / 2 + height) + (width + 2 * height) * Mathf.clamp(unit.healthf()), y,
                    x + topWidth, y + i * -height,
                    x - width / 2, y + i * -height));
        }
        if(Vars.state.rules.unitAmmo){
            Draw.color((unit.dead() || unit instanceof BlockUnitc ? Pal.ammo : unit.type.ammoType.color).cpy().a((settings.getInt("baropacity") / 100f)));
            topWidth = - width / 2 + width * Mathf.clamp(unit.ammof());
    
            Fill.poly(FloatSeq.with(
                    x - (width / 2 + height), y,
                    x - width / 2, y + height,
                    x + topWidth, y + height,
                    x - (width / 2 + height) + (width + 2 * height) * Mathf.clamp(unit.ammof()), y,
                    x + topWidth, y - height,
                    x - width / 2, y - height));
        }

        float max1 = ((ShieldRegenFieldAbility)content.units().copy().filter(ut -> ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility) != null).sort(ut -> ((ShieldRegenFieldAbility)ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max).peek().abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max;
        float max2 = 0f;
        if(unit.type.abilities.find(abil -> abil instanceof ForceFieldAbility) != null) max2 = ((ForceFieldAbility) unit.type.abilities.find(abil -> abil instanceof ForceFieldAbility)).max;
        float max = Mathf.clamp(unit.shield / Math.max(max1, max2));
        
        Draw.color(Pal.surge.cpy().a((settings.getInt("baropacity") / 100f)));
        topWidth = - width / 2 + width * max;

        Fill.poly(FloatSeq.with(
                x - (width / 2 + height), y,
                x - width / 2, y - height,
                x + topWidth, y - height,
                x - (width / 2 + height) + (width + 2 * height) * max, y,
                x + topWidth, y + height,
                x - width / 2, y + height));
        Draw.reset();
    }
}
