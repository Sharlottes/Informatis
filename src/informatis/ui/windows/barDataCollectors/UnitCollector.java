package informatis.ui.windows.barDataCollectors;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.entities.abilities.Ability;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.gen.Payloadc;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;

import static arc.Core.bundle;
import static informatis.SUtils.formatNumber;
import static informatis.ui.components.SIcons.item;
import static informatis.ui.components.SIcons.shield;
import static mindustry.Vars.content;
import static mindustry.Vars.state;

public
class UnitCollector extends ObjectDataCollector<Unit> {
    private float maxUnitShieldAmount;

    public UnitCollector() {
        super();
        Core.app.post(() -> {
            for(UnitType unitType : content.units()) {
                for(Ability ability : unitType.abilities) {
                    if(ability instanceof ShieldRegenFieldAbility shieldRegenFieldAbility) {
                        if(shieldRegenFieldAbility.max > maxUnitShieldAmount) {
                            maxUnitShieldAmount = shieldRegenFieldAbility.max;
                        }
                        break;
                    }
                }
            }
        });
    }

    @Override
    public Seq<BarData> collectData(Unit unit) {
        Seq<BarData> seq = new Seq<>();
        seq.add(new BarData(
                () -> bundle.format("shar-stat.shield", formatNumber(unit.shield())),
                () -> Pal.surge,
                () -> unit.shield() / maxUnitShieldAmount,
                () -> shield
        ));
        seq.add(new BarData(
                () -> bundle.format("shar-stat.capacity", unit.stack.item.localizedName, formatNumber(unit.stack.amount), formatNumber(unit.type.itemCapacity)),
                () -> unit.stack.amount > 0 && unit.stack().item != null ? unit.stack.item.color.cpy().lerp(Color.white, 0.15f) : Color.white,
                () -> unit.stack.amount / (unit.type.itemCapacity * 1f),
                () -> item
        ));
        if(unit instanceof Payloadc pay) seq.add(new BarData(
                () -> bundle.format("shar-stat.payloadCapacity", formatNumber(Mathf.round(Mathf.sqrt(pay.payloadUsed()))), formatNumber(Mathf.round(Mathf.sqrt(unit.type().payloadCapacity)))),
                () -> Pal.items,
                () -> pay.payloadUsed() / unit.type().payloadCapacity
        ));
        if(state.rules.unitAmmo) seq.add(new BarData(
                () -> bundle.format("shar-stat.ammos", formatNumber(unit.ammo()), formatNumber(unit.type().ammoCapacity)),
                () -> unit.type().ammoType.color(),
                () -> unit.ammof()
        ));
        return seq;
    }
}
