package UnitInfo;

import UnitInfo.core.HudUi;
import UnitInfo.core.PlayerParser;
import UnitInfo.core.Setting;
import arc.Core;
import arc.files.Fi;
import arc.struct.Seq;

public class SVars {
    public static Fi modRoot = Core.settings.getDataDirectory().child("mods/UnitInfo");
    public static Seq<PlayerParser.PlayerInfo> playerInfos = new Seq<>();

    public static Setting settingAdder = new Setting();
    public static HudUi hud = new HudUi();
    public static PlayerParser playerinfo = new PlayerParser();
}
