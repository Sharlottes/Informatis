package informatis.ui.windows.barDataCollectors;

import arc.struct.Seq;

public abstract class ObjectDataCollector<T> {
    public abstract Seq<BarData> collectData(T object);

    public boolean isValid(Object object) {
        return true;
    }
}
