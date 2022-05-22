package informatis.ui.draws;

import mindustry.gen.Icon;

public class OverDraws {
    public static OverDraw range, link, unit, block, util;
    public static OverDraw[] all = {};

    public static void init() {
        range = new RangeDraw("Range Draws", Icon.commandRally);
        link = new LinkDraw("Link Draws", Icon.line);
        unit = new UnitDraw("Unit Draws", Icon.units);
        block = new BlockDraw("Block Draws", Icon.crafting);
        util = new UtilDraw("Utils", Icon.github);
        all = new OverDraw[]{range, link, unit, block, util};
    }
}
