package informatis.ui.fragments.sidebar.windows.tools.draws;

import informatis.ui.components.FreeBar;
import mindustry.gen.Unit;

public class UnitBarDraw extends OverDraw {
    public UnitBarDraw() {
        super("unitBar");
    }

    @Override
    public void onUnit(Unit unit) {
        FreeBar.draw(unit);
    }
}
