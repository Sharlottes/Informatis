package informatis.draws;

import arc.Core;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.world.Tile;

public class OverDraw {
    public String name;
    private boolean enabled;

    OverDraw(String name) {
        this.name = name;
        this.enabled = Core.settings.getBool(name, false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
        Core.settings.put(name, value);
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

