package informatis.ui.draws;

import informatis.ui.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.ai.*;
import mindustry.ai.types.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;

import static informatis.SUtils.*;
import static arc.Core.settings;
import static mindustry.Vars.*;

public class UnitDraw extends OverDraw {
    Seq<Tile> pathTiles = new Seq<>();
    int otherCores;

    UnitDraw(String name, TextureRegionDrawable icon) {
        super(name, icon);
        registerOption("pathLine");
        registerOption("logicLine");
        registerOption("unitLine");
        registerOption("unitItem");
        registerOption("unitBar");
    }

    @Override
    public void draw() {
        if(!enabled) return;

        Groups.unit.each(u -> {
            UnitController c = u.controller();

            if(settings.getBool("logicLine") && c instanceof LogicAI ai && (ai.control == LUnitControl.approach || ai.control == LUnitControl.move)) {
                Lines.stroke(1, u.team.color);
                Lines.line(u.x(), u.y(), ai.moveX, ai.moveY);
                Lines.stroke(0.5f + Mathf.absin(6f, 0.5f), Tmp.c1.set(Pal.logicOperations).lerp(Pal.sap, Mathf.absin(6f, 0.5f)));
                Lines.line(u.x(), u.y(), ai.controller.x, ai.controller.y);
            }

            if(c instanceof CommandAI com && com.hasCommand()) {
                Lines.stroke(1, u.team.color);

                Lines.line(u.x(), u.y(), com.targetPos.x, com.targetPos.y);
            }

            if(settings.getBool("unitLine") && u.team == state.rules.waveTeam && !u.type.flying && !(c instanceof MinerAI || c instanceof BuilderAI || c instanceof RepairAI || c instanceof DefenderAI || c instanceof FlyingAI)) {
                Lines.stroke(1, u.team.color);

                otherCores = Groups.build.count(b -> b instanceof CoreBlock.CoreBuild && b.team != u.team);
                pathTiles.clear();
                getNextTile(u.tileOn(), u.controller() instanceof SuicideAI ? 0 : u.pathType(), u.team, u.pathType());
                for(int i = 0; i < pathTiles.size-1; i++) {
                    Tile from = pathTiles.get(i);
                    Tile to = pathTiles.get(i + 1);
                    if(from == null || to == null) continue;
                    Lines.line(from.worldx(), from.worldy(), to.worldx(), to.worldy());
                }
            }

            if(isInCamera(u.x, u.y, u.hitSize)) {
                if (settings.getBool("unitBar")) FreeBar.draw(u);
                if (settings.getBool("unitItem") && !renderer.pixelator.enabled() && u.item() != null && u.itemTime > 0.01f)
                    Fonts.outline.draw(String.valueOf(u.stack.amount),
                            u.x + Angles.trnsx(u.rotation + 180f, u.type.itemOffsetY),
                            u.y + Angles.trnsy(u.rotation + 180f, u.type.itemOffsetY) - 3,
                            Pal.accent, 0.25f * u.itemTime / Scl.scl(1f), false, Align.center);
            }
        });

        if(settings.getBool("pathLine")) spawner.getSpawns().each(t -> {
            Team enemyTeam = state.rules.waveTeam;
            Lines.stroke(1, enemyTeam.color);
            for(int p = 0; p < (Vars.state.rules.spawns.count(g->g.type.naval)>0?3:2); p++) {
                pathTiles.clear();
                otherCores = Groups.build.count(b -> b instanceof CoreBlock.CoreBuild && b.team != enemyTeam);
                getNextTile(t, p, enemyTeam, Pathfinder.fieldCore);
                for(int i = 0; i < pathTiles.size-1; i++) {
                    Tile from = pathTiles.get(i);
                    Tile to = pathTiles.get(i + 1);
                    if(from == null || to == null) continue;
                    Lines.line(from.worldx(), from.worldy(), to.worldx(), to.worldy());
                }
        }
        });
    }

    void getNextTile(Tile tile, int cost, Team team, int finder) {
        Pathfinder.Flowfield field = pathfinder.getField(team, cost, Mathf.clamp(finder, 0, 0));
        Tile tile1 = pathfinder.getTargetTile(tile, field);
        pathTiles.add(tile1);
        if(tile1 == tile || tile1 == null ||
                (finder == 0 && (otherCores != Groups.build.count(b -> b instanceof CoreBlock.CoreBuild && b.team != team) || tile1.build instanceof CoreBlock.CoreBuild))) return;
        getNextTile(tile1, cost, team, finder);
    }
}
