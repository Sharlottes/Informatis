package unitinfo.ui.windows;

import arc.util.*;
import mindustry.gen.*;
import unitinfo.ui.windows.*;

public class Windows {
    public static MapEditorDisplay editorTable;

    public static void load(){
        new UnitDisplay();
        new WaveDisplay();
        new CoreDisplay();
        new PlayerDisplay();
        new ToolDisplay();
        editorTable = new MapEditorDisplay();
        new Window(Icon.box, "test-window", t -> {
            t.labelWrap(() -> t.parent.x + ", " + t.parent.y).top().right().growX();
            t.row();
            t.labelWrap(() -> t.parent.getWidth() + ", " + t.parent.getHeight()).top().right().growX();
            t.row();
            t.labelWrap(() -> "T: " + Time.time).top().right().growX();
        });
    }
}
