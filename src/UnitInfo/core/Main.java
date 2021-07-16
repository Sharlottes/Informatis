package UnitInfo.core;

import UnitInfo.ui.FreeBar;
import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.Scl;
import arc.util.Align;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.ui.Fonts;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Main extends Mod {
    public static Setting settingAdder = new Setting();
    public static HudUi hud = new HudUi();

    @Override
    public void init(){
        Events.on(ClientLoadEvent.class, e -> {
            hud = new HudUi();
            settingAdder.init();
            hud.addCoreTable();
            hud.addWaveTable();
            hud.addUnitTable();
            hud.addTileTable();
            hud.addTable();
            hud.setEvent();
        });

        Events.on(WorldLoadEvent.class, e -> {
            hud = new HudUi();
            hud.addWaveTable();
            hud.addTileTable();
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
                float range = settings.getInt("rangemax") * 8f;

                for(Team team : Team.all)
                    indexer.eachBlock(team, Core.input.mouseWorldX(), Core.input.mouseWorldY(), range, b -> true, b -> new FreeBar().draw(b));

                Draw.color(Tmp.c1.set(Pal.accent).a(0.75f + Mathf.absin(3, 0.25f)));
                Lines.swirl(Core.input.mouseWorldX(), Core.input.mouseWorldY(), range, 0.15f, Time.time % 360);
                Lines.swirl(Core.input.mouseWorldX(), Core.input.mouseWorldY(), range, 0.15f, 90 + Time.time % 360);
                Lines.swirl(Core.input.mouseWorldX(), Core.input.mouseWorldY(), range, 0.15f, 180 + Time.time % 360);
                Lines.swirl(Core.input.mouseWorldX(), Core.input.mouseWorldY(), range, 0.15f, 270 + Time.time % 360);

                Draw.reset();
            }
            if(!mobile && !Vars.state.isPaused() && settings.getBool("gaycursor"))
                Fx.mine.at(Core.input.mouseWorldX(), Core.input.mouseWorldY(), Tmp.c2.set(Color.red).shiftHue(Time.time * 1.5f));

            Groups.unit.each(unit -> {
                Draw.color();
                Tmp.c1.set(Color.white).lerp(Pal.heal, Mathf.clamp(unit.healTime - unit.hitTime));
                Draw.mixcol(Tmp.c1, Math.max(unit.hitTime, Mathf.clamp(unit.healTime)));
                if(unit.drownTime > 0 && unit.floorOn().isDeep())
                    Draw.mixcol(unit.floorOn().mapColor, unit.drownTime * 0.8f);

                //draw back items
                if(unit.item() != null && unit.itemTime > 0.01f){
                    float size = (itemSize + Mathf.absin(Time.time, 5f, 1f)) * unit.itemTime;

                    Draw.mixcol(Pal.accent, Mathf.absin(Time.time, 5f, 0.1f));
                    Draw.rect(unit.item().fullIcon,
                        unit.x + Angles.trnsx(unit.rotation + 180f, unit.type.itemOffsetY),
                        unit.y + Angles.trnsy(unit.rotation + 180f, unit.type.itemOffsetY),
                        size, size, unit.rotation);
                    Draw.mixcol();

                    Lines.stroke(1f, Pal.accent);
                    Lines.circle(
                        unit.x + Angles.trnsx(unit.rotation + 180f, unit.type.itemOffsetY),
                        unit.y + Angles.trnsy(unit.rotation + 180f, unit.type.itemOffsetY),
                        (3f + Mathf.absin(Time.time, 5f, 1f)) * unit.itemTime);

                    if(!renderer.pixelator.enabled()){
                        Fonts.outline.draw(unit.stack.amount + "",
                            unit.x + Angles.trnsx(unit.rotation + 180f, unit.type.itemOffsetY),
                            unit.y + Angles.trnsy(unit.rotation + 180f, unit.type.itemOffsetY) - 3,
                            Pal.accent, 0.25f * unit.itemTime / Scl.scl(1f), false, Align.center);
                    }

                    Draw.reset();
                }
            });
        });
    }

    @Override
    public void loadContent(){

    }


}
