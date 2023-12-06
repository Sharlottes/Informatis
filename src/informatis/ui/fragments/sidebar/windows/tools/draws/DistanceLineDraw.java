package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.util.Align;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.BlockUnitUnit;
import mindustry.gen.Posc;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;

import static arc.Core.*;
import static informatis.SUtils.getTarget;
import static mindustry.Vars.*;

public class DistanceLineDraw extends OverDraw {
    public DistanceLineDraw() {
        super("distanceLine");
    }

    @Override
    public void draw() {
        float sin = Mathf.absin(Time.time, 6f, 1f);
        Posc from = player;
        Position to = getTarget();
        if(to == from || to == null) to = input.mouseWorld();
        if(player.unit() instanceof BlockUnitUnit bu) Tmp.v1.set(bu.x() + bu.tile().block.offset, bu.y() + bu.tile().block.offset).sub(to.getX(), to.getY()).limit(bu.tile().block.size * tilesize +  sin + 0.5f);
        else Tmp.v1.set(from.x(), from.y()).sub(to.getX(), to.getY()).limit((player.unit() == null ? 0 : player.unit().hitSize) + sin + 0.5f);

        float x2 = from.x() - Tmp.v1.x, y2 = from.y() - Tmp.v1.y, x1 = to.getX() + Tmp.v1.x, y1 = to.getY() + Tmp.v1.y;
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
