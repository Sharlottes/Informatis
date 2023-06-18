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
import mindustry.gen.Groups;
import mindustry.world.Tile;

import static arc.Core.graphics;
import static informatis.SVars.turretRange;

public class OverDraws {
    public static OverDraw
        blockBar = new BlockBarDraw(),
        blockStatus = new BlockStatusDraw(),
        powerNode = new PowerNodeDraw(),
        unitCargoLink = new UnitCargoLinkDraw(),
        massLink = new MassLinkDraw(),
        blockRange = new BlockRangeDraw(),
        memoView = new MemoViewDraw(),
        unitRange = new UnitRangeDraw(),
        playerRange = new PlayerRangeDraw(),
        pathLine = new PathLineDraw(),
        logicLine = new LogicLineDraw(),
        commandLine = new CommandLineDraw(),
        unitPathLine = new UnitPathLineDraw(),
        unitItem = new UnitItemDraw(),
        unitBar = new UnitBarDraw(),
        magicCursor = new MagicCursorDraw(),
        autoShoot = new AutoShootDraw(),
        infoRing = new InfoRingDraw();
    static ObjectMap<OverDrawCategory, Seq<OverDraw>> draws;
    static FrameBuffer effectBuffer = new FrameBuffer();

    public static void init() {
        Events.run(EventType.Trigger.draw, () -> {
            Core.camera.bounds(Tmp.r1);
            effectBuffer.resize(graphics.getWidth(), graphics.getHeight());
            for(Team team : Team.baseTeams) {
                Draw.drawRange(166 + (Team.baseTeams.length-team.id) * 3, 1, () -> effectBuffer.begin(Color.clear), () -> {
                    effectBuffer.end();
                    effectBuffer.blit(turretRange);
                });
            }

            Groups.build.each(building ->
                eachDraws(draw -> draw.onBuilding(building))
            );
            Groups.unit.each(unit ->
                eachDraws(draw -> draw.onUnit(unit))
            );
            for(Tile tile : Vars.world.tiles) {
                eachDraws(draw -> draw.onTile(tile));
            }
            eachDraws(OverDraw::draw);
        });

        Events.run(EventType.Trigger.update, () -> {
            eachDraws(OverDraw::update);
        });
    }

    public static ObjectMap<OverDrawCategory, Seq<OverDraw>> getDraws() {
        if(draws == null) {
            draws = new ObjectMap<>();
            for(OverDrawCategory category : OverDrawCategory.values()) {
                draws.put(category, new Seq<>());
            }
        }
        return draws;
    }

    static void eachDraws(Cons<OverDraw> runner) {
        for(ObjectMap.Entry<OverDrawCategory, Seq<OverDraw>> draws : OverDraws.draws.entries()) {
            if(draws.key.enabled) draws.value.each(draw -> {
                if(Core.settings.getBool(draw.name, false)) runner.get(draw);
                Draw.reset();
            });
        };
    }
}
