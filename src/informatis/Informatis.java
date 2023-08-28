package informatis;

import informatis.ui.fragments.sidebar.windows.tools.draws.OverDrawManager;
import informatis.ui.fragments.sidebar.windows.tools.tools.ToolManager;
import informatis.ui.fragments.sidebar.dialogs.DialogManager;
import informatis.ui.fragments.sidebar.windows.*;
import informatis.ui.fragments.FragmentManager;
import informatis.core.ModMetadata;
import informatis.core.OverDrawer;
import informatis.core.Setting;

import mindustry.game.EventType.*;
import mindustry.mod.*;
import arc.*;

public class Informatis extends Mod {
    @Override
    public void init(){
        Events.on(ClientLoadEvent.class, e -> {
            ModMetadata.init();
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
