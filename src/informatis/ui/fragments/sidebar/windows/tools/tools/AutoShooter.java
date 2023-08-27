package informatis.ui.fragments.sidebar.windows.tools.tools;
import arc.input.KeyCode;
import arc.math.Angles;
import arc.math.geom.Geometry;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.logic.Ranged;
import mindustry.world.blocks.ControlBlock;
import mindustry.world.blocks.defense.turrets.Turret;
import static arc.Core.*;
import static mindustry.Vars.*;

public class AutoShooter extends Tool {
    Teamc shotTarget;
    public AutoShooter() {
        super("autoShoot");
    }

    @Override
    public void onUpdate() {
        Unit unit = player.unit();
        if (unit.type == null) return;
        boolean omni = unit.type.omniMovement;
        boolean validHealTarget = unit.type.canHeal && shotTarget instanceof Building b && b.isValid() && b.damaged() && shotTarget.team() == unit.team && shotTarget.within(unit, unit.type.range);
        boolean boosted = (unit instanceof Mechc && unit.isFlying());
        if ((unit.type != null && Units.invalidateTarget(shotTarget, unit, unit.type.range) && !validHealTarget) || state.isEditor()) {
            shotTarget = null;
        }
        float mouseAngle = unit.angleTo(unit.aimX(), unit.aimY());
        boolean aimCursor = omni && player.shooting && unit.type.hasWeapons() && unit.type.faceTarget && !boosted && unit.type.rotateToBuilding;
        unit.lookAt(aimCursor ? mouseAngle : unit.prefRotation());
        //update shooting if not building + not mining
        if(!player.unit().activelyBuilding() && player.unit().mineTile == null) {
            if(input.keyDown(KeyCode.mouseLeft)) {
                player.shooting = !boosted;
                unit.aim(player.mouseX = input.mouseWorldX(), player.mouseY = input.mouseWorldY());
            } else if(shotTarget == null) {
                player.shooting = false;
                if(unit instanceof BlockUnitUnit b) {
                    if(b.tile() instanceof ControlBlock c && !c.shouldAutoTarget()) {
                        Building build = b.tile();
                        float range = build instanceof Ranged ? ((Ranged) build).range() : 0f;
                        boolean targetGround = build instanceof Turret.TurretBuild && ((Turret) build.block).targetAir;
                        boolean targetAir = build instanceof Turret.TurretBuild && ((Turret) build.block).targetGround;
                        shotTarget = Units.closestTarget(build.team, build.x, build.y, range, u -> u.checkTarget(targetAir, targetGround), u -> targetGround);
                    }
                    else shotTarget = null;
                } else if(unit.type != null) {
                    float range = unit.hasWeapons() ? unit.range() : 0f;
                    shotTarget = Units.closestTarget(unit.team, unit.x, unit.y, range, u -> u.checkTarget(unit.type.targetAir, unit.type.targetGround), u -> unit.type.targetGround);
                    if(unit.type.canHeal && shotTarget == null) {
                        shotTarget = Geometry.findClosest(unit.x, unit.y, indexer.getDamaged(Team.sharded));
                        if (shotTarget != null && !unit.within(shotTarget, range)) {
                            shotTarget = null;
                        }
                    }
                }
            } else {
                player.shooting = !boosted;
                unit.rotation(Angles.angle(unit.x, unit.y, shotTarget.x(), shotTarget.y()));
                unit.aim(shotTarget.x(), shotTarget.y());
            }
        }
        unit.controlWeapons(player.shooting && !boosted);
    }
}