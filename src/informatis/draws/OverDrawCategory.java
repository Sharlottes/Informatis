package informatis.draws;

import arc.scene.style.Drawable;
import mindustry.gen.Icon;

public enum OverDrawCategory {
    Range("Range", Icon.commandRallySmall),
    Link("Link", Icon.lineSmall),
    Unit("Unit", Icon.unitsSmall),
    Block("Block", Icon.craftingSmall),
    Util("Util", Icon.githubSmall);

    public final String name;
    public final Drawable icon;
    public boolean enabled = false;

    OverDrawCategory(String name, Drawable icon) {
        this.name = name;
        this.icon = icon;
    }
}
