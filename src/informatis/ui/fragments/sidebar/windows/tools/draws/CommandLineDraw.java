package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.graphics.g2d.Lines;
import mindustry.ai.types.CommandAI;
import mindustry.gen.Unit;

public class CommandLineDraw extends OverDraw {
    public CommandLineDraw() {
        super("commandLine");
    }

    @Override
    public void onUnit(Unit unit) {
        if(unit.controller() instanceof CommandAI com && com.targetPos != null) {
            Lines.stroke(1, unit.team.color);
            Lines.line(unit.x(), unit.y(), com.targetPos.x, com.targetPos.y);
        }
    }
}
