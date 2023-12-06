package informatis.ui.fragments.sidebar;

import arc.input.KeyCode;
import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import informatis.SUtils;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

import static arc.Core.input;

public class TroopingFragment extends Table {
    public TroopingFragment() {
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
            SUtils.loop(10, (i) -> {
                list.table(tab -> {
                    IntSeq troop = TroopingManager.troops.get(i);
                    tab.clicked(() -> TroopingManager.selectTrooping(i));
                    tab.left();

                    tab.add(String.valueOf(i)).fontScale(0.75f).width(15).padRight(30);
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
                        icons.defaults().size(10).padLeft(10).grow();
                        icons.image(Icon.cancelSmall).color(Pal.health).get().clicked(troop::clear);
                        icons.image(Icon.upSmall).color(Pal.heal).size(10).get().clicked(() -> TroopingManager.updateTrooping(i));
                        icons.image(Icon.addSmall).color(Pal.gray).size(10).get().clicked(() -> TroopingManager.applyTrooping(i));
                    }).padLeft(10).grow();
                }).pad(10).grow();
                list.row();
                list.image().height(2f).color(Pal.gray).grow();
                list.row();
            });
        });

        update(() -> {
            int i = 0;
            for(KeyCode numCode : KeyCode.numbers) {
                if(input.keyTap(numCode)) {
                    if(input.keyDown(KeyCode.altLeft)) TroopingManager.applyTrooping(i);
                    else if(input.keyDown(KeyCode.capsLock)) TroopingManager.updateTrooping(i);
                    else TroopingManager.selectTrooping(i);
                    break;
                }
                i++;
            }
        });
    }
}
