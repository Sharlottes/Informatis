package informatis.ui.components;

import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import informatis.SUtils;
import informatis.core.EventEmitter;
import mindustry.ui.Styles;

public class TabsFragment extends Table {
    public int currentTabIndex = 0;
    public final EventEmitter<Event> eventEmitter = new EventEmitter<>();

    public TabsFragment(String... tabs) {
        super();
        TextButton[] buttons = new TextButton[tabs.length];

        SUtils.loop(tabs.length, i -> {
            TextButton button = new TextButton(tabs[i], Styles.flatToggleMenut);
            buttons[i] = button;
            button.clicked(() -> {
                currentTabIndex = i;
                button.toggle();
                SUtils.loop(buttons, (otherButton, bi) -> {
                    otherButton.setChecked(currentTabIndex == bi);
                });
                eventEmitter.fire(Event.TabChanged);
            });
            button.setChecked(i == currentTabIndex);
            add(button).height(50).growX();
        });
    }

    public enum Event {
        TabChanged
    }
}
