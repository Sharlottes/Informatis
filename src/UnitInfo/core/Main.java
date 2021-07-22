package UnitInfo.core;

import UnitInfo.ui.FreeBar;
import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.scene.ui.layout.Scl;
import arc.util.Align;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.mod.Mod;
import mindustry.ui.Fonts;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.BaseTurret;

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
            if(settings.getBool("blockstatus")) Groups.build.each(build -> {
                if(Vars.player != null && Vars.player.team() == build.team) return;

                Block block = build.block;
                if(block.enableDrawStatus && block.consumes.any()){
                    float multiplier = block.size > 1 ? 1 : 0.64f;
                    float brcx = build.x + (block.size * tilesize / 2f) - (tilesize * multiplier / 2f);
                    float brcy = build.y - (block.size * tilesize / 2f) + (tilesize * multiplier / 2f);

                    Draw.z(Layer.power + 1);
                    Draw.color(Pal.gray);
                    Fill.square(brcx, brcy, 2.5f * multiplier, 45);
                    Draw.color(build.status().color);
                    Fill.square(brcx, brcy, 1.5f * multiplier, 45);
                    Draw.color();
                }
            });
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

            if(!renderer.pixelator.enabled()) Groups.unit.each(unit -> unit.item() != null && unit.itemTime > 0.01f, unit -> {
                Fonts.outline.draw(unit.stack.amount + "",
                    unit.x + Angles.trnsx(unit.rotation + 180f, unit.type.itemOffsetY),
                    unit.y + Angles.trnsy(unit.rotation + 180f, unit.type.itemOffsetY) - 3,
                    Pal.accent, 0.25f * unit.itemTime / Scl.scl(1f), false, Align.center);
                Draw.reset();
            });

            if(settings.getBool("rangeNearby")) Groups.all.each(entityc -> entityc instanceof BaseTurret.BaseTurretBuild ||
            (!(entityc instanceof Building) && entityc instanceof Ranged && player != null && player.team() != ((Ranged) entityc).team()), entityc -> {
                float range = ((Ranged) entityc).range();
                float margin = settings.getInt("rangeRadius") * tilesize;
                if(Vars.player.dst((Position) entityc) <= range + margin)
                    Drawf.dashCircle(((Ranged) entityc).x(), ((Ranged) entityc).y(), range, ((Ranged) entityc).team().color);
            });
        });
    }

    @Override
    public void loadContent(){

    }


}
