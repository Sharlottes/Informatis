package UnitInfo.core;

import arc.Events;
import mindustry.game.EventType.*;
import mindustry.mod.Mod;

public class Main extends Mod {
    public static Setting settingAdder = new Setting();
    public Main(){
        Events.on(ClientLoadEvent.class, e -> {
            settingAdder.init();
            new HudUi().addTable();
        });

        Events.on(WorldLoadEvent.class, e -> {
            new HudUi().addTable();
        });
    }

    @Override
    public void init(){
    }

    @Override
    public void loadContent(){

    }


}
