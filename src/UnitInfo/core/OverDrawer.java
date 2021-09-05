package UnitInfo.core;

import UnitInfo.ui.FreeBar;
import arc.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.*;
import arc.math.geom.Geometry;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.ai.Pathfinder;
import mindustry.ai.types.*;
import mindustry.content.Fx;
import mindustry.core.Renderer;
import mindustry.entities.*;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.units.AIController;
import mindustry.entities.units.UnitCommand;
import mindustry.entities.units.UnitController;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.LUnitControl;
import mindustry.logic.Ranged;
import mindustry.ui.Fonts;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ControlBlock;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.payloads.PayloadMassDriver;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.CommandCenter;

import java.util.Objects;

import static UnitInfo.SVars.*;
import static arc.Core.*;
import static arc.Core.input;
import static mindustry.Vars.*;
import static mindustry.Vars.player;

public class OverDrawer {
    @Nullable public static Teamc target;
    public static Seq<MassDriver.MassDriverBuild> linkedMasses = new Seq<>();
    public static Seq<PayloadMassDriver.PayloadDriverBuild> linkedPayloadMasses = new Seq<>();
    public static Seq<Building> linkedNodes = new Seq<>();
    public static int otherCores;
    public static Seq<Tile> pathTiles = new Seq<>();

    @SuppressWarnings("unchecked")
    public static <T extends Teamc> T getTarget(){
        return (T) (target = hud.getTarget());
    }

    public static void setEvent(){
        Events.run(EventType.Trigger.draw, () -> {
            float sin = Mathf.absin(Time.time, 6f, 1f);
            Draw.z(Layer.overlayUI + 1);

            int[] units = {0};
            if(pathLine || unitLine || logicLine) Groups.unit.each(u -> {
                UnitController c = u.controller();
                UnitCommand com = u.team.data().command;

                if(c instanceof LogicAI ai){
                    if(logicLine && (ai.control == LUnitControl.approach || ai.control == LUnitControl.move)) {
                        Lines.stroke(1, u.team.color);
                        Lines.line(u.x(), u.y(), ai.moveX, ai.moveY);
                        Lines.stroke(0.5f + Mathf.absin(6f, 0.5f), Tmp.c1.set(Pal.logicOperations).lerp(Pal.sap, Mathf.absin(6f, 0.5f)));
                        Lines.line(u.x(), u.y(), ai.controller.x, ai.controller.y);
                    }
                    return;
                }

                if(++units[0] > settings.getInt("unitlinelimit") || //prevent lag
                        !unitLine || //disabled
                        u.type.flying || //not flying
                        c instanceof MinerAI || //not mono
                        c instanceof BuilderAI || //not poly
                        c instanceof RepairAI || //not mega
                        c instanceof DefenderAI || //not oct
                        c instanceof FormationAI || //not commanded unit by player
                        c instanceof FlyingAI || //not flying anyway
                        com == UnitCommand.idle) return; //not idle

                otherCores = Groups.build.count(b -> b instanceof CoreBlock.CoreBuild && b.team != u.team);
                getNextTile(u.tileOn(), u.pathType(), u.team, com.ordinal());
                pathTiles.filter(Objects::nonNull);
                for(int i = 1; i < pathTiles.size; i++) {
                    if(i + 1 >= pathTiles.size) continue; //prevent IndexOutException
                    Tile tile1 = pathTiles.get(i);
                    Tile tile2 = pathTiles.get(i + 1);
                    Lines.stroke(1, u.team.color);
                    Lines.line(tile1.worldx(), tile1.worldy(), tile2.worldx(), tile2.worldy());
                }
                pathTiles.clear();
            });

            if(pathLine) spawner.getSpawns().each(t -> {
                Team enemyTeam = state.rules.waveTeam;
                for(int p = 0; p < 3; p++) {
                    otherCores = Groups.build.count(b -> b instanceof CoreBlock.CoreBuild && b.team != enemyTeam);

                    getNextTile(t, p, enemyTeam, Pathfinder.fieldCore);
                    pathTiles.filter(Objects::nonNull);
                    for(int i = 1; i < pathTiles.size; i++) {
                        if(i + 1 >= pathTiles.size) continue; //prevent IndexOutException
                        Tile tile1 = pathTiles.get(i);
                        Tile tile2 = pathTiles.get(i + 1);
                        Lines.stroke(1, enemyTeam.color);
                        Lines.line(tile1.worldx(), tile1.worldy(), tile2.worldx(), tile2.worldy());
                    }
                    pathTiles.clear();
                }
            });

            if(settings.getBool("spawnerarrow")) spawner.getSpawns().each(t -> {
                Drawf.circles(camera.position.x, camera.position.y, (player.unit() != null && player.unit().hitSize > 4 * 8f ? player.unit().hitSize * 1.5f : 4 * 8f) + sin - 4f);
                Drawf.arrow(camera.position.x, camera.position.y, t.x * 8f, t.y * 8f,  (player.unit() != null && player.unit().hitSize > 4 * 8f ? player.unit().hitSize * 1.5f : 4 * 8f) + sin, (Math.min(200 * 8f, Mathf.dst(camera.position.x, camera.position.y, t.x * 8f, t.y * 8f)) / (200 * 8f)) * (5f + sin));
            });

            if(settings.getBool("blockstatus")) Groups.build.each(build -> {
                if(Vars.player != null && Vars.player.team() == build.team) return;

                Block block = build.block;
                if(block.enableDrawStatus && block.consumes.any()){
                    float multiplier = block.size > 1 ? 1 : 0.64f;
                    float brcx = build.x + (block.size * tilesize / 2f) - (tilesize * multiplier / 2f);
                    float brcy = build.y - (block.size * tilesize / 2f) + (tilesize * multiplier / 2f);

                    Draw.color(Pal.gray);
                    Fill.square(brcx, brcy, 2.5f * multiplier, 45);
                    Draw.color(build.status().color);
                    Fill.square(brcx, brcy, 1.5f * multiplier, 45);
                    Draw.color();
                }
            });

            if(Core.settings.getBool("unithealthui"))
                Groups.unit.each(unit -> new FreeBar().draw(unit));

            if(!mobile && !Vars.state.isPaused() && settings.getBool("gaycursor"))
                Fx.mine.at(Core.input.mouseWorldX(), Core.input.mouseWorldY(), Tmp.c2.set(Color.red).shiftHue(Time.time * 1.5f));

            if(!renderer.pixelator.enabled()) Groups.unit.each(unit -> unit.item() != null && unit.itemTime > 0.01f, unit -> {
                Fonts.outline.draw(unit.stack.amount + "",
                        unit.x + Angles.trnsx(unit.rotation + 180f, unit.type.itemOffsetY),
                        unit.y + Angles.trnsy(unit.rotation + 180f, unit.type.itemOffsetY) - 3,
                        Pal.accent, 0.25f * unit.itemTime / Scl.scl(1f), false, Align.center);
                Draw.reset();
            });

            // Turret Ranges
            if(settings.getBool("rangeNearby") && player != null && player.unit() != null && !player.unit().dead) {
                Team team = player.team();
                Unit unit = player.unit();
                Groups.build.each(e -> {
                    if(!settings.getBool("allTeamRange") && e.team == team) return; // Don't draw own turrets
                    if(!(e instanceof BaseTurret.BaseTurretBuild)) return; // Not a turret
                    if((e instanceof Turret.TurretBuild t && !t.hasAmmo()) || !e.cons.valid()) return; // No ammo

                    boolean canHit = e.block instanceof Turret t ? unit.isFlying() ? t.targetAir : t.targetGround :
                            e.block instanceof TractorBeamTurret tu && (unit.isFlying() ? tu.targetAir : tu.targetGround);
                    float range = ((BaseTurret.BaseTurretBuild) e).range();
                    float max = range + settings.getInt("rangeRadius") * tilesize + e.block.offset;
                    float dst = Mathf.dst(control.input.getMouseX(), control.input.getMouseY(), e.x, e.y);

                    if(control.input.block != null && dst <= max) canHit = e.block instanceof Turret t && t.targetGround;
                    if(player.dst(e) <= max || (control.input.block != null && dst <= max)) {
                        if(canHit || settings.getBool("allTargetRange")){
                            if(e instanceof Turret.TurretBuild t){
                                Lines.stroke(1.5f, Tmp.c1.set(canHit ? e.team.color : Team.derelict.color).a(0.75f));
                                Tmp.v1.set(e.x, e.y).trns(((BaseTurret.BaseTurretBuild)e).rotation+((Turret)t.block).shootCone, range);
                                Lines.line(e.x, e.y, e.x + Tmp.v1.x, e.y + Tmp.v1.y);
                                Tmp.v1.set(e.x, e.y).trns(((BaseTurret.BaseTurretBuild)e).rotation-((Turret)t.block).shootCone, range);
                                Lines.line(e.x, e.y, e.x + Tmp.v1.x, e.y + Tmp.v1.y);
                            }
                            Lines.stroke(1, Tmp.c1.set(canHit ? e.team.color : Team.derelict.color).a(0.5f));
                            Lines.poly(e.x, e.y, Lines.circleVertices(range), range);
                            Fill.light(e.x, e.y, Lines.circleVertices(range), range, Color.clear, Tmp.c1.a(Mathf.clamp(1-((control.input.block != null && dst <= max ? dst : player.dst(e))/max), 0, settings.getInt("softRangeOpacity")/100f)));
                        }
                    }
                });

                // Unit Ranges (Only works when turret ranges are enabled)
                if(settings.getBool("unitRange") || (settings.getBool("allTeamRange") && player.unit() != null)) {
                    Groups.unit.each(u -> {
                        if(!settings.getBool("unitRange") && settings.getBool("allTeamRange") && player.unit() != u) return; //player unit rule
                        if(!settings.getBool("allTeamRange") && u.team == team) return; // Don't draw own units
                        if(u.controller() instanceof AIController ai && (ai instanceof BuilderAI || ai instanceof MinerAI)) return; //don't draw poly and mono
                        boolean canHit = unit.isFlying() ? u.type.targetAir : u.type.targetGround;
                        float range = u.range();
                        float max = range + settings.getInt("rangeRadius") * tilesize;

                        if(Vars.player.dst(u) <= max) {
                            if (canHit || settings.getBool("allTargetRange")) // Same as above
                                    Lines.stroke(1, Tmp.c1.set(canHit ? u.team.color : Team.derelict.color).a(0.5f));
                                    Lines.poly(u.x, u.y, Lines.circleVertices(range), range);
                                    Fill.light(u.x, u.y, Lines.circleVertices(range), range, Color.clear, Tmp.c1.a(Math.min(settings.getInt("softRangeOpacity")/100f, 1-Vars.player.dst(u)/max)));
                        }
                    });
                }
            }

            if(!state.rules.polygonCoreProtection && settings.getBool("coreRange") && player != null){
                state.teams.eachEnemyCore(player.team(), core -> {
                    if(Core.camera.bounds(Tmp.r1).overlaps(Tmp.r2.setCentered(core.x, core.y, state.rules.enemyCoreBuildRadius * 2f))){
                        Draw.color(Color.darkGray);
                        Lines.circle(core.x, core.y - 2, state.rules.enemyCoreBuildRadius);
                        Draw.color(Pal.accent, core.team.color, 0.5f + Mathf.absin(Time.time, 10f, 0.5f));
                        Lines.circle(core.x, core.y, state.rules.enemyCoreBuildRadius);
                    }
                });
            }

            if(settings.getBool("linkedMass")){
                if(getTarget() instanceof MassDriver.MassDriverBuild mass) {
                    linkedMasses.clear();
                    drawMassLink(mass);
                }
                else if(getTarget() instanceof PayloadMassDriver.PayloadDriverBuild mass) {
                    linkedPayloadMasses.clear();
                    drawMassPayloadLink(mass);
                }
            }

            if(settings.getBool("linkedNode") && getTarget() instanceof Building node){
                linkedNodes.clear();
                drawNodeLink(node);
            }

            if(settings.getBool("select") && getTarget() != null) {
                Posc entity = getTarget();
                for(int i = 0; i < 4; i++){
                    float rot = i * 90f + 45f + (-Time.time) % 360f;
                    float length = (entity instanceof Unit ? ((Unit)entity).hitSize : entity instanceof Building ? ((Building)entity).block.size * tilesize : 0) * 1.5f + 2.5f;
                    Draw.color(Tmp.c1.set(hud.locked ? Color.orange : Color.darkGray).lerp(hud.locked ? Color.scarlet : Color.gray, Mathf.absin(Time.time, 3f, 1f)).a(settings.getInt("selectopacity") / 100f));
                    Draw.rect("select-arrow", entity.x() + Angles.trnsx(rot, length), entity.y() + Angles.trnsy(rot, length), length / 1.9f, length / 1.9f, rot - 135f);
                }
                if(settings.getBool("distanceLine") && player.unit() != null && !player.unit().dead && getTarget() != player.unit()) { //need selected other unit with living player
                    Teamc from = player.unit();
                    Teamc to = getTarget();
                    if(player.unit() instanceof BlockUnitUnit bu) Tmp.v1.set(bu.x() + bu.tile().block.offset, bu.y() + bu.tile().block.offset).sub(to.x(), to.y()).limit(bu.tile().block.size * tilesize + sin + 0.5f);
                    else Tmp.v1.set(from.x(), from.y()).sub(to.x(), to.y()).limit(player.unit().hitSize + sin + 0.5f);

                    float x2 = from.x() - Tmp.v1.x, y2 = from.y() - Tmp.v1.y,
                            x1 = to.x() + Tmp.v1.x, y1 = to.y() + Tmp.v1.y;
                    int segs = (int) (to.dst(from.x(), from.y()) / tilesize);
                    if(segs > 0){
                        Lines.stroke(2.5f, Pal.gray);
                        Lines.dashLine(x1, y1, x2, y2, segs);
                        Lines.stroke(1f, Pal.placing);
                        Lines.dashLine(x1, y1, x2, y2, segs);

                        Fonts.outline.draw(Strings.fixed(to.dst(from.x(), from.y()), 2) + " (" + segs + "tiles)",
                                from.x() + Angles.trnsx(Angles.angle(from.x(), from.y(), to.x(), to.y()), player.unit().hitSize() + 40),
                                from.y() + Angles.trnsy(Angles.angle(from.x(), from.y(), to.x(), to.y()), player.unit().hitSize() + 40) - 3,
                                Pal.accent, 0.25f, false, Align.center);
                    }
                }
            }

            Draw.reset();
        });

        Events.run(EventType.Trigger.update, ()->{
            if(settings.getBool("autoShooting")) {
                Unit unit = player.unit();
                if (unit.type == null) return;
                boolean omni = unit.type.omniMovement;
                boolean validHealTarget = unit.type.canHeal && target instanceof Building && ((Building) target).isValid() && target.team() == unit.team && ((Building) target).damaged() && target.within(unit, unit.type.range);
                boolean boosted = (unit instanceof Mechc && unit.isFlying());
                if ((unit.type != null && Units.invalidateTarget(target, unit, unit.type.range) && !validHealTarget) || state.isEditor()) {
                    target = null;
                }

                float mouseAngle = unit.angleTo(unit.aimX(), unit.aimY());
                boolean aimCursor = omni && player.shooting && unit.type.hasWeapons() && unit.type.faceTarget && !boosted && unit.type.rotateShooting;
                unit.lookAt(aimCursor ? mouseAngle : unit.prefRotation());

                //update shooting if not building + not mining
                if(!player.unit().activelyBuilding() && player.unit().mineTile == null) {
                    if(input.keyDown(KeyCode.mouseLeft)) {
                        player.shooting = !boosted;
                        unit.aim(player.mouseX = input.mouseWorldX(), player.mouseY = input.mouseWorldY());
                    } else if(target == null) {
                        player.shooting = false;
                        if(unit instanceof BlockUnitUnit b) {
                            if(b.tile() instanceof ControlBlock c && !c.shouldAutoTarget()) {
                                Building build = b.tile();
                                float range = build instanceof Ranged ? ((Ranged) build).range() : 0f;
                                boolean targetGround = build instanceof Turret.TurretBuild && ((Turret) build.block).targetAir;
                                boolean targetAir = build instanceof Turret.TurretBuild && ((Turret) build.block).targetGround;
                                target = Units.closestTarget(build.team, build.x, build.y, range, u -> u.checkTarget(targetAir, targetGround), u -> targetGround);
                            }
                            else target = null;
                        } else if(unit.type != null) {
                            float range = unit.hasWeapons() ? unit.range() : 0f;
                            target = Units.closestTarget(unit.team, unit.x, unit.y, range, u -> u.checkTarget(unit.type.targetAir, unit.type.targetGround), u -> unit.type.targetGround);

                            if(unit.type.canHeal && target == null) {
                                target = Geometry.findClosest(unit.x, unit.y, indexer.getDamaged(Team.sharded));
                                if (target != null && !unit.within(target, range)) {
                                    target = null;
                                }
                            }
                        }
                    } else {
                        player.shooting = !boosted;
                        unit.rotation(Angles.angle(unit.x, unit.y, target.x(), target.y()));
                        unit.aim(target.x(), target.y());
                    }
                }
                unit.controlWeapons(player.shooting && !boosted);
            }
        });
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
        Seq<Building> linkedBuilds = new Seq<>();
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
