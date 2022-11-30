package informatis.draws;

import mindustry.world.Tile;
import mindustry.world.blocks.logic.MessageBlock;

import static mindustry.Vars.tilesize;

public class MemoViewDraw extends OverDraw {
    public MemoViewDraw() {
        super("memoView", OverDrawCategory.Block);
    }

    @Override
    public void onTile(Tile tile) {
        if(tile.build instanceof MessageBlock.MessageBuild message) {
            message.drawSelect();
        }
    }
}
