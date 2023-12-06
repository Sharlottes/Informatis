package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.ai.types.LogicAI;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.logic.LUnitControl;

public class LogicLineDraw extends OverDraw {
    public LogicLineDraw() {
        super("logicLine");
    }

    @Override
    public void onUnit(Unit unit) {
        if(unit.controller() instanceof LogicAI ai &&  ai.controller != null && (ai.control == LUnitControl.approach || ai.control == LUnitControl.move)) {
            Lines.stroke(1, unit.team.color);
            Lines.line(unit.x(), unit.y(), ai.moveX, ai.moveY);
            Lines.stroke(0.5f + Mathf.absin(6f, 0.5f), Tmp.c1.set(Pal.logicOperations).lerp(Pal.sap, Mathf.absin(6f, 0.5f)));
            Lines.line(unit.x(), unit.y(), ai.controller.x, ai.controller.y);
        }
    }
}
