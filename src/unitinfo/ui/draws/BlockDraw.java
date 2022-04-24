package unitinfo.ui.draws;

import arc.scene.style.TextureRegionDrawable;
import mindustry.gen.Groups;

import static unitinfo.SUtils.*;
import static arc.Core.settings;

public class BlockDraw extends OverDraw {
    boolean status = false;

    BlockDraw(String name, TextureRegionDrawable icon) {
        super(name, icon);
        registerOption("blockStatus");
    }

    @Override
    public void draw() {
        super.draw();
        Groups.build.each(b->{
            //this is shit.. VERY SHIT OH GOD
            if(isInCamera(b.x, b.y, b.block.size/2f) && settings.getBool("blockStatus") && enabled) b.drawStatus();
        });
    }
}
