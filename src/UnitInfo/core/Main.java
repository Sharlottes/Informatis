package UnitInfo.core;

import UnitInfo.shaders.*;
import UnitInfo.ui.*;
import UnitInfo.ui.draws.OverDraws;
import UnitInfo.ui.windows.*;
import arc.*;
import arc.scene.ui.Dialog;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

import static UnitInfo.SVars.*;
import static UnitInfo.ui.windows.UnitDisplay.getTarget;
import static arc.Core.*;
import static mindustry.Vars.mobile;
import static mindustry.Vars.ui;

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
            SettingS.init();
            MindowsTex.init();
            WindowTables.init();
            OverDraws.init();

            new HUDFragment().build(Vars.ui.hudGroup);
            hud = new HudUi();
            hud.addWaveInfoTable();
            hud.addSchemTable();
            hud.setEvents();
            OverDrawer.setEvent();
            if(jsonGen) ContentJSON.save();

            scene.add(new ElementDisplay());
            Seq.with(
                ui.picker, ui.editor, ui.controls, ui.restart, ui.join, ui.discord,
                ui.load, ui.custom, ui.language, ui.database, ui.settings, ui.host,
                ui.paused, ui.about, ui.bans, ui.admins, ui.traces, ui.maps, ui.content,
                ui.planet, ui.research, ui.mods, ui.schematics, ui.logic
            ).each(dialog-> dialog.add(new ElementDisplay(dialog)));


        });

        Events.on(WorldLoadEvent.class, e -> {
            ((CoreDisplay) WindowTables.coreTable).resetUsed();
        });
    }
}
