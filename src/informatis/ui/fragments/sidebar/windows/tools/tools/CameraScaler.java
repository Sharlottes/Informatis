package informatis.ui.fragments.sidebar.windows.tools.tools;

import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType;

public class CameraScaler extends Tool {
    private float lastMinZoom = 1.5f;

    // TODO: set subconfig by slider later
    private final float targetMinZoom = 0.35f;

    public CameraScaler() {
        super("camerascaler");

        Events.on(EventType.WorldLoadEndEvent.class, x -> {
            toggleScaler();
        });
    }

    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        toggleScaler();
    }

    private void toggleScaler() {
        if(isEnabled()) {
            lastMinZoom = Vars.renderer.minZoom;
            Vars.renderer.minZoom = targetMinZoom;
        } else {
            Vars.renderer.minZoom = lastMinZoom;
        }
    }
}
