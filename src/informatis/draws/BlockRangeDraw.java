package informatis.draws;

import arc.graphics.g2d.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.*;

import static arc.Core.settings;
import static informatis.SUtils.isInCamera;
import static mindustry.Vars.player;

public class BlockRangeDraw extends OverDraw {
    public BlockRangeDraw() {
        super("blockRange", OverDrawCategory.Range);
    }

    @Override
    public void onBuilding(Building build) {
            Unit playerUnit = player.unit();

            boolean includeInvalid = settings.getBool("invalidRange", true),
                includeAir = settings.getBool("airRange", true), isAir = playerUnit == null || playerUnit.isFlying(),
                includeGround = settings.getBool("groundRange", true), isGround = playerUnit == null || !playerUnit.isFlying(),
                includeAlice = settings.getBool("aliceRange", true),
                shader = settings.getBool("RangeShader", true);

            if(!((includeAlice || player.team() != build.team)
                    && build instanceof BaseTurret.BaseTurretBuild turret && isInCamera(build.x, build.y, turret.range() * 2))) return;

            boolean valid = false;
            if (build instanceof Turret.TurretBuild turretBuild) {
                Turret block = (Turret) turretBuild.block;
                if ((build.team == player.team()
                        || (block.targetAir && isAir && includeAir)
                        || (block.targetGround && isGround && includeGround))
                        && turretBuild.hasAmmo() && turretBuild.canConsume()) valid = true;
            } else if (build instanceof TractorBeamTurret.TractorBeamBuild tractorBeamBuild) {
                TractorBeamTurret block = (TractorBeamTurret) tractorBeamBuild.block;
                if ((build.team == player.team()
                        || (block.targetAir && isAir && includeAir)
                        || (block.targetGround && isGround && includeGround))
                        && tractorBeamBuild.canConsume()) valid = true;
            }

            if(!includeInvalid && !valid) return;

            //non-base teams are considered as crux
            int index = valid ? build.team.id > 5 ? 2 : build.team.id : 0;
            float range = turret.range();
            Draw.color(Team.baseTeams[index].color);
            if (shader) {
                Draw.z(166+(Team.baseTeams.length-index)*3);
                Fill.poly(build.x, build.y, Lines.circleVertices(range), range);
            } else Drawf.dashCircle(build.x, build.y, range, Team.baseTeams[index].color);
    }
}
