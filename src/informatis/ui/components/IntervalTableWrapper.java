package informatis.ui.components;

import arc.func.Boolf;
import arc.func.Cons;
import arc.scene.ui.layout.Table;
import arc.util.Time;

public
class IntervalTableWrapper {
    final Cons<Table> tableCons;
    final Boolf<Table> condition;
    final int interval;

    public IntervalTableWrapper() {
        this(t -> {});
    }
    public IntervalTableWrapper(Cons<Table> tableCons) {
        this(tableCons, (t) -> true, 60);
    }
    public IntervalTableWrapper(Cons<Table> tableCons, int interval) {
        this(tableCons, (t) -> true, interval);
    }
    public IntervalTableWrapper(Cons<Table> tableCons, Boolf<Table> condition, int interval) {
        this.tableCons = tableCons;
        this.condition = condition;
        this.interval = interval;
    }

    float t = 0;
    public Table build() {
        Table table = new Table(tableCons);

        table.update(() -> {
            t -= Time.delta;
            if(t <= 0) {
                t = interval;
                if(!condition.get(table)) return;
                table.clearChildren();
                tableCons.get(table);
            }
        });

        return table;
    }
}