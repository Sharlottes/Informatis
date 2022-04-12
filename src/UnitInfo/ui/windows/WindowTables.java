package UnitInfo.ui.windows;

public class WindowTables {
    public static WindowTable
            unitTable, waveTable, coreTable, playerTable, commandTable;

    public static void init() {
        unitTable = new UnitDisplay();
        waveTable = new WaveDisplay();
        coreTable = new CoreDisplay();
        playerTable = new PlayerDisplay();
        commandTable = new CommandDisplay();
    }
}
