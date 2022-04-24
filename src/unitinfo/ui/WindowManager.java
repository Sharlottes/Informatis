package unitinfo.ui;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ui.*;
import unitinfo.ui.window.Window;

public class WindowManager {
    public static Seq<Window> windows = new Seq<>();

    public static void init(){
        Log.info(windows.size);

        Vars.ui.hudGroup.fill(t -> {
            t.name = "Windows";
            for(Window window : windows){
                t.add(window);
            }
        });

        Vars.ui.hudGroup.fill(t -> {
            t.center().left();
            t.table(Core.atlas.drawable("unitinfo-sidebar"), b -> {
                b.name = "Window Buttons";
                b.left();

                for(Window window : windows){
                    b.button(window.icon, Styles.emptyi, () -> {
                        window.toggle();

                        // Disabling the parent's layout fixes issues with updating elements inside windows.
                        // However, it also disables the layout of all its children, so we need to re-enable them.
                        window.parent.setLayoutEnabled(false);
                        window.setLayoutEnabled(true);
                        for(Window w : windows){
                            w.setLayoutEnabled(true);
                        }
                    }).disabled(window.shown)
                        .size(40f)
                        .tooltip(tt -> {
                            tt.setBackground(Styles.black6);
                            tt.label(() -> Core.bundle.get("window."+window.name+".name")).pad(2f);
                        });
                    b.row();
                }
            }).left();
        });
    }

    public static int register(Window window){
        windows.add(window);
        return windows.size - 1;
    }
}
