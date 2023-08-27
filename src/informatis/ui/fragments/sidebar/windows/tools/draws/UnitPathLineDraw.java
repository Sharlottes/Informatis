package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.graphics.g2d.Lines;
import arc.struct.Seq;
import informatis.SUtils;
import mindustry.ai.types.*;
import mindustry.entities.units.UnitController;
import mindustry.gen.Unit;
import mindustry.world.Tile;

import static mindustry.Vars.state;

public class UnitPathLineDraw extends OverDraw {
    public UnitPathLineDraw() {
        super("unitPathLine");
    }

    @Override
    public void onUnit(Unit unit) {
        UnitController c = unit.controller();
        if(unit.team == state.rules.waveTeam && !unit.type.flying && !(c instanceof MinerAI || c instanceof BuilderAI || c instanceof RepairAI || c instanceof DefenderAI || c instanceof FlyingAI)) {
            Lines.stroke(1, unit.team.color);

            Seq<Tile> pathTiles = SUtils.generatePathTiles(unit.tileOn(),unit.team, unit.controller() instanceof SuicideAI ? 0 : unit.pathType());
            for(int i = 0; i < pathTiles.size - 1; i++) {
                Tile from = pathTiles.get(i), to = pathTiles.get(i + 1);
                if(from == null || to == null) continue;
                Lines.line(from.worldx(), from.worldy(), to.worldx(), to.worldy());
            }
        }
    }
}
