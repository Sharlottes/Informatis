package UnitInfo;

import UnitInfo.core.*;
import UnitInfo.shaders.LineShader;
import UnitInfo.shaders.RangeShader;
import arc.graphics.g2d.TextureRegion;

import static arc.Core.atlas;
import static arc.Core.settings;

public class SVars {
    public static HudUi hud = new HudUi();
    public static float modUiScale = settings.getInt("infoUiScale");
    public static boolean pathLine = false, unitLine = false, logicLine = false;
    public static TextureRegion clear = atlas.find("clear");
    public static TextureRegion error = atlas.find("error");
    public static RangeShader turretRange;
    public static LineShader lineShader;
    public static boolean jsonGen = true;
}
