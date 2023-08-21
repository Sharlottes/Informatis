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
    private final static BlockRangeData blockRangeData = new BlockRangeData();

    private boolean validate(Building build) {
        if(!(build instanceof BaseTurret.BaseTurretBuild turret)) return false;
        if(!isInCamera(build.x, build.y, turret.range() * 2)) return false;

        boolean
                includeAir = settings.getBool("airRange", true),
                includeGround = settings.getBool("groundRange", true),
                includeAlice = settings.getBool("aliceRange", true);

        if(includeAlice && player.team() != build.team) return false;

        blockRangeData.init(turret);
        if(includeGround && !blockRangeData.canDetectGround()) return false;
        if(includeAir && !blockRangeData.canDetectAir()) return false;

        return true;
    }

    @Override
    public void onBuilding(Building build) {
        if(!validate(build)) return;

        boolean includeInvalid = settings.getBool("invalidRange", true);
        boolean canShoot = blockRangeData.canShoot();
        if(includeInvalid && !canShoot) return;
        boolean shader = settings.getBool("RangeShader", true);

        int index = canShoot ? build.team.id > 5 ? 2 : build.team.id : 0;
        float range = ((BaseTurret.BaseTurretBuild) build).range();
        Draw.color(Team.baseTeams[index].color);
        if (shader) {
            Draw.z(166 + (Team.baseTeams.length - index) * 3);
            Fill.poly(build.x, build.y, Lines.circleVertices(range), range);
        } else {
            Drawf.dashCircle(build.x, build.y, range, Team.baseTeams[index].color);
        }
    }
}

class BlockRangeData {
    private boolean canDetectGround; // 지상유닛 감지 여부
    private boolean canDetectAir; // 지상유닛 감지 여부
    private boolean canShoot; // 실제로 발사가 가능한지

    public boolean canDetectGround() {
        return canDetectGround;
    }

    public boolean canDetectAir() {
        return canDetectAir;
    }

    public boolean canShoot() {
        return canShoot;
    }

    public void init(BaseTurret.BaseTurretBuild build) {
        Unit playerUnit = player.unit();

        boolean isAir = playerUnit == null || playerUnit.isFlying(),
                isGround = playerUnit == null || !playerUnit.isFlying();

        if (build instanceof Turret.TurretBuild turretBuild) {
            Turret block = (Turret) turretBuild.block;
            canDetectAir = block.targetAir;
            canDetectGround = block.targetGround;
            canShoot = turretBuild.hasAmmo() && ((canDetectGround && isGround) || (canDetectAir && isAir));
        } else if (build instanceof TractorBeamTurret.TractorBeamBuild tractorBeamBuild) {
            TractorBeamTurret block = (TractorBeamTurret) tractorBeamBuild.block;
            canDetectAir = block.targetAir;
            canDetectGround = block.targetGround;
            canShoot = tractorBeamBuild.canConsume() && ((canDetectGround && isGround) || (canDetectAir && isAir));
        }
    }
}