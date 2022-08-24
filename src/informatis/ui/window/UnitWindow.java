package informatis.ui.window;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.scene.*;
import informatis.core.*;
import informatis.ui.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
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
    int barSize = 6;
    float usedPayload;
    float barScrollPos;
    final Seq<Color> lastColors = new Seq<>();
    final Bits statuses = new Bits();
    Teamc latestTarget;
    ScrollPane barPane;

    public UnitWindow() {
        super(Icon.units, "unit");
    }

    //TODO: add weapons
    @Override
    protected void build(Table table) {
        table.top().background(Styles.black8);
        table.table(profile -> {
            profile.table(title -> {
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
                Label label = title.label(() -> {
                    if (target instanceof Unit u && u.type != null) return u.type.localizedName;
                    if (target instanceof Building b && b.block != null) {
                        if (target instanceof ConstructBlock.ConstructBuild cb) return cb.current.localizedName;
                        return b.block.localizedName;
                    }
                    return "";
                }).color(Pal.accent).get();
                label.clicked(() -> moveCamera(target));
            }).tooltip(tool -> {
                tool.table(Styles.black6, to -> {
                    to.label(() -> target instanceof Unit u ? u.isPlayer() ? u.getPlayer().name : "AI" : "").visible(target instanceof Unit).row();
                    to.label(() -> target.tileX() + ", " + target.tileY()).row();
                    to.label(() -> target instanceof Unit u ? "[accent]"+ Strings.fixed(u.armor, 0) + "[] Armor" : "").visible(target instanceof Unit);
                });
            });
            profile.table(weapon -> {
                weapon.add(new WeaponDisplay());
            });
        }).margin(12f).row();
        table.image().color((target == null ? player.unit() : target).team().color).visible(()-> {
            if(target instanceof Payloadc payload) return payload.payloads().size > 0;
            if(target instanceof Statusc status) {
                Bits applied = status.statusBits();
                return applied == null || Vars.content.statusEffects().contains(effect -> applied.get(effect.id) && !effect.isHidden());
            }
            return false;
        }).height(4f).growX().row();

        table.table(state -> {
            state.left();
            final Cons<Table> rebuildPayload = t -> {
                t.left();
                if (target instanceof Payloadc payloader) {
                    Seq<Payload> payloads = payloader.payloads();
                    for (int i = 0, m = payloader.payloads().size; i < m; i++) {
                        Payload payload = payloads.get(i);
                        Image image = new Image(payload.icon());
                        image.clicked(()->ui.content.show(payload.content()));
                        image.hovered(()->image.setColor(Tmp.c1.set(image.color).lerp(Color.lightGray, Mathf.clamp(Time.delta))));
                        image.exited(()->image.setColor(Tmp.c1.set(image.color).lerp(Color.white, Mathf.clamp(Time.delta))));
                        t.add(image).size(iconSmall).tooltip(l -> l.label(() -> payload.content().localizedName).style(Styles.outlineLabel));
                        if ((i + 1) % Math.max(6, Math.round((window.getWidth() - 24) / iconSmall)) == 0) t.row();
                    }
                }
            };

            final Cons<Table> rebuildStatus = t -> {
                t.top().left();
                if (target instanceof Statusc st) {
                    Bits applied = st.statusBits();
                    if (applied == null) return;
                    Seq<StatusEffect> contents = Vars.content.statusEffects();
                    for (int i = 0, m = Vars.content.statusEffects().size; i < m; i++) {
                        StatusEffect effect = contents.get(i);
                        if (applied.get(effect.id) && !effect.isHidden()) {
                            Image image = new Image(effect.uiIcon);
                            image.clicked(()->ui.content.show(effect));
                            image.hovered(()->image.setColor(Tmp.c1.set(image.color).lerp(Color.lightGray, Mathf.clamp(Time.delta))));
                            image.exited(()->image.setColor(Tmp.c1.set(image.color).lerp(Color.white, Mathf.clamp(Time.delta))));
                            t.add(image).size(iconSmall).tooltip(l -> l.label(() -> effect.localizedName + " [lightgray]" + UI.formatTime(st.getDuration(effect))).style(Styles.outlineLabel));
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
        }).minHeight(0).growX().row();

        table.image().color((target == null ? player.unit() : target).team().color).height(4f).growX().row();

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

    boolean show;
    Teamc targetCache;
    Table buildBarList() {
        show = false;
        return new Table(table -> {
            table.top();
            for (int i = 0; i < barSize; i++) {
                table.add(addBar(i)).growX().get();
                table.row();
            }
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
                    if(hasMouse()) getDrawable().draw(x, y, width, height);
                    else {
                        data.icon.draw(x, y, width, height);
                        if(ScissorStack.push(Tmp.r1.set(ScissorStack.peek().x + x,  ScissorStack.peek().y + y, width, height * data.number))) {
                            Draw.color(data.color);
                            data.icon.draw(x, y, width, height);
                            ScissorStack.pop();
                        }
                    }
                }
            };
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
                        tt.table(ttt -> {
                            ttt.left();
                            ttt.stack(
                                new Table(o -> {
                                    o.left();
                                    o.add(new Image(weapon.region){
                                        @Override
                                        public void draw(){
                                            y -= (mount.reload) / weapon.reload * weapon.recoil;
                                           super.draw();
                                        }
                                    }).size(iconLarge).scaling(Scaling.bounded);
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
                        if(r % 4 == 0) tt.row();
                    }
                }
            });
        }
    }
}
