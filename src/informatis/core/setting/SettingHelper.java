package informatis.core.setting;

import arc.Core;
import arc.func.Boolp;
import arc.func.Func;
import arc.graphics.Color;
import arc.scene.event.Touchable;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SettingsMenuDialog;

import static arc.Core.settings;

public class SettingHelper {
    public static void addGraphicCheckSetting(String key, boolean def, Seq<SharSetting> list) {
        addGraphicCheckSetting(key, def, list, () -> {
        });
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

    public static void addGraphicSlideSetting(String key, int def, int min, int max, int step, SettingsMenuDialog.StringProcessor sp, Seq<SharSetting> list) {
        list.add(new SharSetting(key, def) {

            @Override
            public void add(Table table) {

                Label value = new Label("", Styles.outlineLabel);
                Table content = new Table();
                content.add(title, Styles.outlineLabel).left().growX().wrap();
                content.add(value).padLeft(10f).right();
                content.margin(3f, 33f, 3f, 33f);
                content.touchable = Touchable.disabled;

                Slider slider = new Slider(min, max, step, false);
                slider.setValue(settings.getInt(name));
                slider.changed(() -> {
                    settings.put(name, (int) slider.getValue());
                    value.setText(sp.get((int) slider.getValue()));
                });
                slider.change();

                addDesc(table.stack(slider, content).width(Math.min(Core.graphics.getWidth() / 1.2f, 460f)).left().padTop(4f).get());
                table.row();
            }
        });
    }

    public static void addGraphicTypeSetting(String key, float min, float max, int def, boolean integer, Boolp condition, Func<String, String> h, Seq<SharSetting> list) {
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

                                if (integer) settings.put(key, (int) value[0]);
                                else settings.put(key, value[0]);

                            }).update(a -> a.setDisabled(!condition.get()))
                            .valid(f -> Strings.canParsePositiveFloat(f) && Strings.parseFloat(f) >= min && Strings.parseFloat(f) <= max).width(120f).left();
                });

                addDesc(table.add(table1).left().padTop(4f).get());
                table.row();
            }
        });
    }

}