package UnitInfo.core;

import UnitInfo.shaders.*;
import UnitInfo.ui.HUDFragment;
import UnitInfo.ui.MindowsTex;
import UnitInfo.ui.UnitDisplay;
import UnitInfo.ui.WindowTables;
import arc.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

import static UnitInfo.SVars.*;
import static UnitInfo.ui.UnitDisplay.getTarget;
import static arc.Core.*;

public class Main extends Mod {

    @Override
    public void init(){
        turretRange = new RangeShader();
        lineShader = new LineShader();

        Core.app.post(() -> {
            Mods.ModMeta meta = Vars.mods.locateMod("unitinfo").meta;
            meta.displayName = "[#B5FFD9]Unit Information[]";
            meta.author = "[#B5FFD9]Sharlotte[lightgray]#0018[][]";
            meta.description = bundle.get("shar-description");
        });

        Events.run(Trigger.class, () -> {
            try {
                BarInfo.getInfo(getTarget());
            } catch (IllegalAccessException | NoSuchFieldException err) {
                err.printStackTrace();
            }
        });

        Events.on(ClientLoadEvent.class, e -> {
            new SettingS().init();
            MindowsTex.init();
            new HUDFragment().build(Vars.ui.hudGroup);
            hud = new HudUi();
            hud.addTable();
            hud.addWaveInfoTable();
            hud.addSchemTable();
            hud.setEvents();
            OverDrawer.setEvent();
            if(jsonGen) ContentJSON.save();
        });
    }
}
