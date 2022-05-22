package informatis.core;

import arc.Core;
import arc.scene.ui.layout.Table;
import mindustry.ui.dialogs.SettingsMenuDialog;

public abstract class SharSetting extends SettingsMenuDialog.SettingsTable.Setting {

    public SharSetting(String name) {
        super(name);
    }

    public SharSetting(String name, Object def) {
        this(name);
        Core.settings.defaults(name, def);
    }

    public void add(Table table) {

    }

    @Override
    public void add(SettingsMenuDialog.SettingsTable table) {

    }
}
