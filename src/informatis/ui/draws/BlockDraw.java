package informatis.ui.draws;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.scene.style.TextureRegionDrawable;
import mindustry.gen.Groups;
import mindustry.graphics.Pal;

import static informatis.SUtils.*;
import static arc.Core.settings;

public class BlockDraw extends OverDraw {
    BlockDraw(String name, TextureRegionDrawable icon) {
        super(name, icon);
        registerOption("blockStatus");
    }

    @Override
    public void draw() {
        super.draw();
        Groups.build.each(b->{
            if(isInCamera(b.x, b.y, b.block.size/2f) && settings.getBool("blockStatus") && enabled) {
                if(b.block.consumers.length > 0) {
                    float multiplier = b.block.size > 1 ? 1.0F : 0.64F;
                    float brcx = b.x + (float)(b.block.size * 8) / 2.0F - 8.0F * multiplier / 2.0F;
                    float brcy = b.y - (float)(b.block.size * 8) / 2.0F + 8.0F * multiplier / 2.0F;
                    Draw.z(71.0F);
                    Draw.color(Pal.gray);
                    Fill.square(brcx, brcy, 2.5F * multiplier, 45.0F);
                    Draw.color(b.status().color);
                    Fill.square(brcx, brcy, 1.5F * multiplier, 45.0F);
                    Draw.color();
                }
            };
        });
    }
}
