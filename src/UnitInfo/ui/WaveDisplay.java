package UnitInfo.ui;

import UnitInfo.SVars;
import arc.graphics.Color;
import arc.graphics.g2d.NinePatch;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.event.HandCursorListener;
import arc.scene.style.NinePatchDrawable;
import arc.scene.style.ScaledNinePatchDrawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.game.SpawnGroup;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

import static UnitInfo.SVars.modUiScale;
import static arc.Core.*;
import static arc.Core.settings;
import static mindustry.Vars.*;


public class WaveDisplay extends Table {
    static float waveScrollPos;
    static Table table = new Table();

    public WaveDisplay() {
        fillParent = true;
        visibility = () -> 1 == SVars.hud.uiIndex;

        defaults().size(Scl.scl(modUiScale) * 35 * 8f);
        table(Tex.button, t -> {
            ScrollPane pane = t.pane(Styles.nonePane, rebuild()).get();
            pane.update(() -> {
                if (pane.hasScroll()) {
                    Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                    if (result == null || !result.isDescendantOf(pane)) {
                        scene.setScrollFocus(null);
                    }
                }
                waveScrollPos = pane.getScrollY();
            });
            pane.setOverscroll(false, false);
            pane.setScrollingDisabled(true, false);
            pane.setScrollYForce(waveScrollPos);

            update(() -> {
                NinePatchDrawable patch = (NinePatchDrawable) Tex.button;
                t.setBackground(patch.tint(Tmp.c1.set(patch.getPatch().getColor()).a(settings.getInt("uiopacity") / 100f)));
            });
        }).padRight(Scl.scl(modUiScale) * 70 * 8f);
    }

    public Table rebuild(){
        table.clear();
        int winWave = state.isCampaign() && state.rules.winWave > 0 ? state.rules.winWave : Integer.MAX_VALUE;
        for(int i = settings.getBool("pastwave") ? 0 : state.wave - 1; i <= Math.min(state.wave + settings.getInt("wavemax"), winWave - 2); i++){
            final int j = i;
            if(!settings.getBool("emptywave") && state.rules.spawns.find(g -> g.getSpawned(j) > 0) == null) continue;
            table.table(table1 -> {
                table1.stack(
                    new Table(t -> {
                        Label label = new Label(() -> "[#" + (state.wave == j ? Color.red.toString() : Pal.accent.toString()) + "]" + j + "[]");
                        label.setFontScale(Scl.scl(modUiScale));
                        t.add(label).padRight(Scl.scl(modUiScale) * 24 * 8f);
                    }),
                    new Table(Tex.underline, t -> {
                        t.marginLeft(Scl.scl(modUiScale) * 3 * 8f);
                        if(settings.getBool("emptywave") && state.rules.spawns.find(g -> g.getSpawned(j) > 0) == null) {
                            t.center();
                            Label label = new Label(bundle.get("empty"));
                            label.setFontScale(Scl.scl(modUiScale));
                            t.add(label);
                            return;
                        }

                        ObjectIntMap<SpawnGroup> groups = new ObjectIntMap<>();
                        for(SpawnGroup group : state.rules.spawns) {
                            if(group.getSpawned(j) <= 0) continue;
                            SpawnGroup sameTypeKey = groups.keys().toArray().find(g -> g.type == group.type && g.effect != StatusEffects.boss);
                            if(sameTypeKey != null) groups.increment(sameTypeKey, sameTypeKey.getSpawned(j));
                            else groups.put(group, group.getSpawned(j));
                        }
                        Seq<SpawnGroup> groupSorted = groups.keys().toArray().copy().sort((g1, g2) -> {
                            int boss = Boolean.compare(g1.effect != StatusEffects.boss, g2.effect != StatusEffects.boss);
                            if(boss != 0) return boss;
                            int hitSize = Float.compare(-g1.type.hitSize, -g2.type.hitSize);
                            if(hitSize != 0) return hitSize;
                            return Integer.compare(-g1.type.id, -g2.type.id);
                        });
                        ObjectIntMap<SpawnGroup> groupsTmp = new ObjectIntMap<>();
                        groupSorted.each(g -> groupsTmp.put(g, groups.get(g)));

                        int row = 0;
                        for(SpawnGroup group : groupsTmp.keys()){
                            int spawners = state.rules.waveTeam.cores().size + (group.type.flying ? spawner.countFlyerSpawns() : spawner.countGroundSpawns());
                            int amount = groupsTmp.get(group);
                            t.table(tt -> {
                                Image image = new Image(group.type.uiIcon).setScaling(Scaling.fit);
                                tt.stack(
                                    new Table(ttt -> {
                                        ttt.center();
                                        ttt.add(image).size(iconMed * Scl.scl(modUiScale));
                                        ttt.pack();
                                    }),

                                    new Table(ttt -> {
                                        ttt.bottom().left();
                                        Label label = new Label(() -> amount + "");
                                        label.setFontScale(Scl.scl(modUiScale) * 0.9f);
                                        Label multi = new Label(() -> "[gray]x" + spawners);
                                        multi.setFontScale(Scl.scl(modUiScale) * 0.7f);
                                        ttt.add(label).padTop(2f);
                                        ttt.add(multi).padTop(10f);
                                        ttt.pack();
                                    }),

                                    new Table(ttt -> {
                                        ttt.top().right();
                                        Image image1 = new Image(Icon.warning.getRegion()).setScaling(Scaling.fit);
                                        image1.update(() -> {
                                            image1.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f)));
                                        });
                                        ttt.add(image1).size(Scl.scl(modUiScale) * 12f);
                                        ttt.visible(() -> group.effect == StatusEffects.boss);
                                        ttt.pack();
                                    })
                                ).pad(2f * Scl.scl(modUiScale));
                                tt.clicked(() -> {
                                    if(input.keyDown(KeyCode.shiftLeft) && Fonts.getUnicode(group.type.name) != 0){
                                        app.setClipboardText((char)Fonts.getUnicode(group.type.name) + "");
                                        ui.showInfoFade("@copied");
                                    }else{
                                        ui.content.show(group.type);
                                    }
                                });
                                if(!mobile){
                                    HandCursorListener listener = new HandCursorListener();
                                    tt.addListener(listener);
                                    tt.update(() -> {
                                        image.color.lerp(!listener.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta));
                                    });
                                }
                                tt.addListener(new Tooltip(ttt -> ttt.table(Styles.black6, to -> {
                                    to.margin(4f).left();
                                    to.add("[stat]" + group.type.localizedName + "[]").row();
                                    to.row();
                                    to.add(bundle.format("shar-stat-waveAmount", amount + " [lightgray]x" + spawners + "[]")).row();
                                    to.add(bundle.format("shar-stat-waveShield", group.getShield(j))).row();
                                    if(group.effect != null && group.effect != StatusEffects.none)
                                        to.add(bundle.get("shar-stat.waveStatus") + group.effect.emoji() + "[stat]" + group.effect.localizedName).row();
                                })));
                            });
                            if(++row % 4 == 0) t.row();
                        }
                    })
                );
            });
            table.row();
        }

        return table;
    }
}
