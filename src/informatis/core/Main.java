package informatis.core;

import arc.input.KeyCode;
import informatis.SVars;
import informatis.ui.*;
import informatis.ui.draws.OverDraws;
import informatis.ui.fragments.FragmentManager;
import informatis.ui.windows.*;
import arc.*;
import mindustry.*;
import mindustry.ai.Pathfinder;
import mindustry.game.EventType.*;
import mindustry.mod.*;

import static informatis.SVars.*;
import static arc.Core.*;
import static informatis.SUtils.*;
import static informatis.ui.WindowManager.windows;

public class Main extends Mod {
    @Override
    public void init(){
        Core.app.post(() -> {
            Mods.ModMeta meta = Vars.mods.locateMod("informatis").meta;
            meta.displayName = "[#B5FFD9]Informatis[]";
            meta.author = "[#B5FFD9]Sharlotte[lightgray]#0018[][]";
            meta.description = bundle.get("shar-description");
        });

        Events.run(Trigger.update, () -> {
            target = getTarget();

            for (Window window : windows) {
                if(window instanceof Updatable u) u.update();
            }

            if((input.keyDown(KeyCode.shiftRight) || input.keyDown(KeyCode.shiftLeft))) {
                if(input.keyTap(KeyCode.r)) {
                    if(target==getTarget()) locked = !locked;
                    target = getTarget();
                }
            }
        });

        Events.on(ClientLoadEvent.class, e -> {
            Windows.load();

            Setting.init();
            WindowManager.init();
            FragmentManager.init();
            OverDraws.init();
            OverDrawer.init();

            SVars.pathfinder = new informatis.core.Pathfinder();
        });
    }
}
