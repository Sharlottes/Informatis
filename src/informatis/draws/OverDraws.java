package informatis.draws;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.struct.*;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.world.Tile;

import java.util.Arrays;
import java.util.Iterator;

import static arc.Core.graphics;
import static informatis.SVars.turretRange;

public class OverDraws {
    public static final ObjectMap<OverDrawCategory, OverDraw[]> draws = ObjectMap.of(
            OverDrawCategory.Block, new OverDraw[]{ new BlockBarDraw(), new BlockStatusDraw(), new PowerNodeDraw(), new MemoViewDraw() },
            OverDrawCategory.Unit, new OverDraw[] { new PathLineDraw(), new LogicLineDraw(), new CommandLineDraw(), new UnitPathLineDraw(), new UnitItemDraw(),new UnitBarDraw(), },
            OverDrawCategory.Range, new OverDraw[] { new BlockRangeDraw(), new UnitRangeDraw(), new PlayerRangeDraw() },
            OverDrawCategory.Link, new OverDraw[] { new UnitCargoLinkDraw(), new MassLinkDraw() },
            OverDrawCategory.Util, new OverDraw[] { new MagicCursorDraw(),  new AutoShootDraw(),  new InfoRingDraw() }
    );
    private static final Seq<OverDraw> overDraws = new Seq<>();
    public static float[] zIndexTeamCache = new float[Team.baseTeams.length];
    private static final FrameBuffer effectBuffer = new FrameBuffer();

    public static void init() {
        for(OverDraw[] draws : draws.values()) {
            overDraws.addAll(draws);
        }

        for (int i = 0; i < Team.baseTeams.length; i++) {
            zIndexTeamCache[i] = 166 + (Team.baseTeams.length - Team.baseTeams[i].id) * 3 * 0.001f;
        }

        Events.run(EventType.Trigger.draw, () -> {
            Core.camera.bounds(Tmp.r1);
            effectBuffer.resize(graphics.getWidth(), graphics.getHeight());
            for(float zIndex : zIndexTeamCache) {
                Draw.drawRange(zIndex, () -> effectBuffer.begin(Color.clear), () -> {
                    effectBuffer.end();
                    effectBuffer.blit(turretRange);
                });
            }

            for(OverDraw draw : overDraws) {
                if(!draw.isEnabled()) continue;

                for(Building building : Groups.build) {
                    draw.onBuilding(building);
                }

                for(Unit unit : Groups.unit) {
                    draw.onUnit(unit);
                }

                for(Tile tile : Vars.world.tiles) {
                    draw.onTile(tile);
                }
                draw.draw();
            };
            Draw.reset();
        });
    }

}
