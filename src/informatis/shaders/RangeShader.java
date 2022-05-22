package informatis.shaders;

import arc.Core;
import arc.graphics.gl.Shader;
import arc.scene.ui.layout.Scl;
import arc.util.Time;
import mindustry.Vars;

public class RangeShader extends Shader {
    public RangeShader() {
        super(Core.files.internal("shaders/screenspace.vert"), Vars.tree.get("shaders/turretrange.frag"));
    }

    @Override
    public void apply(){
        setUniformf("u_dp", Scl.scl(1f));
        setUniformf("u_time", Time.time / Scl.scl(1f));
        setUniformf("u_offset",
                Core.camera.position.x - Core.camera.width / 2,
                Core.camera.position.y - Core.camera.height / 2);
        setUniformf("u_texsize", Core.camera.width, Core.camera.height);
        setUniformf("u_invsize", 1f/Core.camera.width, 1f/Core.camera.height);
    }
}
