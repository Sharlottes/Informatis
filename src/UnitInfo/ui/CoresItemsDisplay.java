package UnitInfo.ui;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.event.HandCursorListener;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.input.DesktopInput;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.storage.*;

import static UnitInfo.SVars.*;
import static arc.Core.*;
import static mindustry.Vars.*;

public class CoresItemsDisplay {
    private final ObjectMap<Team, ObjectSet<Item>> usedItems = new ObjectMap<>();
    private final ObjectMap<Team, ObjectSet<UnitType>> usedUnits = new ObjectMap<>();
    private final ObjectMap<Team, Seq<ItemStack>> prevItems = new ObjectMap<>();
    private final ObjectMap<Team, Seq<ItemStack>> updateItems = new ObjectMap<>();
    private final ObjectIntMap<Team> coreAmount = new ObjectIntMap<>();
    private CoreBlock.CoreBuild core;
    public Team[] teams;
    public Seq<Table> tables = new Seq<>();

    float heat;

    public CoresItemsDisplay(Team[] teams) {
        this.teams = teams;
        resetUsed();
    }

    public void resetUsed(){
        usedItems.clear();
        usedUnits.clear();
        updateItems.clear();
        prevItems.clear();
        coreAmount.clear();
        teams = Team.baseTeams;
        for(Team team : teams) {
            usedItems.put(team, new ObjectSet<>());
            usedUnits.put(team, new ObjectSet<>());
            Seq<ItemStack> stacks = new Seq<ItemStack>();
            content.items().each(i -> stacks.add(new ItemStack(i, 0)));
            updateItems.put(team, stacks);
            prevItems.put(team, stacks);
            coreAmount.put(team, team.cores().size);
        }
        tables.each(t->t.background(null));
        rebuild();
    }

    public void updateItem(Team team){
        if(prevItems.get(team) != null && core != null) for(Item item : content.items()){
            updateItems.get(team).get(item.id).set(item, core.items.get(item) - prevItems.get(team).get(item.id).amount);
            prevItems.get(team).get(item.id).set(item, core.items.get(item));
        }
        prevItems.clear();
        Seq<ItemStack> stacks = new Seq<ItemStack>();
        if(core != null) content.items().each(i -> stacks.add(new ItemStack(i, core.items.get(i))));
        prevItems.put(team, stacks);

    }

    public Table setTable(Team team){
        return new Table(t -> {
            t.clear();

            t.update(() -> {
                core = team.core();
                heat += Time.delta;

                if(heat >= settings.getInt("coreItemCheckRate")) {
                    heat = 0;
                    updateItem(team);
                }
                if(coreAmount.get(team) != team.cores().size){
                    coreAmount.put(team, team.cores().size);
                    rebuild();
                }
            });

            t.table(coretable -> {
                final int[] i = {0};
                for(CoreBlock.CoreBuild core : team.cores()) {
                    coretable.table(tt -> {
                        tt.stack(
                            new Table(s -> {
                                s.center();
                                Image image = new Image(core.block.uiIcon);
                                image.clicked(() -> {
                                    if(control.input instanceof DesktopInput)
                                        ((DesktopInput) control.input).panning = true;
                                    Core.camera.position.set(core.x, core.y);
                                });
                                if(!mobile) {
                                    HandCursorListener listener1 = new HandCursorListener();
                                    image.addListener(listener1);
                                    image.update(() -> {
                                        image.color.lerp(!listener1.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta));
                                    });
                                }
                                image.addListener(new Tooltip(tttt -> {
                                    Label label = new Label(() -> "([#" + Tmp.c1.set(Color.green).lerp(Color.red, 1 - core.healthf()).toString() + "]" + Strings.fixed(core.health, 2) + "[]/" + Strings.fixed(core.block.health, 2) + ")");
                                    label.setFontScale(Scl.scl(modUiScale));
                                    tttt.background(Tex.button).add(label);
                                }));
                                s.add(image).size(iconLarge * modUiScale);
                            }),
                            new Table(h -> {
                                h.bottom().defaults().height(Scl.scl(modUiScale) * 9f).width(Scl.scl(modUiScale) * iconLarge * 1.5f).growX();
                                h.add(new SBar(() -> "", () -> Pal.health, () -> core.health / core.block.health).rect().init());
                                h.pack();
                            })
                        );
                        tt.row();
                        Label label = new Label(() -> "(" + (int) core.x / 8 + ", " + (int) core.y / 8 + ")");
                        label.setFontScale(Scl.scl(modUiScale) * 0.75f);
                        tt.add(label);
                    }).padTop(Scl.scl(modUiScale) * 2).padLeft(Scl.scl(modUiScale) * 4).padRight(Scl.scl(modUiScale) * 4);
                    if(++i[0] % 5 == 0) coretable.row();
                }
            });
            t.row();
            t.table().update(itemTable -> {
                itemTable.clear();
                final int[] i = {0};
                for(Item item : content.items()){
                    if(team.core() != null && team.core().items.has(item)) {
                        itemTable.stack(
                            new Table(ttt -> {
                                ttt.image(item.uiIcon).size(iconSmall * modUiScale).tooltip(tttt -> tttt.background(Styles.black6).margin(2f * modUiScale).add(item.localizedName).style(Styles.outlineLabel));
                                Label label = new Label(() -> core == null ? "0" : UI.formatAmount(core.items.get(item)));
                                label.setFontScale(modUiScale);
                                ttt.add(label).minWidth(5 * 8f * modUiScale).left();
                            }),
                            new Table(ttt -> {
                                ttt.bottom().right();
                                Label label = new Label(() -> {
                                    int amount = (int)(updateItems.get(team).get(item.id).amount / ((settings.getInt("coreItemCheckRate") * 1f) / 60f));
                                    return (amount > 0 ? "[green]+" : amount == 0 ? "[orange]" : "[red]") + amount + "/s[]";
                                });
                                label.setFontScale(0.65f * modUiScale);
                                ttt.add(label).bottom().right().padTop(16f * modUiScale);
                                ttt.pack();
                            })
                            ).padRight(3 * modUiScale).left();
                        if(++i[0] % 5 == 0) itemTable.row();
                    }
                }
            });
            t.row();
            t.table().update(unitTable -> {
                unitTable.clear();
                final int[] i = {0};
                for(UnitType unit : content.units()){
                    unitTable.table(tt -> {
                        tt.center();
                        if(unit != UnitTypes.block && Groups.unit.contains(u -> u.type == unit && u.team == team)){
                            tt.image(unit.uiIcon).size(iconSmall * modUiScale).padRight(3 * modUiScale).tooltip(ttt -> ttt.background(Styles.black6).margin(2f * modUiScale).add(unit.localizedName).style(Styles.outlineLabel));
                            Label label = new Label(() -> core == null ? "0" : UI.formatAmount(Groups.unit.count(u -> u.team == team && u.type == unit)));
                            label.setFontScale(modUiScale);
                            tt.add(label).padRight(3 * modUiScale).minWidth(5 * 8f * modUiScale).left();
                            if(++i[0] % 5 == 0) unitTable.row();
                        }
                    });
                }
            });
        });
    }

    public void rebuild(){
        tables.clear();
        for(Team team : teams) {
            tables.add(setTable(team));
        }
    }
}
