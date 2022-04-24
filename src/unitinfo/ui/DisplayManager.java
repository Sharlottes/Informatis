package unitinfo.ui;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import unitinfo.ui.display.ElementDisplay;
import unitinfo.ui.display.SchemDisplay;
import unitinfo.ui.display.WaveInfoDisplay;

import static arc.Core.scene;
import static mindustry.Vars.ui;

public class DisplayManager {
    public static void init() {
        //layout debug
        Seq.with(scene.root,
                ui.picker, ui.editor, ui.controls, ui.restart, ui.join, ui.discord,
                ui.load, ui.custom, ui.language, ui.database, ui.settings, ui.host,
                ui.paused, ui.about, ui.bans, ui.admins, ui.traces, ui.maps, ui.content,
                ui.planet, ui.research, ui.mods, ui.schematics, ui.logic
        ).each(dialog-> dialog.addChild(new ElementDisplay(dialog)));

        //schem quick-slot
        Table table = ((Table) scene.find("minimap/position")).row();
        table.add(new SchemDisplay());
        new WaveInfoDisplay().addWaveInfoTable();

    }
}
