package UnitInfo.ui.draws;

import arc.scene.Element;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.CheckBox;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.ui.Styles;

import static UnitInfo.core.OverDrawer.isInCamera;

public class BlockDraw extends OverDraw {
    boolean status = false;

    BlockDraw(String name, TextureRegionDrawable icon) {
        super(name, icon);
    }

    @Override
    public void draw() {
        super.draw();
        Groups.build.each(b->{
            if(isInCamera(b.x, b.y, b.block.size/2f) && Vars.player.team() == b.team) b.drawStatus();
        });
    }

    @Override
    public void displayStats(Table parent) {
        super.displayStats(parent);

        parent.background(Styles.squaret.up);

        parent.check("enable block status", status&&enabled, b->status=b&&enabled).disabled(!enabled);
    }

    @Override
    public <T> void onEnabled(T param) {
        super.onEnabled(param);

        if(param instanceof Table t) {
            for (int i = 0; i < t.getChildren().size; i++) {
                Element elem = t.getChildren().get(i);
                if (elem instanceof CheckBox cb) cb.setDisabled(!enabled);
            }
        }
    }
}
