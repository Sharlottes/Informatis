package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.struct.*;
import arc.util.Tmp;
import informatis.shaders.Shaders;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.world.Tile;

import static arc.Core.graphics;

public class OverDrawManager {
    public static final ObjectMap<OverDrawCategory, OverDraw[]> draws = ObjectMap.of(
            OverDrawCategory.Block, new OverDraw[]{ new BlockBarDraw(), new BlockStatusDraw(), new PowerNodeDraw(), new MemoViewDraw() },
            OverDrawCategory.Unit, new OverDraw[] { new PathLineDraw(), new LogicLineDraw(), new CommandLineDraw(), new UnitPathLineDraw(), new UnitItemDraw(), new UnitBarDraw(), },
            OverDrawCategory.Range, new OverDraw[] { new RangeDraw()  },
            OverDrawCategory.Link, new OverDraw[] { new UnitCargoLinkDraw(), new MassLinkDraw() },
            OverDrawCategory.Util, new OverDraw[] { new MagicCursorDraw(),new SelectArrawDraw(), new SpawnerArrawDraw(), new DistanceLineDraw() }
    );
    public static final Seq<OverDraw> overDraws = new Seq<>();
    public static final float[] zIndexTeamCache = new float[Team.baseTeams.length];
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
                    effectBuffer.blit(Shaders.turretRange);
                });
            }

            for(OverDraw draw : overDraws) {
                if(!draw.isEnabled()) continue;

                for(Building building : Groups.build) {
                    draw.onBuilding(building);
                    Draw.reset();
                }

                for(Unit unit : Groups.unit) {
                    draw.onUnit(unit);
                    Draw.reset();
                }

                for(Tile tile : Vars.world.tiles) {
                    draw.onTile(tile);
                    Draw.reset();
                }
                draw.draw();
                Draw.reset();
            }
        });
    }

}
