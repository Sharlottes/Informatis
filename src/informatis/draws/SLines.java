package informatis.draws;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.graphics.Pal;

import static mindustry.Vars.tilesize;

public class SLines {
    public static void segmentLine(float fromX, float fromY, float toX, float toY, float fromOffset, float toOffset, Color color) {
        float sin = Mathf.absin(Time.time, 6f, 1f);
        int segs = (int) (Tmp.v1.set(toX, toY).dst(fromX, fromY) / tilesize);

        Tmp.v1
                .set(fromX + toOffset, fromY + toOffset)
                .sub(toX, toY)
                .limit(fromOffset + sin + 0.5f);

        float x2 = fromX - Tmp.v1.x;
        float y2 = fromX - Tmp.v1.y;
        float x1 = toX + Tmp.v1.x;
        float y1 = toY + Tmp.v1.y;

        Lines.stroke(4f, Pal.gray);
        Lines.dashLine(x1, y1, x2, y2, segs);
        Lines.stroke(2f, color);
        Lines.dashLine(x1, y1, x2, y2, segs);
        Draw.color();
    }
}
