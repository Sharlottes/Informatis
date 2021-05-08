package UnitInfo.core;

import arc.Core;
import mindustry.Vars;

public class Setting {
    public void addGraphicSetting(String key){
        Vars.ui.settings.graphics.checkPref(key, Core.settings.getBool(key));
    }

    public void init(){
        boolean tmp = Core.settings.getBool("uiscalechanged", false);
        Core.settings.put("uiscalechanged", false);

        addGraphicSetting("weaponui");
        addGraphicSetting("commandedunitui");
        Vars.ui.settings.graphics.sliderPref("uiopacity", 50, 0, 100, 5, s -> s + "%");

        Core.settings.defaults("weaponui", true);
        Core.settings.defaults("commandedunitui", true);


        Core.settings.put("uiscalechanged", tmp);
    }
}
