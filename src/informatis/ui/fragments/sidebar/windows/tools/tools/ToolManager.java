package informatis.ui.fragments.sidebar.windows.tools.tools;

import arc.Events;
import mindustry.game.EventType;

public class ToolManager {
    public static final Tool[] tools = new Tool[] { new FogRemover(), new CameraScaler(), new UnitVisualizer(), new AutoShooter() };

    public static void init() {
        Events.run(EventType.Trigger.update, () -> {
            for (Tool tool : tools) {
                if(tool.isEnabled()) {
                    tool.onUpdate();
                }
            }
        });
    }
}
