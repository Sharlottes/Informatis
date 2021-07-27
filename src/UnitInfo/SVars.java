package UnitInfo;

import UnitInfo.core.*;
import arc.files.*;
import arc.struct.*;

import static mindustry.Vars.*;

public class SVars {
    public static Fi modRoot = modDirectory.child("UnitInfo");
    public static Seq<PlayerParser.PlayerInfo> playerInfos = new Seq<>();

    public static Setting settingAdder = new Setting();
    public static HudUi hud = new HudUi();
    public static PlayerParser playerinfo = new PlayerParser();
}
