package UnitInfo.core;

import UnitInfo.ui.FreeBar;
import arc.Core;
import arc.Events;
import mindustry.game.EventType.*;
import mindustry.gen.Groups;
import mindustry.mod.Mod;

public class Main extends Mod {
    public static Setting settingAdder = new Setting();
    public static HudUi hud = new HudUi();

    @Override
    public void init(){
        Events.on(ClientLoadEvent.class, e -> {
            hud.reset();

            hud = new HudUi();
            settingAdder.init();
            hud.addTable();
            hud.addCoreTable();
        });

        Events.on(ResetEvent.class, e -> {
            hud.reset();

            hud = new HudUi();
            hud.addWaveTable();
        });

        Events.run(Trigger.draw, () -> {
            if(Core.settings.getBool("unithealthui"))
                Groups.unit.each(unit -> new FreeBar().draw(unit));
        });
        /*
            unitFade[0] = Mathf.lerpDelta(unitFade[0], Mathf.num( Vars.player.unit() != null), 0.1f);
            for(int i = 0; i < 4; i++){
                float rot = i * 90f + 45f + (-Time.time) % 360f;
                float length = Vars.player.unit().hitSize() * 1.5f + (unitFade[0] * 2.5f);
                Draw.color(Tmp.c1.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f)));
                Draw.rect("select-arrow", Vars.player.unit().x + Angles.trnsx(rot, length), Vars.player.unit().y + Angles.trnsy(rot, length), length / 1.9f, length / 1.9f, rot - 135f);
                Draw.reset();
            }
        */
    }

    @Override
    public void loadContent(){

    }


}
