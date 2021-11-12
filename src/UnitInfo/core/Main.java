package UnitInfo.core;

import UnitInfo.shaders.*;
import arc.*;
import arc.audio.Sound;
import arc.files.Fi;
import arc.input.KeyCode;
import arc.scene.ui.TextArea;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.Timer;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.gen.Sounds;
import mindustry.mod.*;
import mindustry.ui.dialogs.BaseDialog;

import static UnitInfo.SVars.*;
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

        Events.on(ClientLoadEvent.class, e -> {
            new SettingS().init();
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
