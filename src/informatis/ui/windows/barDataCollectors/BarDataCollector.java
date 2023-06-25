package informatis.ui.windows.barDataCollectors;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.gen.Healthc;
import mindustry.gen.Unit;

public class BarDataCollector {
    private static final ObjectMap<Class<Object>, ObjectDataCollector<Object>> collectors = ObjectMap.of(
            Healthc.class, new HealthCollector(),
            Unit.class, new UnitCollector()
    );

    public static Seq<BarData> getBarData(Object target) {
        Seq<BarData> res = new Seq<>();
        for (ObjectMap.Entry<Class<Object>, ObjectDataCollector<Object>> collectorEntry : collectors) {
            if (collectorEntry.key.isAssignableFrom(target.getClass()) && collectorEntry.value.isValid(target))
                collectorEntry.value.collectData(target, res);
        }
        return res;
    }
}
