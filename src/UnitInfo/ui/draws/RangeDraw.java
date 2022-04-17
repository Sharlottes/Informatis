package UnitInfo.ui.draws;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.gl.FrameBuffer;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.CheckBox;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.ui.Styles;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.defense.turrets.TractorBeamTurret;
import mindustry.world.blocks.defense.turrets.Turret;

import static UnitInfo.SVars.turretRange;
import static UnitInfo.core.OverDrawer.isInCamera;
import static arc.Core.graphics;
import static arc.Core.settings;
import static mindustry.Vars.player;

public class RangeDraw extends OverDraw {
    FrameBuffer effectBuffer = new FrameBuffer();
    ObjectMap<Team, Seq<BaseTurret.BaseTurretBuild>> tmpbuildobj = new ObjectMap<>();
    boolean ground = false, air = false;

    RangeDraw(String name, TextureRegionDrawable icon) {
        super(name, icon);
    }

    @Override
    public void draw() {
        if(!enabled) return;

        effectBuffer.resize(graphics.getWidth(), graphics.getHeight());

        Unit unit = player.unit();
        tmpbuildobj.clear();
        for(Team team : Team.baseTeams) {
            Draw.drawRange(166 + (Team.baseTeams.length-team.id) * 3, 1, () -> effectBuffer.begin(Color.clear), () -> {
                effectBuffer.end();
                effectBuffer.blit(turretRange);
            });
            tmpbuildobj.put(team, new Seq<>());
        }

        Groups.build.each(b-> settings.getBool("aliceRange") || player.team() != b.team, b -> {
            if(b instanceof BaseTurret.BaseTurretBuild turret) {
                float range = turret.range();
                if (isInCamera(b.x, b.y, range)) {
                    int index = b.team.id;
                    Draw.color(b.team.color);

                    boolean valid = false;
                    if (unit == null) valid = true;
                    else if (b instanceof Turret.TurretBuild build) {
                        Turret t = (Turret) build.block;
                        if(t.targetAir&&!air||t.targetGround&&!ground) return;
                        if((unit.isFlying() ? t.targetAir : t.targetGround) && build.hasAmmo() && build.cons.valid()) valid = true;
                    } else if (b instanceof TractorBeamTurret.TractorBeamBuild build) {
                        TractorBeamTurret t = (TractorBeamTurret) build.block;
                        if(t.targetAir&&!air||t.targetGround&&!ground) return;
                        if((unit.isFlying() ? t.targetAir : t.targetGround) && build.cons.valid()) valid = true;
                    }

                    if(!valid) index = 0;

                    if(b.team==player.team()) index = b.team.id;

                    Draw.color(Team.baseTeams[index].color);
                    if (settings.getBool("RangeShader")) {
                        Draw.z(166+(Team.baseTeams.length-index)*3);
                        Fill.poly(b.x, b.y, Lines.circleVertices(range), range);
                    } else Drawf.dashCircle(b.x, b.y, range, b.team.color);
                }
            }
        });
    }

    @Override
    public void displayStats(Table parent) {
        super.displayStats(parent);

        parent.background(Styles.squaret.up);

        parent.check("enable ground", ground&&enabled, b->ground=b&&enabled).disabled(!enabled).row();
        parent.check("enable air", air&&enabled, b->air=b&&enabled).disabled(!enabled).row();
    }

    @Override
    public <T> void onEnabled(T param) {
        super.onEnabled(param);

        if(param instanceof Table t) {
            for (int i = 0; i < t.getChildren().size; i++) {
                Element elem = t.getChildren().get(i);
                if (elem instanceof CheckBox cb) cb.setDisabled(!enabled);
            }
        }
    }
}
