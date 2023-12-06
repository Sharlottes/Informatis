package informatis;

import informatis.shaders.*;
import arc.graphics.g2d.TextureRegion;

import static arc.Core.atlas;

public class SVars {
    public static final TextureRegion
            clear = atlas.find("clear");
    public static final TextureRegion error = atlas.find("error");
    public static final RangeShader turretRange = new RangeShader();
    public static informatis.core.Pathfinder pathfinder;

    public static void init() {
        pathfinder = new informatis.core.Pathfinder();
    }
}
