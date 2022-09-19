package informatis.ui.windows;

import arc.*;
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
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.payloads.*;

import java.util.Objects;

import static informatis.SVars.*;
import static informatis.SUtils.*;
import static mindustry.Vars.*;

public class UnitWindow extends Window {
    int barSize = 6;
    float usedPayload;
    float barScrollPos;
    float lastWidth;
    final Seq<Color> lastColors = new Seq<>();
    final Bits statuses = new Bits();
    Teamc lastTarget;

    public UnitWindow() { super(Icon.units, "unit"); }

    @Override
    protected void build(Table table) {
        Image profileImage = new Image() {
            final int size = 8;
            @Override
            public void draw() {
                super.draw();

                Draw.color(locked? Pal.accent:Pal.gray);
                Draw.alpha(parentAlpha);
                Lines.stroke(Scl.scl(3f));
                Lines.rect(x-size/2f, y-size/2f, width+size, height+size);
                Draw.reset();
            }
        };
        profileImage.update(() -> {
            TextureRegion region = clear;
            if (target instanceof Unit u && u.type != null) region = u.type.uiIcon;
            else if (target instanceof Building b) {
                if (b instanceof ConstructBlock.ConstructBuild cb) region = cb.current.uiIcon;
                else if (b.block != null) region = b.block.uiIcon;
            }
            profileImage.setDrawable(region);
        });
        profileImage.clicked(() -> {
            if (target == getTarget()) locked = !locked;
            target = getTarget();
        });
        Label profileLabel = new Label(() -> {
            if (target instanceof Unit u && u.type != null) return u.type.localizedName;
            if (target instanceof Building b && b.block != null) {
                if (target instanceof ConstructBlock.ConstructBuild cb) return cb.current.localizedName;
                return b.block.localizedName;
            }
            return "";
        });
        profileLabel.clicked(() -> moveCamera(target));

        ScrollPane barPane = new ScrollPane(buildBarList(), Styles.noBarPane);
        barPane.update(() -> {
            if (lastTarget != target) {
                lastTarget = target;
                for (int i = 0; i < barSize; i++) {
                    Color color = i >= BarInfo.data.size ? Color.clear : BarInfo.data.get(i).color;
                    if (i >= lastColors.size) lastColors.add(color);
                    else lastColors.set(i, color);
                }
            }

            if (((Table) barPane.getWidget()).getChildren().size - 1 != barSize) {
                barPane.setWidget(buildBarList());
            }

            if (barPane.hasScroll()) {
                Element result = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if (result == null || !result.isDescendantOf(barPane)) {
                    Core.scene.setScrollFocus(null);
                }
            }
            barScrollPos = barPane.getScrollY();
        });
        barPane.setScrollingDisabledX(true);
        barPane.setScrollYForce(barScrollPos);

        table.top().background(Styles.black8);
        table.table(profile -> profile
            .table(title -> {
                title.center();
                title.add(profileImage).size(iconMed);
                title.add(profileLabel).padLeft(12f).padRight(12f).color(Pal.accent);
            })
            .tooltip(tool -> {
                tool.background(Styles.black6);
                tool.label(() -> target instanceof Unit u ? u.isPlayer() ? u.getPlayer().name : "AI" : "").row();
                tool.label(() -> target.tileX() + ", " + target.tileY()).row();
                tool.label(() -> target instanceof Unit u ? "[accent]"+ Strings.fixed(u.armor, 0) + "[] Armor" : "");
            }).get()
        ).margin(3f).growX().row();
        table.table().update(tt -> {
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
                                    o.add(new Image(Core.atlas.isFound(weapon.region) ? weapon.region : u.type.uiIcon){
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
                    if((r + 1) % 4 == 0) tt.row();
                }
            }
        }).margin(4f).growX().row();
        table.table(state -> {
            state.left();
            state.table().update(t -> {
                if (!(target instanceof Payloadc payloader)) {
                    t.clear();
                    usedPayload = -1;
                    return;
                }

                if(usedPayload == payloader.payloadUsed() && lastWidth == window.getWidth()) return;
                if(usedPayload != payloader.payloadUsed()) usedPayload = payloader.payloadUsed();
                if(lastWidth != window.getWidth()) lastWidth = window.getWidth();

                t.clear();
                t.top().left();
                Seq<Payload> payloads = payloader.payloads();
                for (int i = 0, m = payloads.size; i < m; i++) {
                    Payload payload = payloads.get(i);
                    Image image = new Image(payload.icon());
                    image.clicked(() -> ui.content.show(payload.content()));
                    image.hovered(() -> image.setColor(Tmp.c1.set(image.color).lerp(Color.lightGray, Mathf.clamp(Time.delta))));
                    image.exited(() -> image.setColor(Tmp.c1.set(image.color).lerp(Color.white, Mathf.clamp(Time.delta))));
                    t.add(image).size(iconSmall).tooltip(l -> l.label(() -> payload.content().localizedName).style(Styles.outlineLabel));
                    if ((i + 1) % Math.max(6, Math.round((window.getWidth() - 24) / iconSmall)) == 0) t.row();
                }
            });

            state.table().update(t -> {
                if (!(target instanceof Statusc st)) {
                    t.clear();
                    statuses.clear();
                    return;
                }
                Bits applied = st.statusBits();

                if((applied == null || statuses.equals(st.statusBits())) && lastWidth == window.getWidth()) return;
                if(!statuses.equals(st.statusBits())) statuses.set(applied);
                if(lastWidth != window.getWidth()) lastWidth = window.getWidth();

                t.clear();
                t.top().left();
                Seq<StatusEffect> contents = Vars.content.statusEffects();
                for (int i = 0, m = Vars.content.statusEffects().size; i < m; i++) {
                    StatusEffect effect = contents.get(i);
                    if (Objects.requireNonNull(applied).get(effect.id) && !effect.isHidden()) {
                        Image image = new Image(effect.uiIcon);
                        image.clicked(() -> ui.content.show(effect));
                        image.hovered(() -> image.setColor(Tmp.c1.set(image.color).lerp(Color.lightGray, Mathf.clamp(Time.delta))));
                        image.exited(() -> image.setColor(Tmp.c1.set(image.color).lerp(Color.white, Mathf.clamp(Time.delta))));
                        t.add(image).size(iconSmall).tooltip(l -> l.label(() -> effect.localizedName + " [lightgray]" + UI.formatTime(st.getDuration(effect))).style(Styles.outlineLabel));
                        if (i + 1 % Math.max(6, Math.round((window.getWidth() - 24) / iconSmall)) == 0) t.row();
                    }
                }
            });
        }).growX().row();
        table.image().color((target == null ? player.unit() : target).team().color).height(4f).growX().row();
        table.add(barPane).grow().padTop(12f);
    }

    boolean show;
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
}
