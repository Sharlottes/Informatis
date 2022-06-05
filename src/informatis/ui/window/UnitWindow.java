package informatis.ui.window;

import arc.*;
import arc.func.*;
import arc.scene.*;
import arc.scene.event.*;
import informatis.core.*;
import informatis.ui.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.Bits;
import arc.struct.*;
import arc.util.*;
import informatis.ui.widgets.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.payloads.*;

import static informatis.SVars.*;
import static informatis.SUtils.*;
import static mindustry.Vars.*;

public class UnitWindow extends Window {
    final Seq<Color> lastColors = new Seq<>();
    Teamc latestTarget;
    ScrollPane barPane;

    private int barSize = 6;
    private float usedPayload;
    private float barScrollPos;
    private final Bits statuses = new Bits();

    public UnitWindow() {
        super(Icon.units, "unit");
    }

    //TODO: add new UnitInfoDisplay(), new WeaponDisplay();
    @Override
    protected void build(Table table) {
        table.top().background(Styles.black8);
        table.table(title -> {
            title.center();
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
            title.add(image).size(iconMed).padRight(12f);
            title.label(() -> {
                if (target instanceof Unit u && u.type != null) return u.type.localizedName;
                if (target instanceof Building b && b.block != null) {
                    if (target instanceof ConstructBlock.ConstructBuild cb) return cb.current.localizedName;
                    return b.block.localizedName;
                }
                return "";
            }).color(Pal.accent);
        }).tooltip(tool -> {
            tool.background(Styles.black6);
            tool.table(to -> {
                to.label(() -> target instanceof Unit u ? u.isPlayer() ? u.getPlayer().name : "AI" : "").row();
                to.label(() -> target.tileX() + ", " + target.tileY()).row();
                to.label(() -> target instanceof Unit u ? "[accent]"+ Strings.fixed(u.armor, 0) + "[] Armor" : "");
            }).margin(12f);
        }).margin(12f).row();

        if(target instanceof Payloadc || target instanceof Statusc) {
            table.image().color((target == null ? player.unit() : target).team().color).height(4f).growX().row();

            table.table(state -> {
                state.left();
                final Cons<Table> rebuildPayload = t -> {
                    t.left();
                    if (target instanceof Payloadc payload) {
                        Seq<Payload> payloads = payload.payloads();
                        for (int i = 0, m = payload.payloads().size; i < m; i++) {
                            t.image(payloads.get(i).icon()).size(iconSmall);
                            if ((i + 1) % Math.max(6, Math.round((window.getWidth() - 24) / iconSmall)) == 0) t.row();
                        }
                    }
                };

                final Cons<Table> rebuildStatus = t -> {
                    t.top().left();
                    if (target instanceof Statusc st) {
                        Bits applied = st.statusBits();
                        if (applied != null) {
                            Seq<StatusEffect> contents = Vars.content.statusEffects();
                            for (int i = 0, m = Vars.content.statusEffects().size; i < m; i++) {
                                StatusEffect effect = contents.get(i);
                                if (applied.get(effect.id) && !effect.isHidden()) {
                                    t.image(effect.uiIcon).size(iconSmall).get()
                                            .addListener(new Tooltip(l -> l.label(() -> effect.localizedName + " [lightgray]" + UI.formatTime(st.getDuration(effect))).style(Styles.outlineLabel)));
                                }
                                if (i + 1 % Math.max(6, Math.round((window.getWidth() - 24) / iconSmall)) == 0) t.row();
                            }
                        }
                    }
                };

                final float[] lastWidth1 = {0};
                state.table(rebuildPayload).update(t -> {
                    t.left();
                    if (lastWidth1[0] != window.getWidth()) {
                        lastWidth1[0] = window.getWidth();
                        t.clear();
                        rebuildPayload.get(t);
                    } else if (target instanceof Payloadc payload) {
                        if (usedPayload != payload.payloadUsed()) {
                            usedPayload = payload.payloadUsed();
                            t.clear();
                            rebuildPayload.get(t);
                        }
                    } else {
                        usedPayload = -1;
                        t.clear();
                        rebuildPayload.get(t);
                    }
                }).grow().row();

                final float[] lastWidth2 = {0};
                state.table(rebuildStatus).update(t -> {
                    t.left();
                    if (lastWidth2[0] != window.getWidth()) {
                        lastWidth2[0] = window.getWidth();
                        t.clear();
                        rebuildStatus.get(t);
                    } else if (target instanceof Statusc st) {
                        Bits applied = st.statusBits();
                        if (applied != null && !statuses.equals(applied)) {
                            statuses.set(applied);
                            t.clear();
                            rebuildStatus.get(t);
                        }
                    } else {
                        statuses.clear();
                        t.clear();
                        rebuildStatus.get(t);
                    }
                }).grow();
            }).pad(12f).growX().row();
        }

        table.image().color((target==null?player.unit():target).team().color).height(4f).growX().row();

        barPane = new ScrollPane(buildBarList(), Styles.noBarPane);
        barPane.update(() -> {
            //rebuild whole bar table
            if(latestTarget != target) {
                for (int i = 0; i < barSize; i++) {
                    Color color = i >= BarInfo.data.size ? Color.clear : BarInfo.data.get(i).color;
                    if (i >= lastColors.size) lastColors.add(color);
                    else lastColors.set(i, color);
                }

                if(((Table) barPane.getWidget()).getChildren().size-1 != barSize) {
                    latestTarget = target;
                    barPane.setWidget(buildBarList());
                }
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
        table.add(barPane).grow().padTop(12f);
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
            bar.add(new SBar(
                    () -> index >= BarInfo.data.size ? "[lightgray]<Empty>[]" : BarInfo.data.get(index).name,
                    () -> index >= BarInfo.data.size ? Color.clear : BarInfo.data.get(index).color,
                    () -> lastColors.get(index),
                    () -> index >= BarInfo.data.size ? 0 : BarInfo.data.get(index).number)
            ).height(4 * 8f).growX();
            if(index >= BarInfo.data.size) return;
            Image icon = new Image(){
                @Override
                public void draw() {
                    validate();
                    if(index >= BarInfo.data.size) return;
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
}
