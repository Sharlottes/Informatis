package informatis.ui.fragments.sidebar.windows.tools.draws;

import informatis.ui.fragments.sidebar.windows.SettingConfiger;
import informatis.ui.fragments.sidebar.windows.ToolConfigable;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.world.Tile;

public class OverDraw extends SettingConfiger {
    private final ToolConfigable[] subConfigs;

    public OverDraw(String name, String... subConfigNames) {
        super(name);
        subConfigs = new ToolConfigable[subConfigNames.length];
        for(int i = 0; i < subConfigNames.length; i++) {
            subConfigs[i] = new SettingConfiger(subConfigNames[i]);
        }
    }

    @Override
    public ToolConfigable[] getSubConfigs() {
        return subConfigs;
    }

    /**
        * Groups.build 에서 각 건물에 대한 그리기를 처리합니다.
        * @param build 각 Building 엔티티
    */
    public void onBuilding(Building build) { }

    /**
        * Groups.unit 에서 각 유닛에 대한 그리기를 처리합니다.
        * @param unit 각 Unit 엔티티
    */
    public void onUnit(Unit unit) { }

    /**
        * Vars.world.tiles 에서 각 타일에 대한 그리기를 처리합니다.
        * @param tile 각 Tile 엔티티
    */
    public void onTile(Tile tile) { }

    /**
        * 매 프레임에 대한 그리기를 처리합니다.
    */
    public void draw() {}
}

