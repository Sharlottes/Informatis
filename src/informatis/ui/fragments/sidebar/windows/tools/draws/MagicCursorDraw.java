package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.Core;
import arc.graphics.Color;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;

import static mindustry.Vars.mobile;

public class MagicCursorDraw extends OverDraw {
    public MagicCursorDraw() {
        super("gaycursor");
    }

    @Override
    public void draw() {
        if(!mobile && !Vars.state.isPaused()) {
            Fx.mine.at(Core.input.mouseWorldX(), Core.input.mouseWorldY(), Tmp.c2.set(Color.red).shiftHue(Time.time * 1.5f));
        }
    }
}
