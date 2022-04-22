package unitinfo.ui.windows;

import mindustry.game.Team;
import unitinfo.ui.OverScrollPane;
import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
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
import unitinfo.ui.Updatable;

import static arc.Core.*;
import static arc.Core.settings;
import static mindustry.Vars.*;


public class WaveDisplay extends Window implements Updatable {
    static Vec2 scrollPos = new Vec2(0, 0);
    Table window;
    float heat;

    public WaveDisplay() {
        super(Icon.waves, "wave");
    }

    @Override
    public void build(Table table) {
        window = table;

        table.top().background(Styles.black8);

        ScrollPane pane = new OverScrollPane(rebuild(), Styles.nonePane, scrollPos).disableScroll(true, false);
        table.add(pane).grow().name("wave-pane");
        Events.on(EventType.WorldLoadEvent.class, e -> {
            pane.clearChildren();
            pane.setWidget(rebuild());
        });
        Events.on(EventType.WaveEvent.class, e -> {
            pane.clearChildren();
            pane.setWidget(rebuild());
        });
    }

    @Override
    public void update() {
        heat += Time.delta;
        if(heat >= 60f) {
            heat = 0f;
            ScrollPane pane = find("wave-pane");
            pane.setWidget(rebuild());
        }
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

    Table rebuild(){
        return new Table(table -> {
            table.touchable = Touchable.enabled;
            table.table(body->{
                body.center();
                for (int i = settings.getBool("pastwave") ? 1 : state.wave; i <= Math.min(state.wave + settings.getInt("wavemax"), (state.isCampaign() && state.rules.winWave > 0 ? state.rules.winWave : Integer.MAX_VALUE)); i++) {
                    final int index = i;

                    body.table(waveRow -> {
                        waveRow.left();

                        waveRow.add(index+"").update(label -> {
                            Color color = Pal.accent;
                            if (state.wave == index) color = Color.red;
                            else if (state.wave - 1 == index && state.enemies > 0) color = Color.red.cpy().shiftHue(Time.time);

                            label.setColor(color);
                        });
                        waveRow.table(unitTable -> {
                            unitTable.center();

                            if (state.rules.spawns.find(g -> g.getSpawned(index-1) > 0) == null) {
                                if (settings.getBool("emptywave")) unitTable.add(bundle.get("empty"));
                                return;
                            }

                            ObjectIntMap<SpawnGroup> groups = getWaveGroup(index-1);

                            int row = 0;
                            int max = Math.max(1, Math.round(window.getWidth()/2/8));
                            for (SpawnGroup group : groups.keys()) {
                                int spawners = state.rules.waveTeam.cores().size + (group.type.flying ? spawner.countFlyerSpawns() : spawner.countGroundSpawns());
                                int amount = groups.get(group);
                                unitTable.stack(
                                    new Table(ttt -> {
                                        ttt.center();
                                        ttt.image(group.type.uiIcon).size(iconMed);
                                        ttt.pack();
                                    }),

                                    new Table(ttt -> {
                                        ttt.bottom().left();
                                        ttt.add(amount + "").padTop(2f).fontScale(0.9f);
                                        ttt.add("[gray]x" + spawners).padTop(10f).fontScale(0.7f);
                                        ttt.pack();
                                    }),

                                    new Table(ttt -> {
                                        ttt.top().right();
                                        ttt.image(Icon.warning.getRegion()).update(img -> img.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f)))).size(12f);
                                        ttt.visible(() -> group.effect == StatusEffects.boss);
                                        ttt.pack();
                                    })
                                ).pad(2f).get().addListener(new Tooltip(to -> {
                                    to.background(Styles.black6);
                                    to.margin(4f).left();
                                    to.add("[stat]" + group.type.localizedName + "[]").row();
                                    to.row();
                                    to.add(bundle.format("shar-stat-waveAmount", amount + " [lightgray]x" + spawners + "[]")).row();
                                    to.add(bundle.format("shar-stat-waveShield", group.getShield(index-1))).row();
                                    if (group.effect != null && group.effect != StatusEffects.none)
                                        to.add(bundle.get("shar-stat.waveStatus") + group.effect.emoji() + "[stat]" + group.effect.localizedName).row();
                                }));
                                if(row++ % max == max-1){
                                    unitTable.row();
                                }
                            }
                        }).growX().margin(12f);
                    }).growX().row();
                    body.image().height(4f).color(Pal.gray).growX().row();
                }
            }).grow().margin(40f);
        });
    }
}
