package UnitInfo.ui;

import arc.scene.*;
import mindustry.ui.fragments.*;

import static UnitInfo.ui.windows.WindowTables.unitTable;

public class HUDFragment extends Fragment{
    @Override
    public void build(Group parent){
        parent.fill(t -> {
            t.name = "Windows";
            t.visible(() -> parent.visible);

            // windows (totally not a copyright violation)
            t.center().right();
            t.add(unitTable).size(250f).visible(false);

            // sidebar
            t.add(new TaskbarTable(
                    unitTable
            )).visible(TaskbarTable.visibility);

            t.update(()->{
                if(unitTable instanceof Updatable u) u.setEvent();
            });
        });
    };
}