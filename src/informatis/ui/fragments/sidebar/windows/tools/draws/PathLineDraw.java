package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.graphics.g2d.Lines;
import arc.struct.Seq;
import informatis.SUtils;
import mindustry.world.Tile;

import static mindustry.Vars.state;

public class PathLineDraw extends OverDraw {
    public PathLineDraw() {
        super("pathLine");
    }

    @Override
    public void draw() {
        Seq<Tile> pathTiles = SUtils.generatePathTiles();

        Lines.stroke(1, state.rules.waveTeam.color);
        for(int i = 0; i < pathTiles.size - 1; i++) {
            Tile from = pathTiles.get(i), to = pathTiles.get(i + 1);
            if(from == null || to == null) continue;
            Lines.line(from.worldx(), from.worldy(), to.worldx(), to.worldy());
        }
    }
}
