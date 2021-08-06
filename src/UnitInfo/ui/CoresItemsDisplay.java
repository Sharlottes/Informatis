package UnitInfo.ui;

import arc.scene.ui.Label;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.Time;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.storage.*;

import static mindustry.Vars.*;

public class CoresItemsDisplay {
    private final ObjectMap<Team, ObjectSet<Item>> usedItems = new ObjectMap<>();
    private final ObjectMap<Team, ObjectSet<UnitType>> usedUnits = new ObjectMap<>();
    private CoreBlock.CoreBuild core;
    public Team[] teams;
    public Seq<Table> tables = new Seq<>();
    public ObjectMap<Team, Seq<ItemStack>> prevItems = new ObjectMap<>();
    public ObjectMap<Team, Seq<ItemStack>> updateItems = new ObjectMap<>();
    float heat;

    public CoresItemsDisplay(Team[] teams) {
        this.teams = teams;
        resetUsed();
        rebuild();
    }

    public void resetUsed(){
        usedItems.clear();
        usedUnits.clear();
        updateItems.clear();
        prevItems.clear();
        for(Team team : teams) {
            usedItems.put(team, new ObjectSet<>());
            usedUnits.put(team, new ObjectSet<>());
            Seq<ItemStack> stacks = new Seq();
            content.items().each(i -> stacks.add(new ItemStack(i, 0)));
            updateItems.put(team, stacks);
            prevItems.put(team, stacks);
        }
        tables.each(t->t.background(null));
    }

    public void updateItem(Team team){
        if(prevItems.get(team) != null && core != null) for(Item item : content.items()){
            updateItems.get(team).get(item.id).set(item, core.items.get(item) - prevItems.get(team).get(item.id).amount);
            prevItems.get(team).get(item.id).set(item, core.items.get(item));
        }
        prevItems.clear();
        Seq<ItemStack> stacks = new Seq();
        if(core != null) content.items().each(i -> stacks.add(new ItemStack(i, core.items.get(i))));
        prevItems.put(team, stacks);

    }

    public void rebuild(){
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

                    heat += Time.delta;
                    if(heat >= 60f) {
                        heat = 0;
                        updateItem(team);
                    }

                    if(content.items().contains(item -> core != null &&
                            core.items.get(item) > 0 &&
                            usedItems.get(team).add(item)) ||
                        content.units().contains(unit -> core != null &&
                            Groups.unit.count(u -> u.type == unit && u.team == team) > 0 &&
                            usedUnits.get(team).add(unit))){
                        rebuild();
                    }
                });

                final int[] i = {0};
                t.table(itemTable -> {
                    itemTable.center();
                    for(Item item : content.items()){
                        if(usedItems.get(team).contains(item)){
                            itemTable.add(new Stack(){{
                                add(new Table(tt -> {
                                    tt.image(item.uiIcon).size(iconSmall).tooltip(ttt -> ttt.background(Styles.black6).margin(2f).add(item.localizedName).style(Styles.outlineLabel));
                                    tt.label(() -> core == null ? "0" : UI.formatAmount(core.items.get(item))).minWidth(5 * 8f).left();
                                }));
                                add(new Table(tt -> {
                                    tt.bottom().right();
                                    Label label = new Label(() -> {
                                        int amount = updateItems.get(team).get(item.id).amount;
                                        return (amount > 0 ? "[green]+" : amount == 0 ? "[orange]" : "[red]") + amount + "[]";
                                    });
                                    label.setFontScale(0.65f);
                                    tt.add(label).bottom().right().padTop(16f);
                                    tt.pack();
                                }));
                            }}).padRight(3).left();
                            if(++i[0] % 5 == 0) itemTable.row();
                        }
                    }
                });
                t.row();
                i[0] = 0;
                t.table(unitTable -> {
                    unitTable.center();
                    for(UnitType unit : content.units()){
                        if(unit != UnitTypes.block && usedUnits.get(team).contains(unit)){
                            unitTable.image(unit.uiIcon).size(iconSmall).padRight(3).tooltip(tt -> tt.background(Styles.black6).margin(2f).add(unit.localizedName).style(Styles.outlineLabel));
                            unitTable.label(() -> core == null ? "0" : UI.formatAmount(Groups.unit.count(u -> u.team == team && u.type == unit))).padRight(3).minWidth(5 * 8f).left();
                            if(++i[0] % 5 == 0) unitTable.row();
                        }
                    }
                });
            }));
        }
    }
}
