package UnitInfo.core;

import UnitInfo.ui.FreeBar;
import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;

import static arc.Core.settings;
import static mindustry.Vars.indexer;

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

            if(Core.settings.getBool("scan")){
                float range = settings.getInt("wavemax");

                for(Team team : Team.all) {
                    indexer.eachBlock(team, Core.input.mouseWorldX(), Core.input.mouseWorldY(), range, b -> true, b -> new FreeBar().draw(b));
                }
                for(int i : Mathf.signs) {
                    Draw.color(Tmp.c1.set(Pal.accent).lerp(Pal.surge, Mathf.absin(4 + i, 1f)).a(0.5f + Mathf.absin(3 + i, 0.5f)));
                    Lines.swirl(Core.input.mouseWorldX(), Core.input.mouseWorldY(), range, 0.1f, i * Time.time % 360);
                    Lines.swirl(Core.input.mouseWorldX(), Core.input.mouseWorldY(), range, 0.1f, 90 + i * Time.time % 360);
                    Lines.swirl(Core.input.mouseWorldX(), Core.input.mouseWorldY(), range, 0.1f, 180 + i * Time.time % 360);
                    Lines.swirl(Core.input.mouseWorldX(), Core.input.mouseWorldY(), range, 0.1f, 270 + i * Time.time % 360);
                }
                Draw.reset();
            }
        });
    }

    @Override
    public void loadContent(){

    }


}
