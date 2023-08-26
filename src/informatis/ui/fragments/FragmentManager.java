package informatis.ui.fragments;

import informatis.ui.fragments.sidebar.dialogs.DialogManager;
import informatis.ui.fragments.sidebar.TroopingFragment;
import informatis.ui.fragments.sidebar.windows.WindowManager;

import static arc.Core.scene;
import static mindustry.Vars.ui;

public class FragmentManager {
    public static ElementViewFragment elementViewFragment;
    public static QuickSchemFragment quickSchemFragment;
    public static TileInfoFragment tileInfoFragment;
    public static ServerSearchFragment serverSearchFragment;
    public static SidebarSwitcher sidebarSwitcherFragment;

    public static void init() {
        elementViewFragment = new ElementViewFragment(
                scene.root,
                ui.picker, ui.editor, ui.controls, ui.restart, ui.join, ui.discord,
                ui.load, ui.custom, ui.language, ui.database, ui.settings, ui.host,
                ui.paused, ui.about, ui.bans, ui.admins, ui.traces, ui.maps, ui.content,
                ui.planet, ui.research, ui.mods, ui.schematics, ui.logic
        );
        serverSearchFragment = new ServerSearchFragment();

        quickSchemFragment = new QuickSchemFragment();
        tileInfoFragment = new TileInfoFragment();
        sidebarSwitcherFragment = new SidebarSwitcher(
                    WindowManager.body,
                    DialogManager.body,
                    new TroopingFragment() // cache later?
            );
    }
}
