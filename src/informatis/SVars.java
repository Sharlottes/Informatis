package informatis;

import informatis.shaders.*;
import arc.graphics.g2d.TextureRegion;
import mindustry.ai.Pathfinder;
import mindustry.gen.Teamc;

import static arc.Core.atlas;

public class SVars {
    public static TextureRegion
            clear = atlas.find("clear"),
            error = atlas.find("error");
    public static RangeShader turretRange = new RangeShader();
    public static informatis.core.Pathfinder pathfinder;

    public static void init() {
        pathfinder = new informatis.core.Pathfinder();
    }
}
