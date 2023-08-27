package informatis.ui.fragments.sidebar.windows.tools.tools;

import arc.Events;
import arc.graphics.Color;
import mindustry.Vars;
import mindustry.game.EventType;

public class FogRemover extends Tool {
    Color lastStaticColor, lastDynamicColor;

    public FogRemover() {
        super("fogremover");

        Events.on(EventType.WorldLoadEndEvent.class, x -> {
            toggleRemover();
        });
    }

    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        toggleRemover();
    }

    private void toggleRemover() {
        if(isEnabled()) {
            lastStaticColor = Vars.state.rules.staticColor;
            lastDynamicColor = Vars.state.rules.dynamicColor;
            Vars.state.rules.staticColor = Color.clear;
            Vars.state.rules.dynamicColor = Color.clear;
        } else {
            Vars.state.rules.staticColor = lastStaticColor == null ? Vars.state.rules.staticColor : lastStaticColor;
            Vars.state.rules.dynamicColor = lastDynamicColor == null ? Vars.state.rules.dynamicColor : lastDynamicColor;
        }
    }
}
