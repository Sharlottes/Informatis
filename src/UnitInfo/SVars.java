package UnitInfo;

import UnitInfo.core.*;
import arc.files.*;
import arc.graphics.g2d.TextureRegion;
import arc.struct.*;

import static arc.Core.atlas;
import static mindustry.Vars.*;

public class SVars {
    public static Fi modRoot = modDirectory.child("UnitInfo");
    public static Seq<PlayerParser.PlayerInfo> playerInfos = new Seq<>();

    public static SettingS settingAdder = new SettingS();
    public static HudUi hud = new HudUi();
    public static PlayerParser playerinfo = new PlayerParser();

    public static TextureRegion clear = atlas.find("clear");
}
