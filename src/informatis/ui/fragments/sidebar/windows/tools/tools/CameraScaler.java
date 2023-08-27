package informatis.ui.fragments.sidebar.windows.tools.tools;

import mindustry.Vars;

public class CameraScaler extends Tool {
    private float lastMinZoom;

    // TODO: set subconfig by slider later
    private float targetMinZoom = 0.35f;

    public CameraScaler() {
        super("camerascaler");
    }

    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        if(value) {
            lastMinZoom = Vars.renderer.minZoom;
            Vars.renderer.minZoom = targetMinZoom;
        } else {
            Vars.renderer.minZoom = lastMinZoom;
        }
    }
}
