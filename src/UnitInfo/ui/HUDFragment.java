package UnitInfo.ui;

import arc.scene.*;
import mindustry.ui.fragments.*;

import static UnitInfo.ui.windows.WindowTables.*;

public class HUDFragment extends Fragment{
    @Override
    public void build(Group parent){
        parent.fill(t -> {
            t.name = "Windows";
            t.visible(() -> parent.visible);

            t.center().left();
            // sidebar
            t.add(new TaskbarTable(
                    unitTable,
                    waveTable,
                    coreTable,
                    playerTable,
                    toolTable,
                    editorTable
            )).visible(TaskbarTable.visibility);

            // windows (totally not a copyright violation)
            t.add(unitTable).size(250f).visible(false);
            t.add(waveTable).size(250f).visible(false);
            t.add(coreTable).size(250f).visible(false);
            t.add(playerTable).size(250f).visible(false);
            t.add(toolTable).size(250f).visible(false);
            t.add(editorTable).size(250f).visible(false);

            t.update(()->{
                for (Element child : t.getChildren()) {
                    if(child instanceof Updatable u) u.update();
                }
            });
        });
    };
}