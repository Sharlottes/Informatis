package unitinfo.core;

import arc.input.KeyCode;
import arc.scene.ui.layout.Table;
import unitinfo.shaders.*;
import unitinfo.ui.*;
import unitinfo.ui.draws.OverDraws;
import unitinfo.ui.windows.*;
import arc.*;
import arc.struct.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

import static unitinfo.SVars.*;
import static arc.Core.*;
import static mindustry.Vars.*;
import static unitinfo.SUtils.*;
import static unitinfo.ui.windows.WindowManager.windows;

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

        Events.run(Trigger.update, () -> {
            target = getTarget();

            for (Window window : windows) {
                if(window instanceof Updatable u) u.update();
            }
            if((input.keyDown(KeyCode.shiftRight) || input.keyDown(KeyCode.shiftLeft))) {
                if(input.keyTap(KeyCode.r)) lockTarget();
            }
        });

        Events.on(ClientLoadEvent.class, e -> {
            Windows.load();
            SettingS.init();
            WindowManager.init();
            OverDraws.init();
            OverDrawer.setEvent();

            Seq.with(scene.root,
                ui.picker, ui.editor, ui.controls, ui.restart, ui.join, ui.discord,
                ui.load, ui.custom, ui.language, ui.database, ui.settings, ui.host,
                ui.paused, ui.about, ui.bans, ui.admins, ui.traces, ui.maps, ui.content,
                ui.planet, ui.research, ui.mods, ui.schematics, ui.logic
            ).each(dialog-> dialog.addChild(new ElementDisplay(dialog)));

            Table table = ((Table) scene.find("minimap/position")).row();
            table.add(new SchemDisplay());
            new WaveInfoDisplay().addWaveInfoTable();

            if(jsonGen) ContentJSON.save();
        });
    }

    public static void lockTarget() {
        if(target==getTarget()) locked = !locked;
        target = getTarget();
    }
}
