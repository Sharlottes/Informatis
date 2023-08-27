package informatis.ui.fragments.sidebar.windows.tools.tools;

import arc.Events;
import mindustry.game.EventType;

public class ToolManager {
    public static Tool[] tools = new Tool[] { new FogRemover() };

    public static void init() {
        Events.run(EventType.Trigger.update, () -> {
            for (Tool tool : tools) {
                tool.onUpdate();
            }
        });
    }
}
