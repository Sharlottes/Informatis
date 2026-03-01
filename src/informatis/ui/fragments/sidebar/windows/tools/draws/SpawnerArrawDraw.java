package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

import static arc.Core.camera;
import static mindustry.Vars.player;
import static mindustry.Vars.spawner;

public class SpawnerArrawDraw extends OverDraw {
    public SpawnerArrawDraw() {
        super("spawnerarrow");
    }

    @Override
    public void draw() {
        Unit playerUnit = player.unit();
        float sin = Mathf.absin(Time.time, 6f, 1f);
        float leng = (playerUnit != null && playerUnit.hitSize > 4 * 8f ? playerUnit.hitSize * 1.5f : 4 * 8f) +  sin;

        if(playerUnit == null) return;

        Draw.z(Layer.max);
        Tmp.v1.set(camera.position);
        Lines.stroke(1f + sin / 2, Pal.accent);
        Lines.circle(Tmp.v1.x, Tmp.v1.y, leng - 4f);
        spawner.getSpawns().each(t -> Drawf.arrow(Tmp.v1.x, Tmp.v1.y, t.worldx(), t.worldy(), leng, (Math.min(200 * 8f, Mathf.dst(Tmp.v1.x, Tmp.v1.y, t.worldx(), t.worldy())) / (200 * 8f)) * (5f +  sin)));
        Draw.color();
    }
}
