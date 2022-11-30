package informatis.draws;

import arc.Events;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.game.EventType;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;

import static arc.Core.camera;
import static mindustry.Vars.player;
import static mindustry.Vars.spawner;

public class InfoRingDraw extends OverDraw {
    public InfoRingDraw() {
        super("infoRing", OverDrawCategory.Util);

        Events.on(EventType.UnitCreateEvent.class, ev -> {

        });
    }

    @Override
    public void draw() {
        super.draw();
        float sin = Mathf.absin(Time.time, 6f, 1f);
        float leng = (player.unit() != null && player.unit().hitSize > 4 * 8f ? player.unit().hitSize * 1.5f : 4 * 8f) +  sin;
        Tmp.v1.set(camera.position);
        Lines.stroke(1f + sin / 2, Pal.accent);
        Lines.circle(Tmp.v1.x, Tmp.v1.y, leng - 4f);
        spawner.getSpawns().each(t ->
                Drawf.arrow(
                        Tmp.v1.x, Tmp.v1.y,
                        t.worldx(), t.worldy(),
                        leng,
                        (Math.min(200 * 8f, Mathf.dst(Tmp.v1.x, Tmp.v1.y, t.worldx(), t.worldy())) / (200 * 8f)) * (5f +  sin)
                )
        );
    }
}
