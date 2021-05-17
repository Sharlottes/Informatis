package UnitInfo.core;

import UnitInfo.ui.FreeBar;
import arc.Core;
import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.gen.Groups;
import mindustry.mod.Mod;

public class Main extends Mod {
    public static Setting settingAdder = new Setting();
    public static HudUi hud = new HudUi();

    @Override
    public void init(){
        Events.on(ClientLoadEvent.class, e -> {
            hud = new HudUi();
            settingAdder.init();
            hud.addTable();
            hud.addCoreTable();
        });

        Events.on(WorldLoadEvent.class, e -> {
            hud = new HudUi();
            hud.addWaveTable();
        });

        Events.on(WaveEvent.class, e -> {
            Vars.ui.hudGroup.removeChild(hud.waveTable);
            hud = new HudUi();
            hud.addWaveTable();
        });

        Events.run(Trigger.draw, () -> {
            if(Core.settings.getBool("unithealthui"))
                Groups.unit.each(unit -> new FreeBar().draw(unit));

        });

    }

    @Override
    public void loadContent(){

    }


}
