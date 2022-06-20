package informatis.ui.window;

import arc.*;
import arc.func.*;
import arc.input.*;
import arc.math.geom.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;
import informatis.ui.*;

public class Window extends Table {
    public TextureRegionDrawable icon;
    public int id;
    public Cons<Table> content;
    public boolean shown = false;
    public Table window;

    public float minWindowWidth = 160, minWindowHeight = 60;
    public float maxWindowWidth = Float.MAX_VALUE, maxWindowHeight = Float.MAX_VALUE;

    public Window(TextureRegionDrawable icon, String name){
        this(icon, name, null);
    }

    public Window(TextureRegionDrawable icon, String name, Cons<Table> content){
        this.content = content;
        this.name = name;
        this.icon = icon;
        window = this;

        titleBar();
        pane(t -> {
            t.setBackground(Styles.black5);
            t.top().left();
            build(t);
        }).grow().get().setScrollingDisabled(true, true);
        bottomBar();

        setPosition(Core.graphics.getWidth() / 2f - getWidth() / 2f, Core.graphics.getHeight() / 2f - getHeight() / 2f);
        id = WindowManager.register(this);

        visible(() -> shown);

        update(() -> {
            if(x > Core.graphics.getWidth() - getWidth()/2) setPosition(Core.graphics.getWidth() - getWidth()/2, y);
            else if(x < -getWidth()/2) setPosition(-getWidth()/2, y);

            if(y > Core.graphics.getHeight() - getHeight()/2) setPosition(x, Core.graphics.getHeight() - getHeight()/2);
            else if(y < -getHeight()/2) setPosition(x, -getHeight()/2);
        });
    }

    protected void build(Table t){
        if(content != null) content.get(t);
    }

    protected void titleBar(){
        table(t -> {
            // icon and title
            t.table(Tex.buttonEdge1, b -> {
                b.left();
                b.image(icon.getRegion()).size(20f).padLeft(15);
                b.pane(Styles.noBarPane, p -> {
                    p.left();
                    p.add(Core.bundle.get("window."+name+".name")).padLeft(20);
                }).grow().get().setScrollingDisabled(false, true);
            }).grow();
            
            // exit button
            t.table(Tex.buttonEdge3, b -> {
                b.button(Icon.cancel, Styles.emptyi, () -> shown = false);
            }).width(80f).growY();

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
        }).height(48f).growX().row();
    }

    protected void bottomBar(){
        row();
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
    }

    public void toggle(){
        shown = !shown;
    }
}