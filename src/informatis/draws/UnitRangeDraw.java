package informatis.draws;

import arc.graphics.g2d.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import static arc.Core.settings;
import static informatis.SUtils.isInCamera;
import static mindustry.Vars.player;

public class UnitRangeDraw extends OverDraw {
    public UnitRangeDraw() {
        super("unitRange", "airRange", "groundRange", "aliceRange", "invalidRange", "RangeShader");
    }

    @Override
    public void onUnit(Unit unit) {
        Unit playerUnit = player.unit();

        boolean includeInvalid = settings.getBool("invalidRange", true),
                includeAir = settings.getBool("airRange", true), isAir = playerUnit == null || playerUnit.isFlying(),
                includeGround = settings.getBool("groundRange", true), isGround = playerUnit == null || !playerUnit.isFlying(),
                includeAlice = settings.getBool("aliceRange", true),
                shader = settings.getBool("RangeShader", true);
        if(!((includeAlice || player.team() != unit.team) && isInCamera(unit.x, unit.y, unit.range() * 2))) return;

        boolean valid = false;
        if (player.unit() == null) valid = true;
        else if ((unit.team == player.team()
                || (unit.type.targetAir && isAir && includeAir)
                || (unit.type.targetGround && isGround && includeGround))
                && unit.canShoot()) valid = true;

        if(!includeInvalid && !valid) return;

        //non-base teams are considered as crux
        int index = valid ? unit.team.id > 5 ? 2 : unit.team.id : 0;
        float range = unit.range();
        Draw.color(Team.baseTeams[index].color.cpy().shiftSaturation(0.25f));
        if (shader) {
            Draw.z(OverDraws.zIndexTeamCache[index]);
            Fill.poly(unit.x, unit.y, Lines.circleVertices(range), range);
        } else Drawf.dashCircle(unit.x, unit.y, range, Team.baseTeams[index].color);
    }
}
