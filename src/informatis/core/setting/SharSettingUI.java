package informatis.core.setting;

import arc.*;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.Align;
import mindustry.ui.*;
import informatis.ui.fragments.FragmentManager;

import static arc.Core.*;
import static informatis.core.setting.SettingHelper.*;
import static mindustry.Vars.*;

public class SharSettingUI {
    public static void init(){
        Seq<Seq<SharSetting>> settingSeq = new Seq<>();
        Seq<SharSetting> tapSeq = new Seq<>();
        addGraphicCheckSetting("tileinfo", true, tapSeq, () -> FragmentManager.tileInfoFragment.rebuildTileInfoTable());
        addGraphicCheckSetting("schem", !mobile, tapSeq, () -> FragmentManager.quickSchemFragment.rebuildBody());
        addGraphicCheckSetting("sidebar", !mobile, tapSeq, () -> FragmentManager.sidebarSwitcherFragment.rebuildSidebarTable());
        addGraphicCheckSetting("elementdebug", false, tapSeq);
        addGraphicCheckSetting("hiddenElem", false, tapSeq);
        addGraphicCheckSetting("serverfilter", false, tapSeq, () -> {});

        //TODO: remove all drawing settings
        Seq<SharSetting> drawSeq = new Seq<>();
        addGraphicSlideSetting("selectopacity", 50, 0, 100, 5, s -> s + "%", drawSeq);
        addGraphicSlideSetting("baropacity", 50, 0, 100, 5, s -> s + "%", drawSeq);
        addGraphicCheckSetting("rangeShader", false, drawSeq);
        addGraphicCheckSetting("select", false, drawSeq);
        addGraphicCheckSetting("distanceLine", false, drawSeq);
        addGraphicCheckSetting("spawnerarrow", false, drawSeq);

        settingSeq.add(tapSeq, drawSeq);

        ui.settings.addCategory(bundle.get("setting.shar-title"), settingsTable -> {
            settingsTable.table(t -> {
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
                for (int i = 0; i < settingSeq.size; i++) {
                    int finalI = i;
                    stack.add(new Table(st -> {
                        for (SharSetting setting : settingSeq.get(finalI))
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
        });
    }
}
