package informatis.ui.windows.barDataCollectors;

import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.gen.Iconc;
import mindustry.graphics.Pal;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.world.blocks.units.UnitAssembler;

import static arc.Core.bundle;

public class UnitAssemblerCollector extends ObjectDataCollector<UnitAssembler.UnitAssemblerBuild> {
    @Override
    public void collectData(UnitAssembler.UnitAssemblerBuild assemblerBuild, Seq<BarData> out) {
        UnitType unit = assemblerBuild.unit();
        if(unit == null) {
            out.add(new BarData(() -> "[lightgray]" + Iconc.cancel, Pal.power, () -> 0f));
        }
        else {
            out.add(new BarData(
                () -> bundle.format("shar-stat.progress", Math.round(assemblerBuild.progress * 100 * 100) / 100),
                Pal.power,
                () -> assemblerBuild.progress
            ));
        }

        UnitAssembler.AssemblerUnitPlan plan = assemblerBuild.plan();
        for(PayloadStack stack : plan.requirements) {
            out.add(new BarData(
                () -> stack.item.localizedName + ": " + assemblerBuild.blocks.get(stack.item) + " / " + stack.amount,
                Pal.accent, Color.orange,
                () -> assemblerBuild.blocks.get(stack.item) / stack.amount,
                stack.item.fullIcon
            ));
        }
    }
}
