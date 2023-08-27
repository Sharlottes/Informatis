package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.math.Angles;
import arc.scene.ui.layout.Scl;
import arc.util.Align;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;

import static informatis.SUtils.isInCamera;
import static mindustry.Vars.renderer;

public class UnitItemDraw extends OverDraw {
    public UnitItemDraw() {
        super("unitItem");
    }

    @Override
    public void onUnit(Unit unit) {
        if(isInCamera(unit.x, unit.y, unit.hitSize) && !renderer.pixelator.enabled() && unit.item() != null && unit.itemTime > 0.01f) {
            Fonts.outline.draw(String.valueOf(unit.stack.amount),
                unit.x + Angles.trnsx(unit.rotation + 180f, unit.type.itemOffsetY),
                unit.y + Angles.trnsy(unit.rotation + 180f, unit.type.itemOffsetY) - 3,
                Pal.accent, 0.25f * unit.itemTime / Scl.scl(1f), false, Align.center);
        }
    }
}
