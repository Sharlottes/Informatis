package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.util.Align;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import informatis.draws.SLines;
import mindustry.gen.BlockUnitUnit;
import mindustry.gen.Posc;
import mindustry.gen.Unit;
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
        Unit playerUnit = player.unit();
        Posc from = player;
        Position to = getTarget();
        float hitSize = playerUnit == null ? 0 : playerUnit.hitSize();

        if(to == from || to == null) {
            to = input.mouseWorld();
        }

        int segs = (int) (to.dst(from.x(), from.y()) / tilesize);

        if(segs > 0) {
            SLines.segmentLine(
                    from.x(), from.y(),
                    to.getX(), to.getY(),
                    playerUnit instanceof BlockUnitUnit bu
                        ? bu.tile().block.size * tilesize
                        : hitSize,
                    playerUnit instanceof BlockUnitUnit bu
                        ? bu.tile().block.offset
                        : 0,
                    Pal.placing
            );
            Fonts.outline.draw(
                    Strings.fixed(to.dst(from.x(), from.y()), 2) + " (" + segs + " " + bundle.get("tiles") + ")",
                    from.x() + Angles.trnsx(Angles.angle(from.x(), from.y(), to.getX(), to.getY()), hitSize + Math.min(segs, 6) * 8f),
                    from.y() + Angles.trnsy(Angles.angle(from.x(), from.y(), to.getX(), to.getY()), hitSize + Math.min(segs, 6) * 8f) - 3,
                    Pal.accent, 0.25f, false, Align.center);
            Draw.color();
        }
    }
}
