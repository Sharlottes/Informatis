package informatis.draws;

import informatis.ui.components.FreeBar;
import mindustry.gen.Unit;

public class UnitBarDraw extends OverDraw {
    public UnitBarDraw() {
        super("unitBar", OverDrawCategory.Unit);
    }

    @Override
    public void onUnit(Unit unit) {
        FreeBar.draw(unit);
    }
}
