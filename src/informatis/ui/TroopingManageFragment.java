package informatis.ui;

import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

public class TroopingManageFragment extends Table {
    public TroopingManageFragment() {
        defaults().growX();

        table(header -> {
            header.center().defaults().pad(10).growX();
            header.button("all", Styles.defaultt, () -> {
                Vars.control.input.selectedUnits.clear();
                Vars.control.input.selectedUnits.addAll(Vars.player.team().data().units);
            }).wrapLabel(false);
            header.button(Icon.cancel, Styles.defaulti, () -> Vars.control.input.selectedUnits.clear());
        });
        row();
        image().height(5f).color(Pal.gray).pad(10, 0, 10, 0);
        row();
        table(list -> {
            for(int i = 1; i <= TroopingManager.troops.size; i++) {
                int j = i % TroopingManager.troops.size;
                IntSeq troop = TroopingManager.troops.get(j);

                Table troopTab = new Table(tab -> {
                    tab.left();
                    tab.add(String.valueOf(j)).fontScale(0.75f).width(15).padRight(30);
                    tab.image(() -> {
                        if(troop.isEmpty()) return Icon.cancel.getRegion();
                        Unit unit = Groups.unit.getByID(troop.peek());
                        if(unit == null) return Icon.cancel.getRegion();
                        return unit.type.fullIcon;
                    }).size(10).padRight(10);
                    tab.label(() -> {
                        int amount = 0;
                        for(int id : troop.toArray()) {
                            Unit unit = Groups.unit.getByID(id);
                            if(unit != null && !unit.dead()) amount++;
                        }
                        return String.valueOf(amount);
                    }).minWidth(30).fontScale(0.5f);
                    tab.table(icons -> {
                        icons.image(Icon.cancelSmall).size(10).color(Pal.health).padLeft(10).grow().get().clicked(troop::clear);
                        icons.image(Icon.upSmall).size(10).color(Pal.heal).padLeft(10).grow().get().clicked(() -> TroopingManager.updateTrooping(j));
                        icons.image(Icon.addSmall).size(10).color(Pal.gray).padLeft(10).grow().get().clicked(() -> TroopingManager.applyTrooping(j));
                    }).padLeft(10).grow();
                });
                troopTab.clicked(() -> TroopingManager.selectTrooping(j));

                list.add(troopTab).pad(10).grow().row();
                list.image().height(2f).color(Pal.gray).grow().row();
            }
        });
    }
}
