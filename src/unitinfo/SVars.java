package unitinfo;

import unitinfo.shaders.*;
import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Teamc;

import static arc.Core.atlas;

public class SVars {
    public static TextureRegion clear = atlas.find("clear");
    public static TextureRegion error = atlas.find("error");
    public static RangeShader turretRange = new RangeShader();
    public static Teamc target;
    public static boolean locked;
    public static float uiResumeRate = 3 * 60f; //default 3s
}
