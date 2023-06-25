package informatis.ui.windows;

import arc.*;
import arc.func.*;
import arc.input.*;
import arc.math.Mathf;
import arc.math.geom.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.Bar;

public class Window extends Table {
    public TextureRegionDrawable icon;
    public Cons<Table> content;
    public boolean shown = false;

    public float minWindowWidth = 160, minWindowHeight = 60;
    public float maxWindowWidth = Float.MAX_VALUE, maxWindowHeight = Float.MAX_VALUE;
    public Window(String name){
        this(new TextureRegionDrawable(Core.atlas.find("clear")), name, null);
    }
    public Window(TextureRegionDrawable icon, String name){
        this(icon, name, null);
    }

    public Window(TextureRegionDrawable icon, String name, Cons<Table> content) {
        this.icon = icon;
        this.name = name;
        this.content = content;
    }

    public void build() {
        buildTitleBar().row();
        pane(new Table(t -> {
            t.setBackground(Styles.black5);
            buildBody(t);
        })).grow();
        row();
        buildBottomBar();
        visible(() -> shown);
        update(() -> {
            setPosition(
                Mathf.clamp(x, 0, Core.graphics.getWidth() - getWidth()),
                Mathf.clamp(y, 0, Core.graphics.getHeight() - getHeight())
            );
        });
    }

    protected Window buildTitleBar() {
        table(t -> {
            t.pane(b -> {
                b.left();
                b.setBackground(Tex.buttonEdge1);
                b.image(icon.getRegion()).scaling(Scaling.fill).size(20f).padLeft(15);
                b.add(Core.bundle.get("window."+name+".name")).padLeft(20);
            })
                .touchable(Touchable.disabled)
                .grow();

            t.table(b -> {
                b.setBackground(Tex.buttonEdge3);
                b.button(Icon.cancel, Styles.emptyi, () -> {
                    shown = false;
                }).fill();
            })
                .width(80f)
                .growY();

            // handles the dragging.
            t.touchable = Touchable.enabled;
            t.addListener(new InputListener(){
                float lastX, lastY;
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    Vec2 v = t.localToStageCoordinates(Tmp.v1.set(x, y));
                    lastX = v.x;
                    lastY = v.y;
                    toFront();
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float dx, float dy, int pointer) {
                    Vec2 v = t.localToStageCoordinates(Tmp.v1.set(dx, dy));
                    setPosition( x + (v.x - lastX),  y + (v.y - lastY));
                    lastX = v.x;
                    lastY = v.y;
                }
            });
        }).height(48f).growX();

        return this;
    }

    protected void buildBody(Table t){
        if(content != null) content.get(t);
    }

    protected Window buildBottomBar() {
        table(Styles.black5, t -> {
            t.table().growX();
            t.table(Icon.resizeSmall, r -> {
                r.bottom().left();
                r.touchable = Touchable.enabled;
                r.addListener(new InputListener(){
                    float lastX, lastY;
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                        Vec2 v = r.localToStageCoordinates(Tmp.v1.set(x, y));
                        lastX = v.x;
                        lastY = v.y;
                        return true;
                    }

                    @Override
                    public void touchDragged(InputEvent event, float x, float y, int pointer) {
                        Vec2 v = r.localToStageCoordinates(Tmp.v1.set(x, y));
                        float w = v.x - lastX;
                        float h = v.y - lastY;

                        // will softlock if initial size is smaller than minimum
                        // so don't do that!
                        if(getWidth() + w < minWindowWidth || getWidth() + w > maxWindowWidth) w = 0;
                        if(getHeight() - h < minWindowHeight || getHeight() - h > maxWindowHeight) h = 0;
                        sizeBy(w, -h);
                        moveBy(0, h);
                        lastX = v.x;
                        lastY = v.y;
                    }
                });
            }).size(20f).left();
        }).height(20f).growX();

        return this;
    }

    public void toggle(){
        shown = !shown;
    }
}