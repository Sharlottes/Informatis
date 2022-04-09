package UnitInfo.ui;

import arc.scene.*;
import mindustry.ui.fragments.*;

import static UnitInfo.ui.windows.WindowTables.unitTable;
import static UnitInfo.ui.windows.WindowTables.waveTable;

public class HUDFragment extends Fragment{
    @Override
    public void build(Group parent){
        parent.fill(t -> {
            t.name = "Windows";
            t.visible(() -> parent.visible);

            // windows (totally not a copyright violation)
            t.center().right();
            t.add(unitTable).size(250f).visible(false);
            t.add(waveTable).size(250f).visible(false);

            // sidebar
            t.add(new TaskbarTable(
                    unitTable,
                    waveTable
            )).visible(TaskbarTable.visibility);

            t.update(()->{
                for (Element child : t.getChildren()) {
                    if(child instanceof Updatable u) u.setEvent();
                }
            });
        });
    };
}