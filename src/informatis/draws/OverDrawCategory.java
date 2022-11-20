package informatis.draws;

import arc.scene.style.Drawable;
import mindustry.gen.Icon;

public enum OverDrawCategory {
    Range("Range", Icon.commandRally),
    Link("Link", Icon.line),
    Unit("Unit", Icon.units),
    Block("Block", Icon.crafting),
    Util("Util", Icon.github);

    public final String name;
    public final Drawable icon;
    public boolean enabled = false;

    OverDrawCategory(String name, Drawable icon) {
        this.name = name;
        this.icon = icon;
    }
}
