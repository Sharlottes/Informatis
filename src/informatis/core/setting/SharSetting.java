package informatis.core.setting;

import arc.Core;
import arc.scene.ui.layout.Table;
import mindustry.ui.dialogs.SettingsMenuDialog;

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
