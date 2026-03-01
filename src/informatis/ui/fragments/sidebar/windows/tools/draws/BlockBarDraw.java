package informatis.ui.fragments.sidebar.windows.tools.draws;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.blocks.units.UnitFactory;

import static arc.Core.settings;
import static informatis.SUtils.isInCamera;

public class BlockBarDraw extends OverDraw {
    public BlockBarDraw() {
        super("blockBar");
    }

    @Override
    public void onTile(Tile tile) {
        if(!isInCamera(tile.worldx(), tile.worldy(), 8) || tile.build == null) {
            return;
        }

        Building b = tile.build;

        if(settings.getBool("blockBar")) {
            drawBar(b, 0, -(b.block.size * 4 - 2), b.healthf(), Pal.health);

            if (b instanceof Turret.TurretBuild turretBuild) {
                drawBar(b, 0, b.block.size * 4 - 2, turretBuild.reloadCounter / ((Turret) b.block).reload, Pal.ammo);
            }

            if(b instanceof ConstructBlock.ConstructBuild constructBuild) {
                drawBar(b, 0, b.block.size * 4 - 2, constructBuild.progress(), b.team.color);
            }

            if(b instanceof Reconstructor.ReconstructorBuild reconstructorBuild) {
                drawBar(b, 0, b.block.size * 4 - 2, reconstructorBuild.fraction(), b.team.color);
            }

            if(b instanceof UnitFactory.UnitFactoryBuild factoryBuild) {
                drawBar(b, 0, b.block.size * 4 - 2, factoryBuild.fraction(), b.team.color);
            }
        }
    }

    void drawBar(Building b, float offsetX, float offsetY, float progress, Color color) {
        float bx = b.x + offsetX;
        float by = b.y + offsetY;
        float backgroundWidth = b.block.size * 7.5f;
        float backgroundHeight = 2;
        float progressWidth = b.block.size * 7.5f - 0.5f;
        float progressHeight = 2 - 0.5f;


        Draw.color(Pal.gray);
        Fill.quad(
                bx - backgroundWidth/2, by + backgroundHeight/2,
                bx - backgroundWidth/2, by - backgroundHeight/2,
                bx + backgroundWidth/2, by - backgroundHeight/2,
                bx + backgroundWidth/2, by + backgroundHeight/2);
        Draw.color(color);
        Fill.quad(
                bx - progressWidth/2, by + progressHeight/2,
                bx - progressWidth/2, by - progressHeight/2,
                bx - progressWidth/2 + progressWidth * progress, by - progressHeight/2,
                bx - progressWidth/2 + progressWidth * progress, by + progressHeight/2);
        Draw.color();
    }
}
