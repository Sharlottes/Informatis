package informatis.ui.windows;

import arc.*;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import mindustry.*;
import mindustry.graphics.Pal;
import mindustry.ui.*;

import java.lang.reflect.InvocationTargetException;

public class WindowManager {
    public static ObjectMap<Class<? extends Window>, Seq<Window>> windows = new ObjectMap<>();

    public static void init(){
        Vars.ui.hudGroup.fill(t -> {
            t.name = "Windows";

            for(Seq<Window> windows : windows.values()){
                for(Window window : windows) {
                    t.add(window).height(window.getHeight()).width(window.getWidth());
                }
            }
        });

        Vars.ui.hudGroup.fill(t -> {
            t.name = "window sidebar";
            t.center().left();
            t.table(Core.atlas.drawable("informatis-sidebar"), b -> {
                b.name = "Window Buttons";
                b.left();

                for(ObjectMap.Entry<Class<? extends Window>, Seq<Window>> windows : windows){
                    Class<? extends Window> key = windows.key;
                    Seq<Window> value = windows.value;
                    Window window = value.peek();
                    b.stack(
                            new Table(bt -> {
                                bt.button(window.icon, Styles.emptyi, () -> {
                                    Window window1 = window;
                                    window1.parent.setLayoutEnabled(false);
                                    if(!window.only) {
                                        try {
                                            window1 = key.getConstructor().newInstance();
                                            window1.setPosition(value.peek().x + 50, value.peek().y + 50);
                                            window1.sizeBy(200, 200);
                                        } catch (InstantiationException | IllegalAccessException |
                                                 InvocationTargetException | NoSuchMethodException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    }
                                    window1.toggle();
                                    for (Seq<Window> ws : WindowManager.windows.values()) {
                                        ws.each(w -> w.setLayoutEnabled(true));
                                    }
                                }).size(40f).tooltip(tt -> {
                                    tt.setBackground(Styles.black6);
                                    tt.label(() -> Core.bundle.get("window."+window.name+".name")).pad(2f);
                                });
                            }),
                            new Table(bt -> {
                                bt.right().bottom();
                                if(!window.only) bt.label(() -> String.valueOf(value.size)).get().setColor(Pal.accent);
                            })
                    ).row();
                }
            }).left();
        });
    }

    public static void register(Window window){
        Table table = Vars.ui.hudGroup.find("Windows");
        if(table != null) table.add(window).height(window.getHeight()).width(window.getWidth());
        if(!windows.containsKey(window.getClass())) windows.put(window.getClass(), Seq.with(window));
        else windows.get(window.getClass()).add(window);
    }
}
