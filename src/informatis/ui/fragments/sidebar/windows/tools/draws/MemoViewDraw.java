package informatis.ui.fragments.sidebar.windows.tools.draws;

import mindustry.world.Tile;
import mindustry.world.blocks.logic.MessageBlock;

public class MemoViewDraw extends OverDraw {
    public MemoViewDraw() {
        super("memoView");
    }

    @Override
    public void onTile(Tile tile) {
        if(tile.build instanceof MessageBlock.MessageBuild message) {
            message.drawSelect();
        }
    }
}
