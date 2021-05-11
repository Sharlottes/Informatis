package UnitInfo.core;

import arc.Core;
import arc.Input;
import arc.scene.ui.Label;
import arc.scene.ui.SettingsDialog;
import arc.scene.ui.TextArea;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;

import java.util.Objects;

import static mindustry.Vars.ui;

public class Setting {
    public void addGraphicSetting(String key){
        ui.settings.graphics.checkPref(key, Core.settings.getBool(key));
    }

    public void init(){
        boolean tmp = Core.settings.getBool("uiscalechanged", false);
        Core.settings.put("uiscalechanged", false);

        addGraphicSetting("coreui");
        addGraphicSetting("waveui");
        addGraphicSetting("unitui");
        addGraphicSetting("weaponui");
        addGraphicSetting("commandedunitui");
        addGraphicSetting("unithealthui");
        SettingsDialog.SettingsTable.Setting waveSetting = new SettingsDialog.SettingsTable.Setting() {
            public int def;
            {
                def = 100;
                name = "wavemax";
                title = Core.bundle.get("setting.wavemax.name");

                Core.settings.defaults(name, def);
            }

            public final StringBuilder message = new StringBuilder();

            @Override
            public void add(SettingsDialog.SettingsTable settingsTable) {
                Label label = new Label(title + ": " + def);

                Table button = new Table(t -> t.button(Icon.pencil, () -> {
                    if(Vars.mobile){
                        Core.input.getTextInput(new Input.TextInput(){{
                            text = message.toString();
                            multiline = false;
                            maxLength = String.valueOf(Integer.MAX_VALUE).length();
                            accepted = str -> {

                                try {
                                    int number = 0;
                                    if(!str.isEmpty() || !(Objects.equals(str, ""))) number = Integer.parseInt(str);
                                    Core.settings.put(name, number);
                                    label.setText(title + ": " + number);
                                } catch(Throwable e) {
                                    Log.info(e);
                                    ui.showErrorMessage("@invalid");

                                    Core.settings.put(name, def);
                                    label.setText(title + ": " + def);
                                }
                            };
                        }});
                    }else{
                        BaseDialog dialog = new BaseDialog("@editmaxwave");
                        dialog.setFillParent(false);
                        TextArea a = dialog.cont.add(new TextArea(message.toString().replace("\r", "\n"))).size(140f, 80f).get();
                        a.setMaxLength(String.valueOf(Integer.MAX_VALUE).length());
                        dialog.buttons.button("@ok", () -> {
                            try {
                                Core.settings.put(name, Integer.parseInt(a.getText()));
                                label.setText(title + ": " + Integer.parseInt(a.getText()));
                            } catch(Throwable e) {
                                Log.info(e);
                                ui.showErrorMessage("@invalid");

                                Core.settings.put(name, def);
                                label.setText(title + ": " + def);
                            }

                            dialog.hide();
                        }).size(70f, 50f);

                        dialog.show();
                    }
                }).size(40f));

                settingsTable.table((t) -> {
                    t.left().defaults().left();
                    t.add(label).minWidth(label.getPrefWidth() / Scl.scl(1.0F) + 50.0F);
                    t.add(button).size(40F);
                }).left().padTop(3.0F);
                settingsTable.row();
            }
        };

        ui.settings.graphics.pref(waveSetting);
        ui.settings.graphics.sliderPref("coreuiopacity", 50, 0, 100, 5, s -> s + "%");
        ui.settings.graphics.sliderPref("waveuiopacity", 50, 0, 100, 5, s -> s + "%");
        ui.settings.graphics.sliderPref("uiopacity", 50, 0, 100, 5, s -> s + "%");
        ui.settings.graphics.sliderPref("baropacity", 100, 0, 100, 5, s -> s + "%");

        Core.settings.defaults("coreui", !Vars.mobile);
        Core.settings.defaults("waveui", true);
        Core.settings.defaults("unitui", true);
        Core.settings.defaults("weaponui", true);
        Core.settings.defaults("commandedunitui", true);
        Core.settings.defaults("unithealthui", true);


        Core.settings.put("uiscalechanged", tmp);
    }
}
