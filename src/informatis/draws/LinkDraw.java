package informatis.draws;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.ai.types.CargoAI;
import mindustry.core.Renderer;
import mindustry.entities.units.UnitController;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Build;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.payloads.PayloadMassDriver;
import mindustry.world.blocks.units.UnitCargoLoader;

import static arc.Core.atlas;
import static arc.Core.settings;
import static informatis.SUtils.getTarget;
import static mindustry.Vars.*;
import static informatis.SVars.*;

public class LinkDraw extends OverDraw {
    Seq<MassDriver.MassDriverBuild> linkedMasses = new Seq<>();
    Seq<PayloadMassDriver.PayloadDriverBuild> linkedPayloadMasses = new Seq<>();
    Seq<Building> linkedNodes = new Seq<>();

    LinkDraw(String name, TextureRegionDrawable icon) {
        super(name, icon);
        registerOption("powerNode");
        registerOption("massDriver");
    }

    @Override
    public void draw() {
        if(!enabled) return;
        Teamc target = getTarget();
        Draw.z(Layer.max);

        Groups.build.each(building -> {
            if(building instanceof UnitCargoLoader.UnitTransportSourceBuild build) {
                Unit unit = build.unit;
                if(unit != null && unit.item() != null && unit.controller() instanceof CargoAI ai && ai.unloadTarget != null) {
                    Building targetBuild = ai.unloadTarget;

                    Lines.stroke(2);
                    Draw.color(build.team.color);
                    Draw.alpha(0.5f);
                    Lines.line(build.x, build.y, unit.x, unit.y);
                    Draw.color(unit.item().color);
                    Draw.alpha(0.5f);
                    Lines.line(unit.x, unit.y, targetBuild.x, targetBuild.y);
                }
            }
        });
        if(target instanceof Building b){
            if(settings.getBool("powerNode") && enabled) {
                linkedNodes.clear();
                drawNodeLink(b);
            }
            if(settings.getBool("massDriver") && enabled) {
                if (target instanceof MassDriver.MassDriverBuild mass) {
                    linkedMasses.clear();
                    drawMassLink(mass);
                } else if (target instanceof PayloadMassDriver.PayloadDriverBuild mass) {
                    linkedPayloadMasses.clear();
                    drawMassPayloadLink(mass);
                }
            }
        }
    }

    void drawMassPayloadLink(PayloadMassDriver.PayloadDriverBuild from){
        float sin = Mathf.absin(Time.time, 6f, 1f);

        //call every mass drivers that link to this driver
        for(Building b : Groups.build) {
            if (b != from && b instanceof PayloadMassDriver.PayloadDriverBuild fromMass && world.build(fromMass.link) == from && !linkedPayloadMasses.contains(fromMass)) {
                linkedPayloadMasses.add(fromMass);
                drawMassPayloadLink(fromMass);
            }
        }

        //get and draw line between this mass driver and linked one
        Building target = world.build(from.link);
        if(target instanceof PayloadMassDriver.PayloadDriverBuild targetDriver) {
            Tmp.v1.set(from.x + from.block.offset, from.y + from.block.offset).sub(targetDriver.x, targetDriver.y).limit(from.block.size * tilesize +  sin + 0.5f);
            float x2 = from.x - Tmp.v1.x, y2 = from.y - Tmp.v1.y, x1 = targetDriver.x + Tmp.v1.x, y1 = targetDriver.y + Tmp.v1.y;
            int segs = (int) (targetDriver.dst(from.x, from.y) / tilesize);
            Lines.stroke(4f, Pal.gray);
            Lines.dashLine(x1, y1, x2, y2, segs);
            Lines.stroke(2f, Pal.placing);
            Lines.dashLine(x1, y1, x2, y2, segs);
            Lines.stroke(1f, Pal.accent);
            Drawf.circles(from.x, from.y, (from.tile.block().size / 2f + 1) * tilesize +  sin - 2f, Pal.accent);
            Drawf.arrow(from.x, from.y, targetDriver.x, targetDriver.y, from.block.size * tilesize +  sin, 4f +  sin);
            for (Building shooter : from.waitingShooters) {
                Drawf.circles(shooter.x, shooter.y, (from.tile.block().size / 2f + 1) * tilesize +  sin - 2f);
                Drawf.arrow(shooter.x, shooter.y, from.x, from.y, from.block.size * tilesize +  sin, 4f +  sin);
            }

            //call method again when target links to another mass driver which isn't stored in array
            if(!linkedPayloadMasses.contains(targetDriver)) {
                linkedPayloadMasses.add(targetDriver);
                drawMassPayloadLink(targetDriver);
            }
        }
    }

    void drawMassLink(MassDriver.MassDriverBuild from){
        float sin = Mathf.absin(Time.time, 6f, 1f);

        //call every mass drivers that link to this driver
        for(Building b : Groups.build) {
            if (b != from && b instanceof MassDriver.MassDriverBuild fromMass && world.build(fromMass.link) == from && !linkedMasses.contains(fromMass)) {
                linkedMasses.add(fromMass);
                drawMassLink(fromMass);
            }
        }

        //get and draw line between this mass driver and linked one
        Building target = world.build(from.link);
        if(target instanceof MassDriver.MassDriverBuild targetDriver) {
            Tmp.v1.set(from.x + from.block.offset, from.y + from.block.offset).sub(targetDriver.x, targetDriver.y).limit(from.block.size * tilesize +  sin + 0.5f);
            float x2 = from.x - Tmp.v1.x, y2 = from.y - Tmp.v1.y, x1 = targetDriver.x + Tmp.v1.x, y1 = targetDriver.y + Tmp.v1.y;
            int segs = (int) (targetDriver.dst(from.x, from.y) / tilesize);
            Lines.stroke(4f, Pal.gray);
            Lines.dashLine(x1, y1, x2, y2, segs);
            Lines.stroke(2f, Pal.placing);
            Lines.dashLine(x1, y1, x2, y2, segs);
            Lines.stroke(1f, Pal.accent);
            Drawf.circles(from.x, from.y, (from.tile.block().size / 2f + 1) * tilesize +  sin - 2f, Pal.accent);
            Drawf.arrow(from.x, from.y, targetDriver.x, targetDriver.y, from.block.size * tilesize +  sin, 4f +  sin);
            for (Building shooter : from.waitingShooters) {
                Drawf.circles(shooter.x, shooter.y, (from.tile.block().size / 2f + 1) * tilesize +  sin - 2f);
                Drawf.arrow(shooter.x, shooter.y, from.x, from.y, from.block.size * tilesize +  sin, 4f +  sin);
            }

            //call method again when target links to another mass driver which isn't stored in array
            if(!linkedMasses.contains(targetDriver)) {
                linkedMasses.add(targetDriver);
                drawMassLink(targetDriver);
            }
        }
    }

    IntSeq getPowerLinkedBuilds(Building build) {
        IntSeq seq = new IntSeq(build.power.links);
        seq.addAll(build.proximity().mapInt(Building::pos));
        return seq;
    }

    void drawNodeLink(Building node) {
        if(node.power == null) return;
        if(!linkedNodes.contains(node)) {
            linkedNodes.add(node);
            int[] builds = getPowerLinkedBuilds(node).items;
            for(int i : builds) {
                Building other = world.build(i);
                if(other == null || other.power == null) return;
                float angle1 = Angles.angle(node.x, node.y, other.x, other.y),
                        vx = Mathf.cosDeg(angle1), vy = Mathf.sinDeg(angle1),
                        len1 = node.block.size * tilesize / 2f - 1.5f, len2 = other.block.size * tilesize / 2f - 1.5f;
                Draw.color(Color.white, Color.valueOf("98ff98"), (1f - node.power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
                Draw.alpha(Renderer.laserOpacity);
                Drawf.laser(atlas.find("informatis-Slaser"), atlas.find("informatis-Slaser"), atlas.find("informatis-Slaser-end"), node.x + vx * len1, node.y + vy * len1, other.x - vx * len2, other.y - vy * len2, 0.25f);

                drawNodeLink(other);
            }
        }
    }
}
