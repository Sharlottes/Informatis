package UnitInfo.ui;

import UnitInfo.ui.windows.WindowTable;
import arc.func.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.ui.*;

public class TaskbarTable extends Table{
    public static Boolp visibility = () -> Vars.ui.hudfrag.shown && !Vars.ui.minimapfrag.shown();

    public TaskbarTable(WindowTable... items){
        visible = true;
        table(MindowsTex.sidebar, t -> {
            t.top().center();
            for(WindowTable w : items){
                t.button(w.icon, Styles.emptyi, () -> {
                    w.visible(visibility);
                }).disabled(b -> w.visible).size(40f).padRight(5f);
                t.row();
            }
        }).left().center().width(40f);
    }
}