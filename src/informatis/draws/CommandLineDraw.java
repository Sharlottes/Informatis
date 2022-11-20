package informatis.draws;

import arc.graphics.g2d.Lines;
import mindustry.ai.types.CommandAI;
import mindustry.gen.Unit;

public class CommandLineDraw extends OverDraw {
    public CommandLineDraw() {
        super("commandLine", OverDrawCategory.Unit);
    }

    @Override
    public void onUnit(Unit unit) {
        if(unit.controller() instanceof CommandAI com && com.hasCommand()) {
            Lines.stroke(1, unit.team.color);
            Lines.line(unit.x(), unit.y(), com.targetPos.x, com.targetPos.y);
        }
    }
}
