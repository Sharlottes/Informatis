package informatis.core;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.Group;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.ui.layout.Stack;
import arc.struct.*;
import arc.util.*;
import informatis.ui.fragments.FragmentManager;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Setting {
    public static SettingsMenuDialog.SettingsTable sharset;

    public static void addGraphicCheckSetting(String key, boolean def, Seq<SharSetting> list) {
        addGraphicCheckSetting(key, def, list, () -> {});
    }
    public static void addGraphicCheckSetting(String key, boolean def, Seq<SharSetting> list, Runnable onSetted) {
        list.add(new SharSetting(key, def) {

            @Override
            public void add(Table table) {
                CheckBox box = new CheckBox(title);
                box.update(() -> box.setChecked(settings.getBool(name)));
                box.changed(() -> {
                    settings.put(name, box.isChecked());
                    onSetted.run();
                });

                box.left();
                addDesc(table.add(box).left().padTop(3f).get());
                table.row();
            }
        });
    }

    public static void addGraphicSlideSetting(String key, int def, int min, int max, int step, SettingsMenuDialog.StringProcessor sp, Seq<SharSetting> list){
        list.add(new SharSetting(key, def) {

            @Override
            public void add(Table table){

                Label value = new Label("", Styles.outlineLabel);
                Table content = new Table();
                content.add(title, Styles.outlineLabel).left().growX().wrap();
                content.add(value).padLeft(10f).right();
                content.margin(3f, 33f, 3f, 33f);
                content.touchable = Touchable.disabled;

                Slider slider = new Slider(min, max, step, false);
                slider.setValue(settings.getInt(name));
                slider.changed(() -> {
                    settings.put(name, (int)slider.getValue());
                    value.setText(sp.get((int)slider.getValue()));
                });
                slider.change();

                addDesc(table.stack(slider, content).width(Math.min(Core.graphics.getWidth() / 1.2f, 460f)).left().padTop(4f).get());
                table.row();
            }
        });
    }

    public static void addGraphicTypeSetting(String key, float min, float max, int def, boolean integer, Boolp condition, Func<String, String> h, Seq<SharSetting> list){
        list.add(new SharSetting(key, def) {

            @Override
            public void add(Table table) {
                final String[] str = {""};
                Table table1 = new Table(t -> {
                    final float[] value = {def};
                    t.add(new Label(title + ": ")).left().padRight(5)
                            .update(a -> a.setColor(condition.get() ? Color.white : Color.gray));

                    t.field((integer ? String.valueOf(value[0]).split("[.]")[0] : value[0]) + str[0], s -> {
                                str[0] = h.get(s);
                                value[0] = s.isEmpty() ? def : Strings.parseFloat(s);

                                if(integer) settings.put(key, (int)value[0]);
                                else settings.put(key, value[0]);

                            }).update(a -> a.setDisabled(!condition.get()))
                            .valid(f -> Strings.canParsePositiveFloat(f) && Strings.parseFloat(f) >= min && Strings.parseFloat(f) <= max).width(120f).left();
                });

                addDesc(table.add(table1).left().padTop(4f).get());
                table.row();
            }
        });
    }

    public static void init(){
        BaseDialog dialog = new BaseDialog(bundle.get("setting.shar-title"));
        dialog.addCloseButton();
        sharset = new SettingsMenuDialog.SettingsTable();
        dialog.cont.center().add(new Table(t -> t.pane(sharset).grow().row()));
        ui.settings.shown(() -> {
            Table settingUi = (Table)((Group)((Group)(ui.settings.getChildren().get(1))).getChildren().get(0)).getChildren().get(0); //This looks so stupid lol
            settingUi.row();
            settingUi.button(bundle.get("setting.shar-title"), Styles.cleart, dialog::show);
        });

        Seq<Seq<SharSetting>> settingSeq = new Seq<>();
        Seq<SharSetting> tapSeq = new Seq<>();
        addGraphicSlideSetting("barstyle", 0, 0, 5, 1, s -> s == 0 ? bundle.get("default-bar") : s + bundle.get("th-bar"), tapSeq);
        addGraphicCheckSetting("schem", !mobile, tapSeq, () -> FragmentManager.quickSchemFragment.setSchemTable());

        //TODO: remove all drawing settings
        Seq<SharSetting> drawSeq = new Seq<>();
        addGraphicSlideSetting("selectopacity", 50, 0, 100, 5, s -> s + "%", drawSeq);
        addGraphicSlideSetting("baropacity", 50, 0, 100, 5, s -> s + "%", drawSeq);
        addGraphicCheckSetting("RangeShader", false, drawSeq);
        addGraphicCheckSetting("select", false, drawSeq);
        addGraphicCheckSetting("distanceLine", false, drawSeq);
        addGraphicCheckSetting("spawnerarrow", false, drawSeq);
        addGraphicCheckSetting("elementdebug", false, drawSeq);
        addGraphicCheckSetting("hiddenElem", false, drawSeq);

        settingSeq.add(tapSeq, drawSeq);

        sharset.table(t -> {
            Seq<TextButton> buttons = new Seq<>();
            buttons.add(new TextButton(bundle.get("setting.shar-ui"), Styles.cleart));
            buttons.add(new TextButton(bundle.get("setting.shar-draw"), Styles.cleart));
            buttons.each(b -> b.clicked(() -> buttons.each(b1 -> b1.setChecked(b1 == b))));
            t.table(Styles.black8, bt -> {
                bt.top().align(Align.top);
                buttons.each(b -> {
                    b.getLabel().setFontScale(0.85f);
                    bt.add(b).minHeight(60f * 0.85f).minWidth(150f * 0.85f).top();
                });
            }).grow().row();

            Stack stack = new Stack();
            for(int i = 0; i < settingSeq.size; i++){
                int finalI = i;
                stack.add(new Table(st -> {
                    for(SharSetting setting : settingSeq.get(finalI))
                        st.table(setting::add).left().row();

                    st.button(Core.bundle.get("settings.reset", "Reset to Defaults"), () -> {
                        settingSeq.get(finalI).each(s -> Core.settings.put(s.name, Core.settings.getDefault(s.name)));
                    }).margin(14.0f).width(240.0f).pad(6.0f);
                    st.visibility = () -> buttons.get(finalI).isChecked();
                    st.pack();
                }));
            }
            t.add(stack);
            t.fillParent = true;
        });
    }
}
abstract class SharSetting extends SettingsMenuDialog.SettingsTable.Setting {

    public SharSetting(String name) {
        super(name);
    }

    public SharSetting(String name, Object def) {
        this(name);
        Core.settings.defaults(name, def);
    }

    public void add(Table table) { }
    public void add(SettingsMenuDialog.SettingsTable table) { }
}
