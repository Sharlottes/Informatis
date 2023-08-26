package informatis.ui.fragments.sidebar.dialogs;

import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;

public class DialogManager {
    public static Dialog resourcePreview;
    public static Table body;

    public static void init() {
        resourcePreview = new ResourcePreviewDialog();

        body = new Table(t -> {
           t.button(Icon.file, () -> resourcePreview.show());
        });
    }
}
