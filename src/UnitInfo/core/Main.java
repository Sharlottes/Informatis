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
            hud.addWaveTable();
            hud.addUnitTable();
            hud.addTable();
            hud.addWaveInfoTable();
            hud.addSchemTable();
            hud.setEvents();
            OverDrawer.setEvent();
            if(jsonGen) ContentJSON.save();
        });

        Events.on(WorldLoadEvent.class, e -> {
            hud = new HudUi();
            hud.addWaveTable();
        });

        Events.on(WaveEvent.class, e -> {
            Vars.ui.hudGroup.removeChild(hud.waveTable);
            hud = new HudUi();
            hud.addWaveTable();
        });

        Events.run(Trigger.update, () -> {
            if((input.keyDown(KeyCode.shiftRight) || input.keyDown(KeyCode.shiftLeft))){
                if(input.keyTap(KeyCode.h)) {
                    mmid_playMusicSeq(mmid_parseMusicString(Fi.get("C:/Users/user/Desktop/test/output.txt").readString()), null);
                };
                if(input.keyTap(KeyCode.c)) {
                    schedules.each(Timer.Task::cancel);
                }
            }
        });
    }

    Seq<Float[]> mmid_parseMusicString(String s) {
        String[] notes = s.split(";");
        Seq<Float[]> output = new Seq();
        for (String value : notes) {
            String[] note = value.split(",");
            if (note.length > 0) {
                output.add(new Float[]{
                        Float.parseFloat(note[0]),
                        (note.length < 2 || note[1] == null) ? 0 : Float.parseFloat(note[1]),
                        (note.length < 3 || note[2] == null) ? 0 : Float.parseFloat(note[2]),
                        (note.length < 4 || note[3] == null) ? 1 : Float.parseFloat(note[3]),
                        (note.length < 5 || note[4] == null) ? 0 : Float.parseFloat(note[4])
                });
            }
        }
        Log.info(output);
        return output;
    };

    Seq<Timer.Task> schedules = new Seq<>();
    void mmid_playMusicSeq(Seq<Float[]> s, @Nullable Player p) {
        Object[][] mmid_instruments = { //Sound, pitch, volume
                {Sounds.minebeam, 0.98f, 20f},
                {Sounds.minebeam, 2.2f, 0.5f}};

        s.each(n-> {
            schedules.add(Timer.schedule(() -> {
                Log.info(mmid_instruments[n[2].intValue()][0].toString() + " sound is called");
                if(p == null || p.con == null) Call.sound(
                        (Sound)mmid_instruments[n[2].intValue()][0],
                        n[3]*(float)mmid_instruments[n[2].intValue()][2],
                        (float)mmid_instruments[n[2].intValue()][1]*(float)Math.pow(1.0595,n[1]), n[4]);
                else Call.sound(p.con,
                        (Sound)mmid_instruments[n[2].intValue()][0],
                        n[3]*(float)mmid_instruments[n[2].intValue()][2],
                        (float)mmid_instruments[n[2].intValue()][1]*1f*(float)Math.pow(1.0595,n[1]), n[4]);
            },n[0]));
            Log.info("start sound after" + n[0] + "sec");
        });
    }
}
