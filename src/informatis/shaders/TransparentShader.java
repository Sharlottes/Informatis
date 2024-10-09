package informatis.shaders;

import arc.Core;
import arc.graphics.gl.Shader;
import mindustry.Vars;

public class TransparentShader extends Shader {
    public float alpha = 1;

    public TransparentShader() {
        super(Core.files.internal("shaders/screenspace.vert"), Vars.tree.get("shaders/transparent.frag"));
    }

    @Override
    public void apply(){
        setUniformf("u_alpha", alpha);
    }
}
