package UnitInfo.ui.windows;

public class WindowTables {
    public static WindowTable
            unitTable, waveTable, coreTable, playerTable, toolTable;
    public static MapEditorDisplay editorTable;

    public static void init() {
        unitTable = new UnitDisplay();
        waveTable = new WaveDisplay();
        coreTable = new CoreDisplay();
        playerTable = new PlayerDisplay();
        toolTable = new ToolDisplay();
        editorTable = new MapEditorDisplay();
    }
}
