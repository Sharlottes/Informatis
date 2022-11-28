package informatis;

import arc.input.KeyCode;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import informatis.core.OverDrawer;
import informatis.core.Setting;
import informatis.draws.OverDraws;
import informatis.ui.SidebarSwitcher;
import informatis.ui.TroopingManager;
import informatis.ui.dialogs.DialogManager;
import informatis.ui.dialogs.ResourcePreviewDialog;
import informatis.ui.fragments.FragmentManager;
import informatis.ui.windows.*;
import arc.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.mod.*;

import static arc.Core.*;
import static informatis.ui.windows.WindowManager.windows;

public class Informatis extends Mod {
    @Override
    public void init(){
        Core.app.post(() -> {
            Mods.ModMeta meta = Vars.mods.locateMod("informatis").meta;
            meta.displayName = "[#B5FFD9]Informatis[]";
            meta.author = "[#B5FFD9]Sharlotte[lightgray]#0018[][]";
            meta.description = bundle.get("shar-description");
        });

        Events.run(Trigger.update, () -> {
            if((input.keyDown(KeyCode.shiftRight) || input.keyDown(KeyCode.shiftLeft))) {
                if(input.keyTap(KeyCode.r)) {
                    UnitWindow.currentWindow.locked = !UnitWindow.currentWindow.locked;
                }
            }
            int i = 0;
            for(KeyCode numCode : KeyCode.numbers) {
                if(input.keyTap(numCode)) {
                    if(input.keyDown(KeyCode.altLeft)) TroopingManager.applyTrooping(i);
                    else if(input.keyDown(KeyCode.capsLock)) TroopingManager.updateTrooping(i);
                    else TroopingManager.selectTrooping(i);
                    break;
                }
                i++;
            }
        });

        Events.on(ClientLoadEvent.class, e -> {
            Setting.init();
            WindowManager.init();
            DialogManager.init();
            TroopingManager.init();
            new SidebarSwitcher(
                WindowManager.body,
                DialogManager.body,
                TroopingManager.body,
                new Table(Tex.buttonEdge4,  t -> {
                    t.label(() -> "it's just label lmao");
                })
            ).init();
            FragmentManager.init();
            OverDraws.init();
            OverDrawer.init();

            //TODO - SVars.init()?
            SVars.pathfinder = new informatis.core.Pathfinder();
        });
    }
}
