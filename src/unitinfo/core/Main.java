package unitinfo.core;

import arc.input.KeyCode;
import unitinfo.ui.*;
import unitinfo.ui.draws.OverDraws;
import unitinfo.ui.windows.*;
import arc.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

import static unitinfo.SVars.*;
import static arc.Core.*;
import static unitinfo.SUtils.*;
import static unitinfo.ui.WindowManager.windows;

public class Main extends Mod {
    @Override
    public void init(){
        Core.app.post(() -> {
            Mods.ModMeta meta = Vars.mods.locateMod("unitinfo").meta;
            meta.displayName = "[#B5FFD9]Unit Information[]";
            meta.author = "[#B5FFD9]Sharlotte[lightgray]#0018[][]";
            meta.description = bundle.get("shar-description");
        });

        Events.run(Trigger.update, () -> {
            try {
                BarInfo.getInfo(getTarget());
            } catch (IllegalAccessException | NoSuchFieldException err) {
                err.printStackTrace();
            }

            target = getTarget();

            for (Window window : windows) {
                if(window instanceof Updatable u) u.update();
            }
            if((input.keyDown(KeyCode.shiftRight) || input.keyDown(KeyCode.shiftLeft))) {
                if(input.keyTap(KeyCode.r)) {
                    if(target==getTarget()) locked = !locked;
                    target = getTarget();
                };
            }
        });

        Events.on(ClientLoadEvent.class, e -> {
            Windows.load();

            SettingS.init();
            WindowManager.init();
            DisplayManager.init();
            OverDraws.init();
            OverDrawer.init();
        });
    }
}
