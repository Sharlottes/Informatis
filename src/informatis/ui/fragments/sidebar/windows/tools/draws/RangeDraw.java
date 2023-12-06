package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.graphics.g2d.*;
import informatis.ui.fragments.sidebar.windows.ToolConfigable;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.Ranged;
import mindustry.world.blocks.defense.turrets.*;

import static informatis.SUtils.isInCamera;
import static mindustry.Vars.player;

public class RangeDraw extends OverDraw {
    private final ToolConfigable blockRange, unitRange, airRange, groundRange, allianceRange, invalidRange, rangeShader;
    public RangeDraw() {
        super("range", "blockRange", "unitRange", "airRange", "groundRange", "allianceRange", "invalidRange", "rangeShader");
        ToolConfigable[] subConfigs = getSubConfigs();
        blockRange = subConfigs[0];
        unitRange = subConfigs[1];
        airRange = subConfigs[2];
        groundRange = subConfigs[3];
        allianceRange = subConfigs[4];
        invalidRange = subConfigs[5];
        rangeShader = subConfigs[6];
    }
    private final static RangeData rangeData = new RangeData();

    private boolean validate(Ranged target) {
        if(!isInCamera(target.x(), target.y(), target.range() * 2)) return false;

        if(!allianceRange.isEnabled() && player.team() == target.team()) return false;

        rangeData.init(target);
        if(groundRange.isEnabled() && rangeData.canDetectGround()) return true;
        return airRange.isEnabled() && rangeData.canDetectAir();
    }

    @Override
    public void onBuilding(Building build) {
        if(!blockRange.isEnabled()) return;
        if(!(build instanceof BaseTurret.BaseTurretBuild turret)) return;
        if(!validate(turret)) return;
        drawRange(turret);
    }

    @Override
    public void onUnit(Unit unit) {
        if(!unitRange.isEnabled()) return;
        if(!validate(unit)) return;
        drawRange(unit);
    }

    private void drawRange(Ranged target) {
        boolean canShoot = rangeData.canShoot();
        if(!canShoot && !invalidRange.isEnabled()) return;
        int index = canShoot ? target.team().id > 5 ? 2 : target.team().id : 0;
        float range = target.range();
        Draw.color(Team.baseTeams[index].color.cpy().shiftSaturation(target instanceof Unit ? 0.25f : 0));
        if (rangeShader.isEnabled()) {
            Draw.z(OverDrawManager.zIndexTeamCache[index]);
            Fill.poly(target.x(), target.y(), Lines.circleVertices(range), range);
        } else {
            Drawf.dashCircle(target.x(), target.y(), range, Team.baseTeams[index].color);
        }
    }
}

class RangeData {
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

    public void init(Entityc entity) {
        Unit playerUnit = player.unit();

        boolean isAir = playerUnit == null || playerUnit.isFlying(),
                isGround = playerUnit == null || !playerUnit.isFlying();

        if (entity instanceof Unit unit) {
            canDetectAir = unit.type.targetAir;
            canDetectGround = unit.type.targetGround;
            canShoot = unit.canShoot() && ((canDetectGround && isGround) || (canDetectAir && isAir));
        }
        else if (entity instanceof BaseTurret.BaseTurretBuild build) {
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
}