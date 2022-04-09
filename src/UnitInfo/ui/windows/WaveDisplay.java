package UnitInfo.ui.windows;

import UnitInfo.ui.OverScrollPane;
import arc.Events;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.event.HandCursorListener;
import arc.scene.event.Touchable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.StatusEffects;
import mindustry.game.EventType;
import mindustry.game.SpawnGroup;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.ui.*;

import static arc.Core.*;
import static arc.Core.settings;
import static mindustry.Vars.*;


public class WaveDisplay extends WindowTable {
    static Vec2 waveScrollPos = new Vec2(0, 0);

    public WaveDisplay() {
        super("Wave Display", Icon.waves, t -> {});
    }

    @Override
    public void build() {
        top();
        topBar();

        table(Styles.black8, t -> {
            ScrollPane pane = new OverScrollPane(rebuild(), Styles.nonePane, waveScrollPos).disableScroll(true, false);
            t.add(pane).get().parent = null;
            Events.on(EventType.WorldLoadEvent.class, e -> {
                pane.clearChildren();
                pane.setWidget(rebuild());
            });
            Events.on(EventType.WaveEvent.class, e -> {
                pane.clearChildren();
                pane.setWidget(rebuild());
            });
        }).top().right().grow().get().parent = null;

        resizeButton();
    }

    public ObjectIntMap<SpawnGroup> getWaveGroup(int index) {
        ObjectIntMap<SpawnGroup> groups = new ObjectIntMap<>();
        for (SpawnGroup group : state.rules.spawns) {
            if (group.getSpawned(index) <= 0) continue;
            SpawnGroup sameTypeKey = groups.keys().toArray().find(g -> g.type == group.type && g.effect != StatusEffects.boss);
            if (sameTypeKey != null) groups.increment(sameTypeKey, sameTypeKey.getSpawned(index));
            else groups.put(group, group.getSpawned(index));
        }
        Seq<SpawnGroup> groupSorted = groups.keys().toArray().copy().sort((g1, g2) -> {
            int boss = Boolean.compare(g1.effect != StatusEffects.boss, g2.effect != StatusEffects.boss);
            if (boss != 0) return boss;
            int hitSize = Float.compare(-g1.type.hitSize, -g2.type.hitSize);
            if (hitSize != 0) return hitSize;
            return Integer.compare(-g1.type.id, -g2.type.id);
        });
        ObjectIntMap<SpawnGroup> groupsTmp = new ObjectIntMap<>();
        groupSorted.each(g -> groupsTmp.put(g, groups.get(g)));

        return groupsTmp;
    }

    public Table rebuild(){
        return new Table(table -> {
            table.touchable = Touchable.enabled;
            for (int i = settings.getBool("pastwave") ? 0 : state.wave - 1;
                 i <= Math.min(state.wave + settings.getInt("wavemax"), (state.isCampaign() && state.rules.winWave > 0 ? state.rules.winWave : Integer.MAX_VALUE) - 2);
                 i++) {
                final int j = i;

                table.stack(
                    new Table(t -> {
                        t.label(() -> "[#" + (state.wave == j ? Color.red.toString() : Pal.accent.toString()) + "]" + j + "[]").padRight(24 * 8f);
                    }),
                    new Table(Tex.underline, t -> {
                        if (settings.getBool("emptywave") && state.rules.spawns.find(g -> g.getSpawned(j) > 0) == null) {
                            t.add(bundle.get("empty")).center();
                            return;
                        }

                        ObjectIntMap<SpawnGroup> groups = getWaveGroup(j-2);

                        int row = 0;
                        for (SpawnGroup group : groups.keys()) {
                            int spawners = state.rules.waveTeam.cores().size + (group.type.flying ? spawner.countFlyerSpawns() : spawner.countGroundSpawns());
                            int amount = groups.get(group);
                            t.stack(
                                new Table(ttt -> {
                                    ttt.center();
                                    ttt.image(group.type.uiIcon).size(iconMed);
                                    ttt.pack();
                                }),

                                new Table(ttt -> {
                                    ttt.bottom().left();
                                    ttt.add(amount+"").padTop(2f).fontScale(0.9f);
                                    ttt.add("[gray]x"+spawners).padTop(10f).fontScale(0.7f);
                                    ttt.pack();
                                }),

                                new Table(ttt -> {
                                    ttt.top().right();
                                    ttt.image(Icon.warning.getRegion()).update(img->img.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f)))).size(12f);
                                    ttt.visible(() -> group.effect == StatusEffects.boss);
                                    ttt.pack();
                                })
                            ).pad(2f).get().addListener(new Tooltip(to -> {
                                to.background(Styles.black6);
                                to.margin(4f).left();
                                to.add("[stat]" + group.type.localizedName + "[]").row();
                                to.row();
                                to.add(bundle.format("shar-stat-waveAmount", amount + " [lightgray]x" + spawners + "[]")).row();
                                to.add(bundle.format("shar-stat-waveShield", group.getShield(j))).row();
                                if (group.effect != null && group.effect != StatusEffects.none)
                                    to.add(bundle.get("shar-stat.waveStatus") + group.effect.emoji() + "[stat]" + group.effect.localizedName).row();
                            }));
                            if (++row % 4 == 0) t.row();
                        }
                    })
                );
                table.row();
            }
        });
    }
}
