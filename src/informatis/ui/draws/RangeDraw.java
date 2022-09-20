package informatis.ui.draws;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.graphics.gl.FrameBuffer;
import arc.scene.style.TextureRegionDrawable;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.type.Weapon;
import mindustry.world.blocks.defense.turrets.*;

import static informatis.SVars.turretRange;
import static informatis.SUtils.*;
import static arc.Core.*;
import static mindustry.Vars.player;

public class RangeDraw extends OverDraw {
    FrameBuffer effectBuffer = new FrameBuffer();

    RangeDraw(String name, TextureRegionDrawable icon) {
        super(name, icon);
        registerOption("blockRange");
        registerOption("unitRange");
        registerOption("aliceRange");
        registerOption("airRange");
        registerOption("groundRange");
    }

    @Override
    public void draw() {
        if(!enabled) return;

        Unit target = player.unit();

        effectBuffer.resize(graphics.getWidth(), graphics.getHeight());
        for(Team team : Team.baseTeams) {
            Draw.drawRange(166 + (Team.baseTeams.length-team.id) * 3, 1, () -> effectBuffer.begin(Color.clear), () -> {
                effectBuffer.end();
                effectBuffer.blit(turretRange);
            });
        }

        boolean includeBlock = settings.getBool("blockRange"),
                includeUnit = settings.getBool("unitRange"),
                includeAir = settings.getBool("airRange"),
                includeGround = settings.getBool("groundRange"),
                includeAlice = settings.getBool("aliceRange"),
                shader = settings.getBool("RangeShader");

        Teamc selected = getTarget();
        if(selected instanceof BaseTurret.BaseTurretBuild turretBuild) {
            Drawf.dashCircle(turretBuild.x, turretBuild.y, turretBuild.range(), turretBuild.team.color);
        } else if(selected instanceof Unit unit) {
            Drawf.dashCircle(unit.x, unit.y, unit.range(), unit.team.color);
        }

        if(includeBlock) {
            for(Building building : Groups.build) {
                if(!((includeAlice || player.team() != building.team)
                    && building instanceof BaseTurret.BaseTurretBuild turret && isInCamera(building.x, building.y, turret.range()))) continue;

                boolean valid = false;
                if (target == null) valid = true;
                else if (building instanceof Turret.TurretBuild turretBuild) {
                    Turret block = (Turret) turretBuild.block;
                    if(block.targetAir && !includeAir || block.targetGround && !includeGround) continue;
                    if((target.isFlying() ? block.targetAir : block.targetGround) && turretBuild.hasAmmo() && turretBuild.canConsume()) valid = true;
                } else if (building instanceof TractorBeamTurret.TractorBeamBuild tractorBeamBuild) {
                    TractorBeamTurret block = (TractorBeamTurret) tractorBeamBuild.block;
                    if(block.targetAir && !includeAir || block.targetGround && !includeGround) continue;
                    if((target.isFlying() ? block.targetAir : block.targetGround) && tractorBeamBuild.canConsume()) valid = true;
                }

                //non-base teams are considered as crux
                int index = valid ? building.team.id > 5 ? 2 : building.team.id : 0;
                float range = turret.range();
                Draw.color(Team.baseTeams[index].color);
                if (shader) {
                    Draw.z(166+(Team.baseTeams.length-index)*3);
                    Fill.poly(building.x, building.y, Lines.circleVertices(range), range);
                } else Drawf.dashCircle(building.x, building.y, range, Team.baseTeams[index].color);
            }
        }

        if(includeUnit) {
            for(Unit unit : Groups.unit) {
                if(!((includeAlice || player.team() != unit.team) && isInCamera(unit.x, unit.y, unit.range()))) continue;

                boolean valid = false;
                if (target == null) valid = true;
                else if (!(unit.type.targetAir && !includeAir || unit.type.targetGround && !includeGround)
                    && (target.isFlying() ?unit.type.targetAir : unit.type.targetGround)
                    && unit.canShoot()
                ) valid = true;

                //non-base teams are considered as crux
                int index = valid ? unit.team.id > 5 ? 2 : unit.team.id : 0;
                float range = unit.range();
                Draw.color(Team.baseTeams[index].color);
                if (shader) {
                    Draw.z(166 + (Team.baseTeams.length - index) * 3);
                    Fill.poly(unit.x, unit.y, Lines.circleVertices(range), range);
                } else Drawf.dashCircle(unit.x, unit.y, range, Team.baseTeams[index].color);
            }
        }
    }
}
