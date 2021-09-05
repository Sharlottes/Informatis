package UnitInfo;

import UnitInfo.core.*;
import arc.Core;
import arc.files.*;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Rect;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import mindustry.world.Tile;

import static arc.Core.atlas;
import static arc.Core.settings;
import static mindustry.Vars.*;

public class SVars {
    public static HudUi hud = new HudUi();
    public static float modUiScale = settings.getInt("infoUiScale") / 100f == 0 ? 1 : settings.getInt("infoUiScale") / 100f;
    public static boolean pathLine = false, unitLine = false, logicLine = false;
    public static TextureRegion clear = atlas.find("clear");
    public static TextureRegion error = atlas.find("error");

    public static boolean jsonGen = false;
}
