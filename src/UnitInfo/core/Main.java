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
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;
import mindustry.world.Block;
import mindustry.world.blocks.ConstructBlock;
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

            if(settings.getBool("rangeNearby") && player != null) {
                Groups.all.each(entityc -> entityc instanceof Teamc && (entityc instanceof Building || (settings.getBool("unitRange") && entityc instanceof Unit)), entityc -> { //only turret and unit are allowed
                    boolean h = false;
                    float range = 0;
                    if(entityc instanceof Turret.TurretBuild) { //if it's turret and can target player and
                        Turret turret = (Turret) ((Turret.TurretBuild) entityc).block;
                        if((entityc instanceof PowerTurret.PowerTurretBuild)) { //can shoot to player or
                            if(((PowerTurret.PowerTurretBuild) entityc).power().graph.getLastScaledPowerIn() > 0f) {
                                if(player.unit().isGrounded() && turret.targetGround) h = true;
                                if(player.unit().isFlying() && turret.targetAir) h = true;
                            }
                        }else if(((Turret.TurretBuild) entityc).hasAmmo()) {
                            if(player.unit().isGrounded() && turret.targetGround) h = true;
                            if(player.unit().isFlying() && turret.targetAir) h = true;
                        }
                        range = turret.range;
                    }
                    if(entityc instanceof TractorBeamTurret.TractorBeamBuild) { //if it's parallax and can target player or
                        TractorBeamTurret turret = (TractorBeamTurret) ((TractorBeamTurret.TractorBeamBuild) entityc).block;
                        if(((TractorBeamTurret.TractorBeamBuild) entityc).power().graph.getLastScaledPowerIn() > 0f) {
                            if(player.unit().isGrounded() && turret.targetGround) h = true;
                            if(player.unit().isFlying() && turret.targetAir) h = true;
                        }
                        range = turret.range;
                    }
                    if(entityc instanceof ConstructBlock.ConstructBuild) { //if it's not constructed yet but can target player later or
                        if (((ConstructBlock.ConstructBuild) entityc).current instanceof Turret) {
                            Turret turret = (Turret) ((ConstructBlock.ConstructBuild) entityc).current;
                            if (player.unit().isGrounded() && turret.targetGround) h = true;
                            if (player.unit().isFlying() && turret.targetAir) h = true;
                            range = turret.range;
                        } else if (((ConstructBlock.ConstructBuild) entityc).current instanceof TractorBeamTurret) {
                            TractorBeamTurret turret = (TractorBeamTurret) ((ConstructBlock.ConstructBuild) entityc).current;
                            if (player.unit().isGrounded() && turret.targetGround) h = true;
                            if (player.unit().isFlying() && turret.targetAir) h = true;
                            range = turret.range;
                        }
                    }
                    if(entityc instanceof Unit) {  //if it's just unit and can target player,
                        UnitType type = ((Unit) entityc).type;
                        if (player.unit().isGrounded() && type.targetGround) h = true;
                        if (player.unit().isFlying() && type.targetAir) h = true;
                        range = ((Unit) entityc).range();
                    }

                    //draw range.
                    if(Vars.player.dst((Position) entityc) <= range + settings.getInt("rangeRadius") * tilesize && //out of range is not allowed
                        (settings.getBool("allTeamRange") || //derelict or player team are not allowed without setting
                            (((Teamc) entityc).team() != player.team() && ((Teamc) entityc).team() != Team.derelict))){
                    if (h) {
                        if(entityc instanceof ConstructBlock.ConstructBuild){
                            Lines.stroke(3f, Tmp.c1.set(Pal.gray).a(((ConstructBlock.ConstructBuild) entityc).progress));
                            Lines.dashCircle(((Teamc) entityc).x(), ((Teamc) entityc).y(), range);
                            Lines.stroke(1f, Tmp.c1.set(((Teamc) entityc).team().color).a(((ConstructBlock.ConstructBuild) entityc).progress));
                            Lines.dashCircle(((Teamc) entityc).x(), ((Teamc) entityc).y(), range);
                            Draw.reset();
                        }
                        else Drawf.dashCircle(((Teamc) entityc).x(), ((Teamc) entityc).y(), range, ((Teamc) entityc).team().color);
                    }else if (settings.getBool("allTargetRange"))
                        Drawf.dashCircle(((Teamc) entityc).x(), ((Teamc) entityc).y(), range, Color.gray);
                    }
                });
            }
        });
    }

    @Override
    public void loadContent(){

    }
}
