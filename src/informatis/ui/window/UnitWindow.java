package informatis.ui.window;

import arc.Core;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.utils.Disableable;
import informatis.core.*;
import informatis.ui.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.Bits;
import arc.struct.*;
import arc.util.*;
import informatis.ui.widgets.RectWidget;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.type.Weapon;
import mindustry.ui.Styles;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.payloads.Payload;

import java.util.Objects;

import static informatis.SVars.*;
import static informatis.SUtils.*;
import static mindustry.Vars.*;



class UnitWindow extends Window {
    final Seq<Color> lastColors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);
    Teamc latestTarget = getTarget();
    int barSize = 6;
    ScrollPane barPane;
    Table window;

    private float barScrollPos;

    public UnitWindow() {
        super(Icon.units, "unit");
        window = this;
    }

    //TODO: add new UnitInfoDisplay(), new WeaponDisplay();
    @Override
    protected void build(Table table) {
        table.top().background(Styles.black8);
        table.table(tt -> {
            tt.center();
            Image image = RectWidget.build();
            image.update(()->{
                TextureRegion region = clear;
                if (target instanceof Unit u && u.type != null) region = u.type.uiIcon;
                else if (target instanceof Building b) {
                    if (b instanceof ConstructBlock.ConstructBuild cb) region = cb.current.uiIcon;
                    else if (b.block != null) region = b.block.uiIcon;
                }
                image.setDrawable(region);
            });
            image.clicked(()->{
                if(target == getTarget()) locked = !locked;
                target = getTarget();
            });
            tt.add(image).size(iconMed).padRight(12f);
            tt.label(() -> {
                if (target instanceof Unit u && u.type != null) return u.type.localizedName;
                if (target instanceof Building b && b.block != null) {
                    if (target instanceof ConstructBlock.ConstructBuild cb) return cb.current.localizedName;
                    return b.block.localizedName;
                }
                return "";
            }).color(Pal.accent);
        }).tooltip(tool -> {
            tool.background(Styles.black6);
            tool.table().update(to -> {
                to.clear();
                if(target instanceof Unit u) {
                    to.add(u.isPlayer() ? u.getPlayer().name : "AI").row();
                    to.add(target.tileX() + ", " + target.tileY()).row();
                    to.add("[accent]"+ Strings.fixed(u.armor, 0) + "[] Armor");
                }
            }).margin(12f);
        }).margin(12f).row();
        table.image().height(4f).color((target==null?player.unit():target).team().color).growX().row();
        barPane = new ScrollPane(buildBarList(), Styles.noBarPane);
        barPane.update(() -> {
            if(latestTarget != target) {
                latestTarget = target;
                barPane.setWidget(buildBarList());
                Log.info("updated");
            }
            if(barPane.hasScroll()){
                Element result = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if(result == null || !result.isDescendantOf(barPane)){
                    Core.scene.setScrollFocus(null);
                }
            }
            barScrollPos = barPane.getScrollY();
        });
        barPane.setScrollingDisabledX(true);
        barPane.setScrollYForce(barScrollPos);
        table.add(barPane).growX().padTop(12f);
    }

    Table buildBarList() {
        return new Table(table -> {
            table.top();
            for (int i = 0; i < barSize; i++) {
                table.add(addBar(i)).growX().get();
                table.row();
            }
            table.add(new SBar("+", Color.clear, 0)).height(4 * 8f).growX().get().clicked(()->{
                barSize++;
                barPane.setWidget(buildBarList());
            });
        });
    }

    Table addBar(int index) {
        return new Table(bar -> {
            if(index >= BarInfo.data.size) {
                bar.add(new SBar("[lightgray]<Empty>[]", Color.clear, 0)).height(4 * 8f).growX();
                return;
            }

            bar.update(()->{
                BarInfo.BarData data = BarInfo.data.get(index);
                if (index >= lastColors.size) lastColors.add(data.color);
                else lastColors.set(index, data.color);
            });

            bar.add(new SBar(() -> BarInfo.data.get(index).name, () -> BarInfo.data.get(index).color, () -> BarInfo.data.get(index).number)).height(4 * 8f).growX();
            Image icon = new Image(){
                @Override
                public void draw() {
                    validate();

                    float x = this.x + imageX;
                    float y = this.y + imageY;
                    float width = imageWidth * this.scaleX;
                    float height = imageHeight * this.scaleY;
                    Draw.color(Color.white);
                    Draw.alpha(parentAlpha * color.a);
                    BarInfo.BarData data = BarInfo.data.get(index);
                    getDrawable().draw(x, y, width, height);
                    if (!hasMouse() && ScissorStack.push(Tmp.r1.set(ScissorStack.peek().x + x,  ScissorStack.peek().y + y, width, height * data.number))) {
                        Draw.color(data.color);
                        getDrawable().draw(x, y, width, height);
                        ScissorStack.pop();
                    }

                    Log.info("----------------------");
                }
            };
            icon.addListener(new InputListener(){
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Element fromActor){
                    icon.setDrawable(Icon.cancel);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Element fromActor){
                    icon.setDrawable(BarInfo.data.get(index).icon);
                }
            });
            icon.clicked(()->{
                if(barSize > 0) barSize--;
                barPane.setWidget(buildBarList());
            });
            icon.setDrawable(BarInfo.data.get(index).icon);
            bar.add(icon).size(iconMed * 0.75f).padLeft(8f);
        });
    }

    static class WeaponDisplay extends Table {
        WeaponDisplay() {
            table().update(tt -> {
                tt.clear();
                if(getTarget() instanceof Unit u && u.type != null && u.hasWeapons()) {
                    for(int r = 0; r < u.type.weapons.size; r++){
                        Weapon weapon = u.type.weapons.get(r);
                        WeaponMount mount = u.mounts[r];
                        int finalR = r;
                        tt.table(ttt -> {
                            ttt.left();
                            if((1 + finalR) % 4 == 0) ttt.row();
                            ttt.stack(
                                new Table(o -> {
                                    o.left();
                                    o.add(new Image(!weapon.name.equals("") && weapon.outlineRegion.found() ? weapon.outlineRegion : u.type.uiIcon){
                                        @Override
                                        public void draw(){
                                            validate();
                                            float x = this.x;
                                            float y = this.y;
                                            float scaleX = this.scaleX;
                                            float scaleY = this.scaleY;
                                            Draw.color(color);
                                            Draw.alpha(parentAlpha * color.a);

                                            if(getDrawable() instanceof TransformDrawable){
                                                float rotation = getRotation();
                                                if(scaleX != 1 || scaleY != 1 || rotation != 0){
                                                    getDrawable().draw(x + imageX, y + imageY, originX - imageX, originY - imageY, imageWidth, imageHeight, scaleX, scaleY, rotation);
                                                    return;
                                                }
                                            }
                                            y -= (mount.reload) / weapon.reload * weapon.recoil;
                                            if(getDrawable() != null)
                                                getDrawable().draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
                                        }
                                    }).size(iconLarge);
                                }),
                                new Table(h -> {
                                    h.defaults().growX().height(9f).width(iconLarge).padTop(18f);
                                    h.add(new SBar(
                                            () -> "",
                                            () -> Pal.accent.cpy().lerp(Color.orange, mount.reload / weapon.reload),
                                            () -> mount.reload / weapon.reload).rect().init());
                                    h.pack();
                                })
                            );
                        }).pad(4);
                    }
                }
            });
        }
    }
    static class UnitInfoDisplay extends Table {
        UnitInfoDisplay() {
            top();
            float[] count = new float[]{-1};
            table().update(t -> {
                if(getTarget() instanceof Payloadc payload){
                    if(count[0] != payload.payloadUsed()){
                        t.clear();
                        t.top().left();

                        float pad = 0;
                        float items = payload.payloads().size;
                        if(8 * 2 * items + pad * items > 275f){
                            pad = (275f - (8 * 2) * items) / items;
                        }
                        int i = 0;
                        for(Payload p : payload.payloads()){
                            t.image(p.icon()).size(8 * 2).padRight(pad);
                            if(++i % 12 == 0) t.row();
                        }

                        count[0] = payload.payloadUsed();
                    }
                }else{
                    count[0] = -1;
                    t.clear();
                }
            }).growX().visible(() -> getTarget() instanceof Payloadc p && p.payloadUsed() > 0).colspan(2).row();

            Bits statuses = new Bits();
            table().update(t -> {
                t.left();
                if(getTarget() instanceof Statusc st){
                    Bits applied = st.statusBits();
                    if(!statuses.equals(applied)){
                        t.clear();

                        if(applied != null){
                            for(StatusEffect effect : Vars.content.statusEffects()){
                                if(applied.get(effect.id) && !effect.isHidden()){
                                    t.image(effect.uiIcon).size(iconSmall).get()
                                        .addListener(new Tooltip(l -> l.label(() -> effect.localizedName + " [lightgray]" + UI.formatTime(st.getDuration(effect))).style(Styles.outlineLabel)));
                                }
                            }
                            statuses.set(applied);
                        }
                    }
                }
            }).left();
        }
    }
}
