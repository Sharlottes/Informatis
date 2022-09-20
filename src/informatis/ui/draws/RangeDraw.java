package informatis.ui.draws;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.graphics.gl.FrameBuffer;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.defense.turrets.TractorBeamTurret;
import mindustry.world.blocks.defense.turrets.Turret;

import static informatis.SVars.turretRange;
import static informatis.SUtils.*;
import static arc.Core.*;
import static mindustry.Vars.player;

public class RangeDraw extends OverDraw {
    FrameBuffer effectBuffer = new FrameBuffer();
    ObjectMap<Team, Seq<BaseTurret.BaseTurretBuild>> turrets = new ObjectMap<>();

    RangeDraw(String name, TextureRegionDrawable icon) {
        super(name, icon);
        registerOption("unitRange");
        registerOption("aliceRange");
        registerOption("airRange");
        registerOption("groundRange");
    }

    @Override
    public void draw() {
        if(!enabled) return;

        effectBuffer.resize(graphics.getWidth(), graphics.getHeight());

        Unit unit = player.unit();
        turrets.clear();
        for(Team team : Team.baseTeams) {
            Draw.drawRange(166 + (Team.baseTeams.length-team.id) * 3, 1, () -> effectBuffer.begin(Color.clear), () -> {
                effectBuffer.end();
                effectBuffer.blit(turretRange);
            });
            turrets.put(team, new Seq<>());
        }

        Groups.build.each(b-> settings.getBool("aliceRange") || player.team() != b.team, b -> {
            if(b instanceof BaseTurret.BaseTurretBuild turret) {
                float range = turret.range();
                if (!isInCamera(b.x, b.y, range)) return;

                boolean air = settings.getBool("airRange") && enabled;
                boolean ground = settings.getBool("groundRange") && enabled;
                boolean valid = false;
                if (unit == null) valid = true;
                else if (b instanceof Turret.TurretBuild build) {
                    Turret t = (Turret) build.block;
                    if(t.targetAir&&!air||t.targetGround&&!ground) return;
                    if((unit.isFlying() ? t.targetAir : t.targetGround) && build.hasAmmo() && build.canConsume()) valid = true;
                } else if (b instanceof TractorBeamTurret.TractorBeamBuild build) {
                    TractorBeamTurret t = (TractorBeamTurret) build.block;
                    if(t.targetAir&&!air||t.targetGround&&!ground) return;
                    if((unit.isFlying() ? t.targetAir : t.targetGround) && build.canConsume()) valid = true;
                }

                int index = valid ? b.team.id : 0;
                Draw.color(Team.baseTeams[index].color);
                if (settings.getBool("RangeShader")) {
                    Draw.z(166+(Team.baseTeams.length-index)*3);
                    Fill.poly(b.x, b.y, Lines.circleVertices(range), range);
                } else Drawf.dashCircle(b.x, b.y, range, b.team.color);
            }
        });
    }
}
