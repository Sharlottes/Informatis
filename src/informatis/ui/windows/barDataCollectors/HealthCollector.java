package informatis.ui.windows.barDataCollectors;

import arc.struct.Seq;
import mindustry.gen.Healthc;
import mindustry.graphics.Pal;

import static arc.Core.bundle;
import static informatis.SUtils.formatNumber;
import static informatis.ui.components.SIcons.health;

public class HealthCollector extends ObjectDataCollector<Healthc> {
    @Override
    public void collectData(Healthc healthc, Seq<BarData> seq) {
        seq.add(new BarData(
                () -> bundle.format("shar-stat.health", formatNumber(healthc.health())),
                Pal.health,
                () -> healthc.healthf(),
                health
        ));
    }
}