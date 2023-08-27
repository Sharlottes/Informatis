package informatis.ui.fragments.sidebar.windows.tools.tools;

import arc.graphics.Color;
import mindustry.Vars;

public class FogRemover extends Tool {
    Color lastStaticColor, lastDynamicColor;

    public FogRemover() {
        super("fogremover");
    }

    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        if(value) {
            lastStaticColor = Vars.state.rules.staticColor;
            lastDynamicColor = Vars.state.rules.dynamicColor;
            Vars.state.rules.staticColor = Color.clear;
            Vars.state.rules.dynamicColor = Color.clear;
        } else {
            Vars.state.rules.staticColor = lastStaticColor;
            Vars.state.rules.dynamicColor = lastDynamicColor;
        }
    }
}
