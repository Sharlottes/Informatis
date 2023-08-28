package informatis.core;

import arc.Core;
import mindustry.Vars;
import mindustry.mod.Mods;

public class ModMetadata {
    public static Mods.ModMeta metadata;

    public static void init() {
        Core.app.post(() -> {
            Mods.ModMeta meta = Vars.mods.locateMod("informatis").meta;
            meta.displayName = "[#B5FFD9]Informatis[]";
            meta.author = "[#B5FFD9]Sharlotte[lightgray]#0018[][]";
            meta.description = Core.bundle.get("shar-description");
            metadata = meta;
        });
    }
}
