package informatis.ui.windows.barDataCollectors;

import arc.struct.Seq;

public abstract class ObjectDataCollector<T> {
    public abstract void collectData(T object, Seq<BarData> out);

    public boolean isValid(Object object) {
        return true;
    }
}
