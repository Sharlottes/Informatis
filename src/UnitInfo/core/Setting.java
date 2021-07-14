package UnitInfo.core;

import arc.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

import static mindustry.Vars.*;

public class Setting {
    public void addGraphicSetting(String key){
        ui.settings.graphics.checkPref(key, Core.settings.getBool(key));
    }
    public void addGraphicTypeSetting(String key, int defs, String dialogs, String invalid, int warnMax){
        ui.settings.graphics.pref(new SettingsMenuDialog.SettingsTable.Setting() {
            public final int def;
            {
                def = defs;
                name = key;
                title = Core.bundle.get("setting." + key + ".name", key);

                Core.settings.defaults(name, def);
            }

            public final StringBuilder message = new StringBuilder();

            @Override
            public void add(SettingsMenuDialog.SettingsTable settingsTable) {
                String settingTitle = title;
                String settingName = name;
                Label label = new Label(title + ": " + def);

                Table button = new Table(t -> t.button(Icon.pencil, () -> {
                    if(Vars.mobile){
                        Core.input.getTextInput(new Input.TextInput(){{
                            text = message.toString();
                            multiline = false;
                            maxLength = String.valueOf(Integer.MAX_VALUE).length();
                            accepted = str -> {

                                try {
                                    int number = Integer.parseInt(str);
                                    if(number >= warnMax){
                                        new Dialog(""){{
                                            setFillParent(true);
                                            cont.margin(15f);
                                            cont.add("@warn");
                                            cont.row();
                                            cont.image().width(300f).pad(2).height(4f).color(Color.scarlet);
                                            cont.row();
                                            cont.add("@warning").pad(2f).growX().wrap().get().setAlignment(Align.center);
                                            cont.row();
                                            cont.table(t -> {
                                                t.button("@yes", () -> {
                                                    this.hide();
                                                    Core.settings.put(settingName, number);
                                                    label.setText(settingTitle + ": " + number);
                                                }).size(120, 50);
                                                t.button("@no", () -> {
                                                    this.hide();
                                                    Core.settings.put(settingName, def);
                                                    label.setText(settingTitle + ": " + Core.settings.getInt(settingName));
                                                }).size(120, 50);
                                            }).pad(5);
                                            closeOnBack();
                                        }}.show();
                                    }
                                    else {
                                        Core.settings.put(settingName, number);
                                        label.setText(settingTitle + ": " + number);
                                    }
                                } catch(Throwable e) {
                                    Log.info(e);
                                    ui.showErrorMessage("@invalid");

                                    Core.settings.put(settingName, def);
                                    label.setText(settingTitle + ": " + def);
                                }
                            };
                        }});
                    }else{
                        BaseDialog dialog = new BaseDialog(dialogs);
                        dialog.setFillParent(false);
                        TextArea a = dialog.cont.add(new TextArea(message.toString().replace("\r", "\n"))).size(140f, 80f).get();
                        a.setMaxLength(String.valueOf(Integer.MAX_VALUE).length());
                        dialog.buttons.button("@ok", () -> {
                            try {
                                int number = Integer.parseInt(a.getText());
                                if(number >= warnMax){
                                    String name1 = name;
                                    String title1 = title;
                                    new Dialog(""){{
                                        setFillParent(true);
                                        cont.margin(15f);
                                        cont.add("@warn");
                                        cont.row();
                                        cont.image().width(300f).pad(2).height(4f).color(Color.scarlet);
                                        cont.row();
                                        cont.add("@warning").pad(2f).growX().wrap().get().setAlignment(Align.center);
                                        cont.row();
                                        cont.table(t -> {
                                            t.button("@yes", () -> {
                                                this.hide();
                                                Core.settings.put(name1, number);
                                                label.setText(title1 + ": " + number);
                                            }).size(120, 50);
                                            t.button("@no", () -> {
                                                this.hide();
                                                Core.settings.put(name1, def);
                                                label.setText(title1 + ": " + Core.settings.getInt(name1));
                                            }).size(120, 50);
                                        }).pad(5);
                                        closeOnBack();
                                    }}.show();
                                }else {
                                    Core.settings.put(name, number);
                                    label.setText(title + ": " + number);
                                }
                            } catch(Throwable e) {
                                Log.info(e);
                                ui.showErrorMessage(invalid);

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
        });
    }

    public void init(){
        boolean tmp = Core.settings.getBool("uiscalechanged", false);
        Core.settings.put("uiscalechanged", false);

        addGraphicSetting("pastwave");
        addGraphicSetting("emptywave");
        addGraphicSetting("ssim");
        addGraphicSetting("gaycursor");
        addGraphicSetting("panfix");
        addGraphicSetting("scan");
        addGraphicSetting("range");
        addGraphicSetting("select");
        addGraphicSetting("infoui");
        addGraphicSetting("weaponui");
        addGraphicSetting("commandedunitui");
        addGraphicSetting("unithealthui");
        addGraphicTypeSetting("wavemax", 100, "@editmaxwave","@invalid", 200);
        addGraphicTypeSetting("rangemax", 10, "@editrange","@invalid", 100);

        ui.settings.graphics.sliderPref("selectopacity", 25, 0, 100, 5, s -> s + "%");
        ui.settings.graphics.sliderPref("baropacity", 100, 0, 100, 5, s -> s + "%");
        ui.settings.graphics.sliderPref("uiopacity", 50, 0, 100, 5, s -> s + "%");

        Core.settings.defaults("pastwave", false);
        Core.settings.defaults("emptywave", true);
        Core.settings.defaults("ssim", false);
        Core.settings.defaults("select", false);
        Core.settings.defaults("gaycursor", true);
        Core.settings.defaults("panfix", false);
        Core.settings.defaults("scan", false);
        Core.settings.defaults("range", false);
        Core.settings.defaults("infoui", true);
        Core.settings.defaults("weaponui", true);
        Core.settings.defaults("commandedunitui", true);
        Core.settings.defaults("unithealthui", true);

        Core.settings.put("uiscalechanged", tmp);
    }
}
