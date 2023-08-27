package informatis.ui.fragments.sidebar.windows.tools.draws;

import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.*;

import static informatis.SUtils.getTarget;

public class PlayerRangeDraw extends OverDraw {
    public PlayerRangeDraw() {
        super("playerRange");
    }

    @Override
    public void draw() {
        Teamc selected = getTarget();
        if(selected instanceof BaseTurret.BaseTurretBuild turretBuild) {
            Drawf.dashCircle(turretBuild.x, turretBuild.y, turretBuild.range(), turretBuild.team.color);
        } else if(selected instanceof Unit unit) {
            Drawf.dashCircle(unit.x, unit.y, unit.range(), unit.team.color);
        }
    }
}
