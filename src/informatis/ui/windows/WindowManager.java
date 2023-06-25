package informatis.ui.windows;

import arc.*;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.Nullable;
import mindustry.*;
import mindustry.ui.*;

public class WindowManager {
    public static Seq<Window> windows = new Seq<>();
    public static UnitWindow unitWindow;
    public static MapEditorWindow mapEditorWindow;
    public static Table body;

    public static void init(){
        windows.addAll(
                unitWindow = new UnitWindow(),
            new WaveWindow(),
            new CoreWindow(),
            new PlayerWindow(),
            new ToolWindow(),
            mapEditorWindow = new MapEditorWindow()
        );

        for(Window window : windows) {
            window.build();
        }

        // windows place for dragging
        Vars.ui.hudGroup.fill(t -> {
            t.name = "Windows";

            for(Window window : windows) {
                t.add(window).height(window.getHeight()).width(window.getWidth());
            }
        });

        body = new Table(t -> {
            t.name = "Window Buttons";
            t.left();

            for(Window window : windows){
                t.button(window.icon, Styles.emptyi, () -> {
                    window.parent.setLayoutEnabled(false);
                    window.toggle();
                    for (Window w : windows) {
                        w.setLayoutEnabled(true);
                    }
                }).size(40f).tooltip(tt -> {
                    tt.setBackground(Styles.black6);
                    tt.label(() -> Core.bundle.get("window."+window.name+".name")).pad(2f);
                });
                t.row();
            }
        }).left();
    }
}
