uniform sampler2D u_texture;
uniform float u_alpha;

varying vec2 v_texCoords;

void main(){
    vec4 color = texture2D(u_texture, v_texCoords);
    color.a *= u_alpha;
    gl_FragColor = color;
}