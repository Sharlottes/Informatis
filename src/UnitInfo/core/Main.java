package UnitInfo.core;

import UnitInfo.ui.FreeBar;
import arc.Core;
import arc.Events;
import arc.math.geom.Rect;
import mindustry.game.EventType.*;
import mindustry.gen.Groups;
import mindustry.mod.Mod;

public class Main extends Mod {
    public static Setting settingAdder = new Setting();

    public Main(){
        Events.on(ClientLoadEvent.class, e -> {
            settingAdder.init();
            HudUi hud = new HudUi();
            hud.addTable();
        });

        Events.on(WorldLoadEvent.class, e -> {
            HudUi hud = new HudUi();
            hud.addTable();
        });
        Events.run(Trigger.draw, () -> {
            if(Core.settings.getBool("unithealthui"))
                Groups.unit.each(unit -> new FreeBar().draw(unit));
        });
    }

    @Override
    public void init(){
    }

    @Override
    public void loadContent(){

    }


}
