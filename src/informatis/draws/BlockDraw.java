package informatis.draws;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.scene.style.TextureRegionDrawable;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.blocks.units.UnitFactory;

import static informatis.SUtils.*;
import static arc.Core.settings;

public class BlockDraw extends OverDraw {
    BlockDraw(String name, TextureRegionDrawable icon) {
        super(name, icon);
        registerOption("blockBar");
        registerOption("blockstatus");
    }

    @Override
    public void draw() {
        super.draw();

        if(!enabled) return;
        Groups.build.each(b -> {
            if(!isInCamera(b.x, b.y, b.block.size/2f)) return;
            if(settings.getBool("blockstatus") && b.team != Vars.player.team() && b.block.consumers.length > 0) {
                float multiplier = b.block.size > 1 ? 1.0F : 0.64F;
                float brcx = b.x + (float)(b.block.size * 8) / 2.0F - 8.0F * multiplier / 2.0F;
                float brcy = b.y - (float)(b.block.size * 8) / 2.0F + 8.0F * multiplier / 2.0F;
                Draw.z(71.0F);
                Draw.color(Pal.gray);
                Fill.square(brcx, brcy, 2.5F * multiplier, 45.0F);
                Draw.color(b.status().color);
                Fill.square(brcx, brcy, 1.5F * multiplier, 45.0F);
                Draw.color();
            }

        });
        for (Tile tile : Vars.world.tiles) {
            if(!isInCamera(tile.worldx(), tile.worldy(), 8) || tile.build == null) continue;

            Building b = tile.build;

            if(settings.getBool("blockBar")) {
                drawBar(b, 0, -(b.block.size * 4 - 2), b.healthf(), Pal.health);

                if (b instanceof Turret.TurretBuild turretBuild) {
                    drawBar(b, 0, b.block.size * 4 - 2, turretBuild.reloadCounter / ((Turret) b.block).reload, Pal.ammo);
                }
                if(b instanceof ConstructBlock.ConstructBuild constructBuild)
                    drawBar(b, 0, b.block.size * 4 - 2, constructBuild.progress(), b.team.color);
                if(b instanceof Reconstructor.ReconstructorBuild reconstructorBuild)
                    drawBar(b, 0, b.block.size * 4 - 2, reconstructorBuild.fraction(), b.team.color);
                if(b instanceof UnitFactory.UnitFactoryBuild factoryBuild)
                    drawBar(b, 0, b.block.size * 4 - 2, factoryBuild.fraction(), b.team.color);
            }
        }
    }

    void drawBar(Building b, float offsetX, float offsetY, float progress, Color color) {
        float bx = b.x + offsetX, by = b.y + offsetY;
        float width = b.block.size * 7.5f, height = 2;
        Draw.color(Pal.gray);
        Fill.quad(
                bx - width/2, by + height/2,
                bx - width/2, by - height/2,
                bx + width/2, by - height/2,
                bx + width/2, by + height/2);
        Draw.color(color);
        width = b.block.size * 7.5f - 0.5f; height = 2 - 0.5f;
        Fill.quad(
                bx - width/2, by + height/2,
                bx - width/2, by - height/2,
                bx - width/2 + width * progress, by - height/2,
                bx - width/2 + width * progress, by + height/2);
    }
}
