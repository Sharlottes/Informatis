package informatis.ui.fragments.sidebar;

import arc.struct.IntSeq;
import arc.struct.Seq;
import informatis.SUtils;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

public class TroopingManager {
    static final Seq<IntSeq> troops = new Seq<>(10) {{
        SUtils.loop(10, (i) -> {
            add(new IntSeq());
        });
    }};

    public static void applyTrooping(int index) {
        IntSeq seq = troops.get(index);
        Vars.control.input.selectedUnits.each(unit -> seq.add(unit.id));
    }

    public static void selectTrooping(int index) {
        Vars.control.input.commandMode = true;
        Vars.control.input.selectedUnits.clear();
        IntSeq troop = troops.get(index);
        for(int i = 0; i < troop.size; i++) {
            int id = troop.get(i);
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
