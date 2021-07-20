package UnitInfo.core;

import arc.*;
import arc.graphics.*;
import arc.scene.event.Touchable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.Seq;
import arc.util.*;
import mindustry.*;
import mindustry.core.Version;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.*;

import java.util.Iterator;

import static arc.Core.bundle;
import static arc.Core.settings;
import static mindustry.Vars.*;

public class Setting {
    public static SettingsMenuDialog.SettingsTable waveTable = new SettingsMenuDialog.SettingsTable();
    public static SettingsMenuDialog.SettingsTable opacityTable = new SettingsMenuDialog.SettingsTable();
    public static SettingsMenuDialog.SettingsTable scanTable = new SettingsMenuDialog.SettingsTable();

    public void addGraphicCheckSetting(String key, boolean def){
        ui.settings.graphics.checkPref(key, def);
    }
    public void addGraphicSlideSetting(String key, int def, int min, int max, int step, SettingsMenuDialog.StringProcessor s, Seq<SettingsMenuDialog.SettingsTable.Setting> list){
        list.add(new SettingsMenuDialog.SettingsTable.Setting() {
            {
                name = key;
                title = bundle.get("setting." + key + ".name");

                Core.settings.defaults(name, def);
            }

            @Override
            public void add(SettingsMenuDialog.SettingsTable table){
                Slider slider = new Slider(min, max, step, false);

                if(Version.build <= 128) slider.addListener(new Tooltip(t -> t.background(Tex.button).table(to -> to.add("[lightgray]" + Core.bundle.get("setting." + key + ".description") + "[]"))));
                slider.setValue(settings.getInt(name));

                Label value = new Label("");
                value.setStyle(Styles.outlineLabel);
                value.touchable = Touchable.disabled;

                slider.changed(() -> {
                    settings.put(name, (int)slider.getValue());
                    value.setText(title + ": " + s.get((int)slider.getValue()));
                });

                value.setAlignment(Align.center);
                value.setWrap(true);

                slider.change();

                table.stack(slider, value).width(Math.min(Core.graphics.getWidth() / 1.2f, 460f)).left().padTop(4);
                table.row();
            }
        });
    }
    public void addGraphicCheckSetting(String key, boolean def, Seq<SettingsMenuDialog.SettingsTable.Setting> list){
        list.add(new SettingsMenuDialog.SettingsTable.Setting() {
            {
                name = key;
                title = bundle.get("setting." + key + ".name");

                Core.settings.defaults(name, def);
            }

            @Override
            public void add(SettingsMenuDialog.SettingsTable table) {
                CheckBox box = new CheckBox(title);
                if(Version.build <= 128) box.addListener(new Tooltip(t -> t.background(Tex.button).table(to -> to.add("[lightgray]" + Core.bundle.get("setting." + key + ".description") + "[]"))));

                box.update(() -> box.setChecked(settings.getBool(name)));

                box.changed(() -> {
                    settings.put(name, box.isChecked());
                });

                box.left();
                table.add(box).left().padTop(3f);
                table.row();
            }
        });
    }

    public void addGraphicTypeSetting(String key, int defs, String dialogs, String invalid, int warnMax, Seq<SettingsMenuDialog.SettingsTable.Setting> list){
        list.add(new SettingsMenuDialog.SettingsTable.Setting() {
            public final int def;
            {
                def = defs;
                name = key;
                title = Core.bundle.get("setting." + key + ".name");

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

                settingsTable.table(t -> {
                    t.left().defaults().left();
                    t.add(label).minWidth(label.getPrefWidth() / Scl.scl(1.0F) + 50.0F);
                    t.add(button).size(40F);
                    if(Version.build <= 128) t.addListener(new Tooltip(tt -> tt.background(Tex.button).table(to -> to.add("[lightgray]" + Core.bundle.get("setting." + key + ".description") + "[]"))));
                }).left().padTop(3.0F);
                settingsTable.row();
            }
        });
    }

    public void addGraphicDialogSetting(String key, Seq<SettingsMenuDialog.SettingsTable.Setting> list, SettingsMenuDialog.SettingsTable table){
    ui.settings.graphics.pref(new SettingsMenuDialog.SettingsTable.Setting() {
            {
                name = key;
                title = Core.bundle.get("setting." + key + ".name");

                Core.settings.defaults(name, 1);
            }

            public Table rebuild() {
                table.clearChildren();

                Iterator var1 = list.iterator();

                while(var1.hasNext()) {
                    SettingsMenuDialog.SettingsTable.Setting setting = (SettingsMenuDialog.SettingsTable.Setting)var1.next();
                    setting.add(table);
                }
                table.button(Core.bundle.get("settings.reset", "Reset to Defaults"), () -> {
                    Iterator var2 = list.iterator();

                    while(var2.hasNext()) {
                        SettingsMenuDialog.SettingsTable.Setting setting = (SettingsMenuDialog.SettingsTable.Setting)var1.next();
                        if (setting.name != null && setting.title != null) {
                            Core.settings.put(setting.name, Core.settings.getDefault(setting.name));
                        }
                    }
                    rebuild();
                }).margin(14.0F).width(240.0F).pad(6.0F);

                return table;
            }

            @Override
            public void add(SettingsMenuDialog.SettingsTable settingsTable) {
                settingsTable.table(Core.scene.getStyle(Button.ButtonStyle.class).up, t->{
                    t.add(rebuild());
                    t.row();
                });
                settingsTable.row();
            }
        });
    }

    public void init(){
        Seq<SettingsMenuDialog.SettingsTable.Setting> waveSeq = new Seq<>();
        addGraphicCheckSetting("pastwave", false, waveSeq);
        addGraphicCheckSetting("emptywave", true, waveSeq);
        addGraphicTypeSetting("wavemax", 100, "@editmaxwave","@invalid", 200, waveSeq);
        addGraphicDialogSetting("wavesetting", waveSeq, waveTable);

        Seq<SettingsMenuDialog.SettingsTable.Setting> scanSeq = new Seq<>();
        addGraphicCheckSetting("scan", false, scanSeq);
        addGraphicCheckSetting("range", false, scanSeq);
        addGraphicTypeSetting("rangemax", 10, "@editrange","@invalid", 100, scanSeq);
        addGraphicDialogSetting("wavesetting", scanSeq, scanTable);

        Seq<SettingsMenuDialog.SettingsTable.Setting> opacitySeq = new Seq<>();
        addGraphicSlideSetting("selectopacity", 25, 0, 100, 5, s -> s + "%", opacitySeq);
        addGraphicSlideSetting("baropacity", 100, 0, 100, 5, s -> s + "%", opacitySeq);
        addGraphicSlideSetting("uiopacity", 50, 0, 100, 5, s -> s + "%", opacitySeq);
        addGraphicDialogSetting("opacitysetting", opacitySeq, opacityTable);

        addGraphicCheckSetting("infoui", true);
        addGraphicCheckSetting("weaponui", true);
        addGraphicCheckSetting("select", false);
        addGraphicCheckSetting("unithealthui", true);
        addGraphicCheckSetting("ssim", false);
        addGraphicCheckSetting("shar", false);
        addGraphicCheckSetting("gaycursor", false);
    }
}
