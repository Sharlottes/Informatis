package UnitInfo.ui.draws;

import mindustry.gen.Icon;

public class OverDraws {
    public static OverDraw range, link, unit, block;
    public static OverDraw[] all = {};

    public static void init() {
        range = new RangeDraw("Range Draws", Icon.commandRally);
        link = new LinkDraw("Link Draws", Icon.line);
        unit = new UnitDraw("Unit Draws", Icon.units);
        block = new BlockDraw("Block Draws", Icon.crafting);
        all = new OverDraw[]{range, link, unit, block};
    }
}
