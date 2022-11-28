package informatis.ui;

import arc.graphics.Color;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Scaling;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Healthc;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.input.InputHandler;
import mindustry.type.UnitType;
import mindustry.ui.Styles;

public class TroopingManager {
    public static Table body;
    static Seq<IntSeq> troops = new Seq<>(10);

    public static void init() {
        for (int i = 0; i < 10; i++) troops.add(new IntSeq());

        body = new Table(t -> {
            t.defaults().growX();

            t.table(header -> {
                header.center().defaults().pad(10).growX();
                header.button("all", Styles.defaultt, () -> {
                    Vars.control.input.selectedUnits.clear();
                    Vars.control.input.selectedUnits.addAll(Vars.player.team().data().units);
                }).wrapLabel(false);
                header.button(Icon.cancel, Styles.defaulti, () -> Vars.control.input.selectedUnits.clear());
            }).row();

            t.image().height(5f).color(Pal.gray).pad(10, 0, 10, 0).row();

            t.table(list -> {
                for(int i = 1; i <= troops.size; i++) {
                    int j = i % troops.size;
                    IntSeq troop = troops.get(j);

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
                            icons.image(Icon.upSmall).size(10).color(Pal.heal).padLeft(10).grow().get().clicked(() -> updateTrooping(j));
                            icons.image(Icon.addSmall).size(10).color(Pal.gray).padLeft(10).grow().get().clicked(() -> applyTrooping(j));
                        }).padLeft(10).grow();
                    });
                    troopTab.clicked(() -> selectTrooping(j));

                    list.add(troopTab).pad(10).grow().row();
                    list.image().height(2f).color(Pal.gray).grow().row();
                }
            });
        });
    }

    public static void applyTrooping(int index) {
        IntSeq seq = troops.get(index);
        Vars.control.input.selectedUnits.each(unit -> seq.add(unit.id));
    }
    public static void selectTrooping(int index) {
        Vars.control.input.commandMode = true;
        Vars.control.input.selectedUnits.clear();
        for(int id : troops.get(index).toArray()) {
            Unit unit = Groups.unit.getByID(id);
            if(unit != null) Vars.control.input.selectedUnits.add(unit);
        }
    }
    public static void updateTrooping(int index) {
        IntSeq seq = troops.get(index);
        seq.clear();
        Vars.control.input.selectedUnits.each(unit -> seq.add(unit.id));
    }
}
