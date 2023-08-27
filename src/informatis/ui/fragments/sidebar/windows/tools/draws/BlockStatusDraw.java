package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Pal;

import static informatis.SUtils.isInCamera;

public class BlockStatusDraw extends OverDraw {
    public BlockStatusDraw() {
        super("blockstatus");
    }
    @Override
    public void onBuilding(Building build) {
        if(!isInCamera(build.x, build.y, build.block.size/2f)) return;
        if(build.team != Vars.player.team() && build.block.consumers.length > 0) {
            float multiplier = build.block.size > 1 ? 1.0F : 0.64F;
            float brcx = build.x + (float)(build.block.size * 8) / 2.0F - 8.0F * multiplier / 2.0F;
            float brcy = build.y - (float)(build.block.size * 8) / 2.0F + 8.0F * multiplier / 2.0F;
            Draw.z(71.0F);
            Draw.color(Pal.gray);
            Fill.square(brcx, brcy, 2.5F * multiplier, 45.0F);
            Draw.color(build.status().color);
            Fill.square(brcx, brcy, 1.5F * multiplier, 45.0F);
            Draw.color();
        }
    }
}
