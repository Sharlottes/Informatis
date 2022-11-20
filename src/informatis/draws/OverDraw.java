package informatis.draws;

import arc.scene.Element;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.CheckBox;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.ui.Styles;
import mindustry.world.Tile;

import static arc.Core.bundle;
import static arc.Core.settings;

public class OverDraw {
    public String name;

    OverDraw(String name, OverDrawCategory category) {
        this.name = name;

        OverDraws.getDraws().get(category, new Seq<>()).add(this);
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

    public void update() {}
}

