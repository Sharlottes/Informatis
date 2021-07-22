package UnitInfo.ui;

import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import mindustry.content.UnitTypes;
import mindustry.core.UI;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.content;
import static mindustry.Vars.iconSmall;

public class CoresItemsDisplay {
    private final ObjectSet<Item> usedItems = new ObjectSet<>();
    private final ObjectSet<UnitType> usedUnits = new ObjectSet<>();
    private CoreBlock.CoreBuild core;
    public Team[] teams;
    public Seq<Table> tables = new Seq<>();

    public CoresItemsDisplay(Team[] teams) {
        this.teams = teams;
        rebuild();
    }

    public void resetUsed(){
        usedItems.clear();
        usedUnits.clear();
        tables.each(t->t.background(null));
    }

    void rebuild(){
        tables.clear();
        for(Team team : teams) {
            tables.add(new Table(t -> {
                t.clear();

                if(usedItems.size > 0 || usedUnits.size > 0){
                    t.background(Styles.black6);
                    t.margin(2);
                }

                t.update(() -> {
                    core = team.core();

                    if(content.items().contains(item -> core != null && core.items.get(item) > 0 && usedItems.add(item)) || content.units().contains(unit -> core != null && Groups.unit.count(u -> u.team == team) > 0 && usedUnits.add(unit))){
                        rebuild();
                    }
                });

                int i = 0;
                for(Item item : content.items()){
                    if(usedItems.contains(item)){
                        t.image(item.uiIcon).size(iconSmall).padRight(3).tooltip(tt -> tt.background(Styles.black6).margin(2f).add(item.localizedName).style(Styles.outlineLabel));
                        t.label(() -> core == null ? "0" : UI.formatAmount(core.items.get(item))).padRight(3).minWidth(5 * 8f).left();
                        if(++i % 5 == 0) t.row();
                    }
                }
                t.row();
                i = 0;
                for(UnitType unit : content.units()){
                    if(unit == UnitTypes.block) continue;
                    if(usedUnits.contains(unit)){
                        t.image(unit.uiIcon).size(iconSmall).padRight(3).tooltip(tt -> tt.background(Styles.black6).margin(2f).add(unit.localizedName).style(Styles.outlineLabel));
                        t.label(() -> core == null ? "0" : UI.formatAmount(Groups.unit.count(u -> u.team == team && u.type == unit))).padRight(3).minWidth(5 * 8f).left();
                        if(++i % 5 == 0) t.row();
                    }
                }
            }));
        }
    }
}
