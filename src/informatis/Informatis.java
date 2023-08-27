package informatis;

import informatis.core.OverDrawer;
import informatis.core.Setting;
import informatis.ui.fragments.sidebar.windows.tools.draws.OverDrawManager;
import informatis.ui.fragments.FragmentManager;
import informatis.ui.fragments.sidebar.dialogs.DialogManager;
import informatis.ui.fragments.sidebar.windows.*;
import arc.*;
import informatis.ui.fragments.sidebar.windows.tools.tools.ToolManager;
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
            OverDrawManager.init();
            ToolManager.init();
            OverDrawer.init();
            SVars.init();
        });
    }
}
