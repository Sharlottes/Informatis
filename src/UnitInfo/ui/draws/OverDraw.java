package UnitInfo.ui.draws;

import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;

public class OverDraw {
    public TextureRegionDrawable icon;
    public String name;
    public boolean enabled =false;

    OverDraw(String name, TextureRegionDrawable icon) {
        this.name = name;
        this.icon = icon;
    }

    public void displayStats(Table parent) {}
    public void draw() {}
    public <T> void onEnabled(T param) {}
}

