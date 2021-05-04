package UnitInfo.core;

import arc.Events;
import mindustry.game.EventType.*;
import mindustry.mod.Mod;

public class Main extends Mod {
    public Main(){
        Events.on(ClientLoadEvent.class, e -> {
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
