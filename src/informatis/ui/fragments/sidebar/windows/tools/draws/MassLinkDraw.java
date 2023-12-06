package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.payloads.PayloadMassDriver;

import static informatis.SUtils.getTarget;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class MassLinkDraw extends OverDraw {
    final Seq<MassDriver.MassDriverBuild> linkedMasses = new Seq<>();
    final Seq<PayloadMassDriver.PayloadDriverBuild> linkedPayloadMasses = new Seq<>();

    public MassLinkDraw() {
        super("massLink");
    }


    @Override
    public void draw() {
        Teamc target = getTarget();
        if(target instanceof Building build) {
            if (target instanceof MassDriver.MassDriverBuild mass) {
                linkedMasses.clear();
                drawMassLink(mass);
            } else if (target instanceof PayloadMassDriver.PayloadDriverBuild mass) {
                linkedPayloadMasses.clear();
                drawMassPayloadLink(mass);
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
}
