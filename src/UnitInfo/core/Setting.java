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

        addGraphicSetting("coreui");
        addGraphicSetting("unitui");
        addGraphicSetting("weaponui");
        addGraphicSetting("commandedunitui");
        addGraphicSetting("unithealthui");
        Vars.ui.settings.graphics.sliderPref("coreuiopacity", 25, 0, 100, 5, s -> s + "%");
        Vars.ui.settings.graphics.sliderPref("uiopacity", 50, 0, 100, 5, s -> s + "%");
        Vars.ui.settings.graphics.sliderPref("baropacity", 50, 0, 100, 5, s -> s + "%");

        Core.settings.defaults("coreui", true);
        Core.settings.defaults("unitui", true);
        Core.settings.defaults("weaponui", true);
        Core.settings.defaults("commandedunitui", true);
        Core.settings.defaults("unithealthui", true);


        Core.settings.put("uiscalechanged", tmp);
    }
}
