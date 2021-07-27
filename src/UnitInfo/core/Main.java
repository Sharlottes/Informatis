package UnitInfo.core;

import UnitInfo.ui.*;
import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.mod.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;

import static UnitInfo.SVars.*;
import static arc.Core.*;
import static mindustry.Vars.*;

public class Main extends Mod {
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
            playerinfo.createFile();
            playerinfo.setEvent();
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
                for(int i = 0; i < 4; i++)
                    Lines.swirl(Core.input.mouseWorldX(), Core.input.mouseWorldY(), range, 0.15f, 90 * i + Time.time % 360);
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

            // Turret Ranges
            if(settings.getBool("rangeNearby") && player != null) {
                Team team = player.team();
                Unit unit = player.unit();
                Groups.build.each(e -> {
                    if (e.team == team) return; // Don't draw own turrets
                    if (!(e instanceof BaseTurret.BaseTurretBuild )) return; // Not a turret
                    if ((e instanceof Turret.TurretBuild t && !t.hasAmmo()) || !e.cons.valid()) return; // No ammo

                    boolean canHit = e.block instanceof Turret t ? unit.isFlying() ? t.targetAir : t.targetGround :
                        e.block instanceof TractorBeamTurret tu && (unit.isFlying() ? tu.targetAir : tu.targetGround);
                    float range = ((BaseTurret.BaseTurretBuild) e).range();


                    if(Vars.player.dst(e) <= range + settings.getInt("rangeRadius") * tilesize + e.block.offset) {
                        if (canHit || settings.getBool("allTargetRange"))
                            Drawf.dashCircle(e.x, e.y, range, canHit ? e.team.color : Team.derelict.color);
                    }
                });

                // Unit Ranges (Only works when turret ranges are enabled)
                if (!settings.getBool("unitRange")) {
                    Groups.unit.each(u -> {
                        if (u.team == team) return; // Don't draw own units

                        boolean canHit = unit.isFlying() ? u.type.targetAir : u.type.targetGround;
                        float range = u.range();

                        if(Vars.player.dst(u) <= range + settings.getInt("rangeRadius") * tilesize) { // TODO: Store value of rangeRadius as an int, should increase performance
                            if (canHit || settings.getBool("allTargetRange")) // Same as above
                                Drawf.dashCircle(u.x, u.y, range, canHit ? u.team.color : Team.derelict.color);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void loadContent(){

    }
}
