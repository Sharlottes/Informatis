package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import informatis.ui.fragments.sidebar.windows.WindowManager;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;

import static arc.Core.settings;
import static informatis.SUtils.getTarget;
import static mindustry.Vars.*;

public class SelectArrawDraw extends OverDraw {
    public SelectArrawDraw() {
        super("select");
    }

    @Override
    public void draw() {
        Teamc target = getTarget();
        Draw.z(Layer.max);
        Draw.color(Tmp.c1.set(WindowManager.unitWindow.isLocked() ? Color.orange : Color.darkGray).lerp(WindowManager.unitWindow.isLocked() ? Color.scarlet : Color.gray, Mathf.absin(Time.time, 3f, 1f)).a(settings.getInt("selectopacity") / 100f));

        float length = (target instanceof Unit u
                ? u.hitSize
                : target instanceof Building b
                ? b.block.size * tilesize
                : 0
        ) * 1.5f + 2.5f;

        for(int i = 0; i < 4; i++){
            float rot = i * 90f + 45f + (-Time.time) % 360f;
            Draw.rect("select-arrow", target.x() + Angles.trnsx(rot, length), target.y() + Angles.trnsy(rot, length), length / 1.9f, length / 1.9f, rot - 135f);
        }

        Draw.color();
    }
}

