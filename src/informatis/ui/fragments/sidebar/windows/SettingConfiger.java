package informatis.ui.fragments.sidebar.windows;

import arc.Core;
import informatis.ui.fragments.sidebar.windows.ToolConfigable;

public class SettingConfiger implements ToolConfigable {
    public final String name;
    private boolean enabled;

    public SettingConfiger(String name) {
        this.name = name;
        this.enabled = Core.settings.getBool(name, false);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


    @Override
    public void setEnabled(boolean value) {
        enabled = value;
        Core.settings.put(name, value);
    }

    @Override
    public ToolConfigable[] getSubConfigs() {
        return new ToolConfigable[0];
    }
}
