package informatis.ui.components;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import mindustry.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.payloads.Payload;

import informatis.SVars;

import static arc.Core.*;
import static mindustry.Vars.*;

public class FreeBar {
    public static void draw(Unit unit){
        if(unit.dead()) return;

        Draw.z(Layer.flyingUnit + 1);

        Bits statuses = new Bits();
        Bits applied = unit.statusBits();
        if(!statuses.equals(applied) && applied != null){
            int i = 0;
            int row = 0;
            for(StatusEffect effect : content.statusEffects()){
                if(applied.get(effect.id) && !effect.isHidden()){
                    Draw.rect(effect.uiIcon, unit.x - (unit.type.hitSize + 4)/2 + i * 4, unit.y - 4 + 4 * row, 4,4);
                    if(++i > 2 * (unit.type.hitSize + 4)) row++;
                }
            }
            statuses.set(applied);
        }

        if(unit instanceof Payloadc payload && payload.payloads().any()){
            int i = 0;
            int row = 0;
            for(Payload p : payload.payloads()){
                Draw.rect(p.icon(), unit.x - (unit.type.hitSize + 4)/2 + i * 4, unit.y - 12 - 4 * row, 4,4);
                if(++i > 2 * (unit.type.hitSize + 4)) row++;
            }
        }

        Draw.color(0.1f, 0.1f, 0.1f, (settings.getInt("baropacity") / 100f));
        float width = unit.type.hitSize + 4f;
        float height = 2f;
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

        if(Vars.state.rules.unitAmmo) {
            float topWidth = - width / 2 + width * Mathf.clamp(unit.ammof());
            float moser = topWidth + height;
            if(unit.ammo <= 0) moser = (width / 2 + height) * (2 * Mathf.clamp(unit.ammof()) - 1);

            Draw.color((unit.dead() || unit instanceof BlockUnitc ? Pal.ammo : unit.type.ammoType.color()).cpy().a((settings.getInt("baropacity") / 100f)));
            Fill.poly(FloatSeq.with(
                    x - (width / 2 + height), y,
                    x - width / 2, y + height,
                    x + topWidth, y + height,
                    x + moser, y,
                    x + topWidth, y - height,
                    x - width / 2, y - height));
        }

        float maxShield = -1;
        for(Ability ability : unit.abilities) {
            if(ability instanceof  ForceFieldAbility forceFieldAbility) {
                maxShield = forceFieldAbility.max;
            }
        }
        if(maxShield != -1) {

            float max = Mathf.clamp(unit.shield / Math.max(SVars.maxShieldAmongUnits, maxShield));

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
}
