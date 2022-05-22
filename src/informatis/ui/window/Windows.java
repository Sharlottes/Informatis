package informatis.ui.window;

public class Windows {
    public static MapEditorWindow editorTable;

    public static void load(){
        new UnitWindow();
        new WaveWindow();
        new CoreWindow();
        new PlayerWindow();
        new ToolWindow();
        editorTable = new MapEditorWindow();
    }
}
