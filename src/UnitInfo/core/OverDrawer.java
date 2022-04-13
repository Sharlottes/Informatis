package UnitInfo.core;

import UnitInfo.ui.*;
import arc.*;
import arc.audio.Sound;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ai.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;

import java.util.Objects;

import static UnitInfo.SVars.*;
import static arc.Core.*;
import static mindustry.Vars.*;

public class OverDrawer {
    public static Teamc target;
    public static Seq<MassDriver.MassDriverBuild> linkedMasses = new Seq<>();
    public static Seq<PayloadMassDriver.PayloadDriverBuild> linkedPayloadMasses = new Seq<>();
    public static Seq<Building> linkedNodes = new Seq<>();
    public static Seq<Building> linkedBuilds = new Seq<>();
    public static Seq<Tile> pathTiles = new Seq<>();
    public static FrameBuffer effectBuffer = new FrameBuffer();
    public static int otherCores;
    public static boolean locked;
    public static ObjectMap<Team, Seq<BaseTurret.BaseTurretBuild>> tmpbuildobj = new ObjectMap<>();

    static float sin = Mathf.absin(Time.time, 6f, 1f);

    public static void setEvent(){
        Events.run(EventType.Trigger.draw, () -> {
            effectBuffer.resize(graphics.getWidth(), graphics.getHeight());

            //local drawing, drawn on player/camera position
            if(settings.getBool("spawnerarrow")) {
                spawner.getSpawns().each(t -> {
                    float leng = (player.unit() != null && player.unit().hitSize > 4 * 8f ? player.unit().hitSize * 1.5f : 4 * 8f) + sin;

                    Tmp.v1.set(camera.position);
                    Lines.stroke(1f + sin / 2, Pal.accent);
                    Lines.circle(Tmp.v1.x, Tmp.v1.y, leng - 4f);
                    Drawf.arrow(Tmp.v1.x, Tmp.v1.y, t.worldx(), t.worldy(), leng, (Math.min(200 * 8f, Mathf.dst(Tmp.v1.x, Tmp.v1.y, t.worldx(), t.worldy())) / (200 * 8f)) * (5f + sin));
                });
            }

            if(settings.getBool("select") && target != null) {
                for(int i = 0; i < 4; i++){
                    float rot = i * 90f + 45f + (-Time.time) % 360f;
                    float length = (target instanceof Unit u ? u.hitSize : target instanceof Building b ? b.block.size * tilesize : 0) * 1.5f + 2.5f;
                    Draw.color(Tmp.c1.set(locked ? Color.orange : Color.darkGray).lerp(locked ? Color.scarlet : Color.gray, Mathf.absin(Time.time, 3f, 1f)).a(settings.getInt("selectopacity") / 100f));
                    Draw.rect("select-arrow", target.x() + Angles.trnsx(rot, length), target.y() + Angles.trnsy(rot, length), length / 1.9f, length / 1.9f, rot - 135f);
                }
                if(settings.getBool("distanceLine") && player.unit() != null && !player.unit().dead && target != null) { //need selected other unit with living player
                    Teamc from = player.unit();
                    Position to = target;
                    if(to == from) to = input.mouseWorld();
                    if(player.unit() instanceof BlockUnitUnit bu) Tmp.v1.set(bu.x() + bu.tile().block.offset, bu.y() + bu.tile().block.offset).sub(to.getX(), to.getY()).limit(bu.tile().block.size * tilesize + sin + 0.5f);
                    else Tmp.v1.set(from.x(), from.y()).sub(to.getX(), to.getY()).limit(player.unit().hitSize + sin + 0.5f);

                    float x2 = from.x() - Tmp.v1.x, y2 = from.y() - Tmp.v1.y,
                            x1 = to.getX() + Tmp.v1.x, y1 = to.getY() + Tmp.v1.y;
                    int segs = (int) (to.dst(from.x(), from.y()) / tilesize);
                    if(segs > 0){
                        Lines.stroke(2.5f, Pal.gray);
                        Lines.dashLine(x1, y1, x2, y2, segs);
                        Lines.stroke(1f, Pal.placing);
                        Lines.dashLine(x1, y1, x2, y2, segs);

                        Fonts.outline.draw(Strings.fixed(to.dst(from.x(), from.y()), 2) + " (" + segs + " " + bundle.get("tiles") + ")",
                                from.x() + Angles.trnsx(Angles.angle(from.x(), from.y(), to.getX(), to.getY()), player.unit().hitSize() + Math.min(segs, 6) * 8f),
                                from.y() + Angles.trnsy(Angles.angle(from.x(), from.y(), to.getX(), to.getY()), player.unit().hitSize() + Math.min(segs, 6) * 8f) - 3,
                                Pal.accent, 0.25f, false, Align.center);
                    }
                }
            }

            //global drawing, which needs camera-clipping
            Core.camera.bounds(Tmp.r1);
            Groups.unit.each(u -> {
                UnitController c = u.controller();
                UnitCommand com = u.team.data().command;

                if(logicLine && c instanceof LogicAI ai && (ai.control == LUnitControl.approach || ai.control == LUnitControl.move) && isInCamera(Tmp.r1, u.x, u.y, u.hitSize)) {
                    Lines.stroke(1, u.team.color);
                    Lines.line(u.x(), u.y(), ai.moveX, ai.moveY);
                    Lines.stroke(0.5f + Mathf.absin(6f, 0.5f), Tmp.c1.set(Pal.logicOperations).lerp(Pal.sap, Mathf.absin(6f, 0.5f)));
                    Lines.line(u.x(), u.y(), ai.controller.x, ai.controller.y);
                }

                if(!unitLine || u.type.flying || com == UnitCommand.idle ||
                        c instanceof MinerAI ||
                        c instanceof BuilderAI ||
                        c instanceof RepairAI ||
                        c instanceof DefenderAI ||
                        c instanceof FormationAI ||
                        c instanceof FlyingAI) return;

                Lines.stroke(1, u.team.color);

                otherCores = Groups.build.count(b -> b instanceof CoreBlock.CoreBuild && b.team != u.team);
                getNextTile(u.tileOn(), u.controller() instanceof SuicideAI ? 0 : u.pathType(), u.team, com.ordinal());
                pathTiles.filter(Objects::nonNull);

                for(int i = 0; i < pathTiles.size; i++) {
                    Tile from = pathTiles.get(i);
                    Tile to = pathTiles.get(i + 1);
                    if(!isInCamera(Tmp.r1, from.x, from.y)) continue;
                    Lines.line(from.worldx(), from.worldy(), to.worldx(), to.worldy());
                }
                pathTiles.clear();
            });

            if(pathLine) spawner.getSpawns().each(t -> {
                Team enemyTeam = state.rules.waveTeam;
                Lines.stroke(1, enemyTeam.color);
                for(int p = 0; p < (Vars.state.rules.spawns.count(g->g.type.naval)>0?3:2); p++) {
                    otherCores = Groups.build.count(b -> b instanceof CoreBlock.CoreBuild && b.team != enemyTeam);
                    getNextTile(t, p, enemyTeam, Pathfinder.fieldCore);
                    pathTiles.filter(Objects::nonNull);

                    for(int i = 0; i < pathTiles.size; i++) {
                        Tile from = pathTiles.get(i);
                        Tile to = pathTiles.get(i + 1);
                        if(!isInCamera(Tmp.r1, from.x, from.y)) continue;
                        Lines.line(from.worldx(), from.worldy(), to.worldx(), to.worldy());
                    }
                    pathTiles.clear();
                }
            });

            Draw.z(Layer.overlayUI);


            if(Core.settings.getBool("blockfont")) {
                indexer.eachBlock(null, camera.position.x, camera.position.y, 400, b -> isInCamera(Tmp.r1, b.x, b.y, b.block.size/2f), b -> {
                    if(b instanceof ForceProjector.ForceBuild force) {
                        ForceProjector forceBlock = (ForceProjector) force.block;
                        float max = forceBlock.shieldHealth + forceBlock.phaseShieldBoost * force.phaseHeat;

                        Fonts.outline.draw((int)b.health + " / " + (int)b.maxHealth,
                                b.x, b.y - b.block.size * 8 * 0.25f - 2,
                                Tmp.c1.set(Pal.items).lerp(Pal.health, 1-b.healthf()), (b.block.size == 1 ? 0.3f : 0.25f) * 0.25f * b.block.size, false, Align.center);
                        Fonts.outline.draw((int)(max-force.buildup) + " / " + (int)max,
                                b.x, b.y - b.block.size * 8 * 0.25f + 2,
                                Tmp.c1.set(Pal.shield).lerp(Pal.gray, 1-((max-force.buildup) / max)), (b.block.size == 1 ? 0.3f : 0.25f) * 0.25f * b.block.size, false, Align.center);
                    }
                    else if(b instanceof ReloadTurret.ReloadTurretBuild || b instanceof UnitFactory.UnitFactoryBuild || b instanceof Reconstructor.ReconstructorBuild) {
                        float progress = 0f;
                        if(b instanceof ReloadTurret.ReloadTurretBuild turret) progress = turret.reload / ((ReloadTurret)turret.block).reloadTime * 100;
                        if(b instanceof UnitFactory.UnitFactoryBuild factory) progress = factory.fraction() * 100;
                        if(b instanceof Reconstructor.ReconstructorBuild reconstructor) progress = reconstructor.fraction() * 100;

                        Fonts.outline.draw((int)b.health + " / " + (int)b.maxHealth,
                                b.x, b.y - b.block.size * 8 * 0.25f - 2,
                                Tmp.c1.set(Pal.items).lerp(Pal.health, 1-b.healthf()), (b.block.size == 1 ? 0.3f : 0.25f) * 0.25f * b.block.size, false, Align.center);
                        Fonts.outline.draw((int)progress + "%",
                                b.x, b.y - b.block.size * 8 * 0.25f + 2,
                                Tmp.c1.set(Color.lightGray).lerp(Pal.accent, progress/100), (b.block.size == 1 ? 0.3f : 0.25f) * 0.25f * b.block.size, false, Align.center);
                    }
                    else Fonts.outline.draw((int)b.health + " / " + (int)b.maxHealth,
                                b.x, b.y - b.block.size * 8 * 0.25f,
                                Tmp.c1.set(Pal.items).lerp(Pal.health, 1-b.healthf()), (b.block.size == 1 ? 0.3f : 0.25f) * 0.25f * b.block.size, false, Align.center);
                });
            }

            if(Core.settings.getBool("unithealthui")) //display unit health bar
                Groups.unit.each(u->isInCamera(Tmp.r1, u.x, u.y, u.hitSize/2f), FreeBar::draw);

            if(settings.getBool("blockstatus")) //display enemy block status
                Groups.build.each(b->isInCamera(Tmp.r1, b.x, b.y, b.block.size/2f) && Vars.player.team() == b.team, Building::drawStatus);

            if(!renderer.pixelator.enabled())
                Groups.unit.each(unit -> isInCamera(Tmp.r1, unit.x, unit.y, unit.hitSize/2f) && unit.item() != null && unit.itemTime > 0.01f, unit ->
                    Fonts.outline.draw(unit.stack.amount + "",
                        unit.x + Angles.trnsx(unit.rotation + 180f, unit.type.itemOffsetY),
                        unit.y + Angles.trnsy(unit.rotation + 180f, unit.type.itemOffsetY) - 3,
                        Pal.accent, 0.25f * unit.itemTime / Scl.scl(1f), false, Align.center)
            );

            if(settings.getBool("linkedMass")){
                if(target instanceof MassDriver.MassDriverBuild mass) {
                    linkedMasses.clear();
                    drawMassLink(mass);
                }
                else if(target instanceof PayloadMassDriver.PayloadDriverBuild mass) {
                    linkedPayloadMasses.clear();
                    drawMassPayloadLink(mass);
                }
            }

            if(settings.getBool("linkedNode") && target instanceof Building node){
                linkedNodes.clear();
                drawNodeLink(node);
            }


            if(settings.getBool("rangeNearby")) {
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
                        if (isInCamera(Tmp.r1, b.x, b.y, range)) {
                            Draw.color(b.team.color);

                            boolean valid = false;
                            if (unit == null) valid = true;
                            else if (b instanceof Turret.TurretBuild build) {
                                Turret t = (Turret) build.block;
                                if((unit.isFlying() ? t.targetAir : t.targetGround)) valid = true;
                                if(!build.hasAmmo() || !build.cons.valid()) Draw.color(Pal.gray);
                            } else if (b instanceof TractorBeamTurret.TractorBeamBuild build) {
                                TractorBeamTurret t = (TractorBeamTurret) build.block;
                                if((unit.isFlying() ? t.targetAir : t.targetGround)) valid = true;
                                if(!build.cons.valid()) Draw.color(Pal.gray);
                            }

                            if(!valid) return;

                            if(b.team==player.team()) Draw.color(player.team().color);

                            if (settings.getBool("RangeShader")) {
                                Draw.z(166+(Team.baseTeams.length-b.team.id)*3);
                                Fill.poly(b.x, b.y, Lines.circleVertices(range), range);
                            } else Drawf.dashCircle(b.x, b.y, range, b.team.color);
                        }
                    }
                });
            }
        });
    }

    static boolean isInCamera(Rect rect, float x, float y) {
        return isInCamera(rect, x, y, 0);
    }

    static boolean isInCamera(Rect rect, float x, float y, float size) {
        Tmp.r2.setCentered(x, y, size);
        return rect.overlaps(Tmp.r2);
    }

    public static Tile getNextTile(Tile tile, int cost, Team team, int finder) {
        Pathfinder.Flowfield field = pathfinder.getField(team, cost, Mathf.clamp(finder, 0, 1));
        Tile tile1 = pathfinder.getTargetTile(tile, field);
        pathTiles.add(tile1);
        if(tile1 == tile || tile1 == null ||
                (finder == 0 && (otherCores != Groups.build.count(b -> b instanceof CoreBlock.CoreBuild && b.team != team) || tile1.build instanceof CoreBlock.CoreBuild)) ||
                (finder == 1 && tile1.build instanceof CommandCenter.CommandBuild))
            return tile1;
        return getNextTile(tile1, cost, team, finder);
    }

    public static void drawMassPayloadLink(PayloadMassDriver.PayloadDriverBuild from){
        float sin = Mathf.absin(Time.time, 6f, 1f);
        Groups.build.each(b -> b instanceof PayloadMassDriver.PayloadDriverBuild fromMass &&
                world.build(fromMass.link) == from &&
                from.within(fromMass.x, fromMass.y, ((PayloadMassDriver)fromMass.block).range) &&
                !linkedPayloadMasses.contains(from), b -> {
            linkedPayloadMasses.add((PayloadMassDriver.PayloadDriverBuild) b);
            drawMassPayloadLink((PayloadMassDriver.PayloadDriverBuild) b);
        });

        if(world.build(from.link) instanceof PayloadMassDriver.PayloadDriverBuild to && from != to &&
                to.within(from.x, from.y, ((PayloadMassDriver)from.block).range)){
            Tmp.v1.set(from.x + from.block.offset, from.y + from.block.offset).sub(to.x, to.y).limit(from.block.size * tilesize + sin + 0.5f);
            float x2 = from.x - Tmp.v1.x, y2 = from.y - Tmp.v1.y,
                    x1 = to.x + Tmp.v1.x, y1 = to.y + Tmp.v1.y;
            int segs = (int)(to.dst(from.x, from.y)/tilesize);

            Lines.stroke(4f, Pal.gray);
            Lines.dashLine(x1, y1, x2, y2, segs);
            Lines.stroke(2f, Pal.placing);
            Lines.dashLine(x1, y1, x2, y2, segs);
            Lines.stroke(1f, Pal.accent);
            Drawf.circles(from.x, from.y, (from.tile.block().size / 2f + 1) * tilesize + sin - 2f, Pal.accent);

            for(var shooter : from.waitingShooters){
                Drawf.circles(shooter.x, shooter.y, (from.tile.block().size / 2f + 1) * tilesize + sin - 2f);
                Drawf.arrow(shooter.x, shooter.y, from.x, from.y, from.block.size * tilesize + sin, 4f + sin);
            }
            if(from.link != -1 && world.build(from.link) instanceof PayloadMassDriver.PayloadDriverBuild other && other.block == from.block && other.team == from.team && from.within(other, ((PayloadMassDriver)from.block).range)){
                Building target = world.build(from.link);
                Drawf.circles(target.x, target.y, (target.block().size / 2f + 1) * tilesize + sin - 2f);
                Drawf.arrow(from.x, from.y, target.x, target.y, from.block.size * tilesize + sin, 4f + sin);
            }
            if(world.build(to.link) instanceof PayloadMassDriver.PayloadDriverBuild newTo && to != newTo &&
                    newTo.within(to.x, to.y, ((PayloadMassDriver)to.block).range) && !linkedPayloadMasses.contains(to)){
                linkedPayloadMasses.add(to);
                drawMassPayloadLink(to);
            }
        }
    }

    public static void drawMassLink(MassDriver.MassDriverBuild from){
        float sin = Mathf.absin(Time.time, 6f, 1f);
        Groups.build.each(b -> b instanceof MassDriver.MassDriverBuild fromMass &&
                world.build(fromMass.link) == from &&
                from.within(fromMass.x, fromMass.y, ((MassDriver)fromMass.block).range) &&
                !linkedMasses.contains(from), b -> {
            linkedMasses.add((MassDriver.MassDriverBuild) b);
            drawMassLink((MassDriver.MassDriverBuild) b);
        });

        if(world.build(from.link) instanceof MassDriver.MassDriverBuild to && from != to && to.within(from.x, from.y, ((MassDriver)from.block).range)){
            Tmp.v1.set(from.x + from.block.offset, from.y + from.block.offset).sub(to.x, to.y).limit(from.block.size * tilesize + sin + 0.5f);
            float x2 = from.x - Tmp.v1.x, y2 = from.y - Tmp.v1.y,
                    x1 = to.x + Tmp.v1.x, y1 = to.y + Tmp.v1.y;
            int segs = (int)(to.dst(from.x, from.y)/tilesize);

            Lines.stroke(4f, Pal.gray);
            Lines.dashLine(x1, y1, x2, y2, segs);
            Lines.stroke(2f, Pal.placing);
            Lines.dashLine(x1, y1, x2, y2, segs);
            Lines.stroke(1f, Pal.accent);
            Drawf.circles(from.x, from.y, (from.tile.block().size / 2f + 1) * tilesize + sin - 2f, Pal.accent);

            for(var shooter : from.waitingShooters){
                Drawf.circles(shooter.x, shooter.y, (from.tile.block().size / 2f + 1) * tilesize + sin - 2f);
                Drawf.arrow(shooter.x, shooter.y, from.x, from.y, from.block.size * tilesize + sin, 4f + sin);
            }
            if(from.link != -1 && world.build(from.link) instanceof MassDriver.MassDriverBuild other && other.block == from.block && other.team == from.team && from.within(other, ((MassDriver)from.block).range)){
                Building target = world.build(from.link);
                Drawf.circles(target.x, target.y, (target.block().size / 2f + 1) * tilesize + sin - 2f);
                Drawf.arrow(from.x, from.y, target.x, target.y, from.block.size * tilesize + sin, 4f + sin);
            }
            if(world.build(to.link) instanceof MassDriver.MassDriverBuild newTo && to != newTo &&
                    newTo.within(to.x, to.y, ((MassDriver)to.block).range) && !linkedMasses.contains(to)){
                linkedMasses.add(to);
                drawMassLink(to);
            }
        }
    }

    public static Seq<Building> getPowerLinkedBuilds(Building build) {
        linkedBuilds.clear();
        build.power.links.each(i -> linkedBuilds.add(world.build(i)));
        build.proximity().each(linkedBuilds::add);
        linkedBuilds.filter(b -> b != null && b.power != null);
        if(!build.block.outputsPower && !(build instanceof PowerNode.PowerNodeBuild))
            linkedBuilds.filter(b -> b.block.outputsPower || b instanceof PowerNode.PowerNodeBuild);
        return linkedBuilds;
    }

    public static void drawNodeLink(Building node) {
        if(node.power == null) return;
        if(!linkedNodes.contains(node)) {
            linkedNodes.add(node);
            getPowerLinkedBuilds(node).each(other -> {
                float angle1 = Angles.angle(node.x, node.y, other.x, other.y),
                        vx = Mathf.cosDeg(angle1), vy = Mathf.sinDeg(angle1),
                        len1 = node.block.size * tilesize / 2f - 1.5f, len2 = other.block.size * tilesize / 2f - 1.5f;

                Draw.color(Color.white, Color.valueOf("98ff98"), (1f - node.power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
                Draw.alpha(Renderer.laserOpacity);
                Drawf.laser(node.team, atlas.find("unitinfo-Slaser"), atlas.find("unitinfo-Slaser-end"), node.x + vx*len1, node.y + vy*len1, other.x - vx*len2, other.y - vy*len2, 0.25f);

                if(other.power != null) getPowerLinkedBuilds(other).each(OverDrawer::drawNodeLink);
            });
        }
    }
}
