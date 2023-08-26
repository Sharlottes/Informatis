package informatis;

import informatis.core.OverDrawer;
import informatis.core.Setting;
import informatis.draws.OverDraws;
import informatis.ui.fragments.FragmentManager;
import informatis.ui.fragments.sidebar.dialogs.DialogManager;
import informatis.ui.fragments.sidebar.windows.*;
import arc.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

import static arc.Core.*;

public class Informatis extends Mod {
    @Override
    public void init(){
        Core.app.post(() -> {
            Mods.ModMeta meta = Vars.mods.locateMod("informatis").meta;
            meta.displayName = "[#B5FFD9]Informatis[]";
            meta.author = "[#B5FFD9]Sharlotte[lightgray]#0018[][]";
            meta.description = bundle.get("shar-description");
        });

        Events.on(ClientLoadEvent.class, e -> {
            Setting.init();
            WindowManager.init();
            DialogManager.init();
            FragmentManager.init();
            OverDraws.init();
            OverDrawer.init();
            SVars.init();
        });
    }
}
