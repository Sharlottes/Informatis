package UnitInfo.ui.windows;

import UnitInfo.ui.OverScrollPane;
import UnitInfo.ui.SBar;
import UnitInfo.ui.Updatable;
import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.event.HandCursorListener;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.core.UI;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.input.DesktopInput;
import mindustry.type.*;
import mindustry.ui.Styles;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.*;

public class CoreDisplay extends WindowTable implements Updatable {
    Vec2 scrollPos = new Vec2(0, 0);
    ObjectMap<Team, ObjectSet<Item>> usedItems = new ObjectMap<>();
    ObjectMap<Team, ObjectSet<UnitType>> usedUnits = new ObjectMap<>();
    ObjectMap<Team, Seq<ItemStack>> prevItems = new ObjectMap<>();
    ObjectMap<Team, Seq<ItemStack>> updateItems = new ObjectMap<>();
    ObjectIntMap<Team> coreAmount = new ObjectIntMap<>();
    float heat;

    public CoreDisplay()  {
        super("Core Display", Icon.list, t -> {});
        resetUsed();
    }

    @Override
    public void build() {
        scrollPos = new Vec2(0, 0);

        top();
        topBar();

        table(Styles.black8, t -> {
            ScrollPane pane = new OverScrollPane(new Table(table -> {
                for(Team team : Team.baseTeams) {
                    table.add(setTable(team).background(((NinePatchDrawable)Tex.underline2).tint(team.color))).row();
                }
            }), Styles.nonePane, scrollPos).disableScroll(true, false);
            t.add(pane);
        }).top().right().grow().get().parent = null;

        resizeButton();
    }

    @Override
    public void update() {
        heat += Time.delta;
        if(heat >= 60f) {
            heat = 0f;
            for(Team team : Team.baseTeams) {
                if(team==Team.sharded) Log.info(prevItems.get(Team.sharded));
                updateItem(team);
                Log.info(prevItems.get(Team.sharded));
                if(coreAmount.get(team) != team.cores().size){
                    coreAmount.put(team, team.cores().size);
                }
            }
        }
    }

    public void resetUsed(){
        usedItems.clear();
        usedUnits.clear();
        updateItems.clear();
        prevItems.clear();
        coreAmount.clear();
        for(Team team : Team.baseTeams) {
            usedItems.put(team, new ObjectSet<>());
            usedUnits.put(team, new ObjectSet<>());
            Seq<ItemStack> stacks = new Seq<>();
            Vars.content.items().each(i -> stacks.add(new ItemStack(i, 0)));
            updateItems.put(team, stacks);
            prevItems.put(team, stacks);
            coreAmount.put(team, team.cores().size);
        }
    }

    public void updateItem(Team team){
        CoreBlock.CoreBuild core = team.core();
        Seq<ItemStack> prev = prevItems.get(team);
        if (core != null) {
            Seq<ItemStack> stack = updateItems.get(team);
            if(stack.isEmpty()) Vars.content.items().each(i -> stack.add(new ItemStack(i, 0)));
            for (Item item : Vars.content.items()) {
                stack.get(item.id).set(item, core.items.get(item) - (prev != null ? prev.get(item.id).amount : 0));
                if (prev != null) prev.get(item.id).set(item, core.items.get(item));
            }
        }
        if (prev != null) prev.clear();
        Seq<ItemStack> stacks = new Seq<>();
        if(core != null) Vars.content.items().each(i -> stacks.add(new ItemStack(i, core.items.get(i))));
        prevItems.put(team, stacks);
    }

    public Table setTable(Team team){
        return new Table(table -> {
            table.label(() -> "[#" + team.color.toString() + "]" + team.name + "[]").row();
            table.table().update(coretable -> {
                coretable.clear();

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

                                s.add(image).size(iconLarge).tooltip(tool -> {
                                    tool.background(Tex.button).label(() -> "([#" + Tmp.c1.set(Color.green).lerp(Color.red, 1 - core.healthf()).toString() + "]" + Strings.fixed(core.health, 2) + "[]/" + Strings.fixed(core.block.health, 2) + ")");
                                });
                            }),
                            new Table(h -> {
                                h.bottom().defaults().height(9f).width(iconLarge * 1.5f).growX();
                                h.add(new SBar(() -> "", () -> Pal.health, () -> core.health / core.block.health).rect().init());
                                h.pack();
                            })
                        ).row();
                        Label label = new Label(() -> "(" + (int) core.x / 8 + ", " + (int) core.y / 8 + ")");
                        label.setFontScale(0.75f);
                        tt.add(label);
                    }).padTop(2).padLeft(4).padRight(4);
                    if(++i[0] % 4 == 0) coretable.row();
                }
            }).row();

            table.table().update(itemTable -> {
                itemTable.clear();
                final int[] i = {0};
                CoreBlock.CoreBuild core = team.core();
                if(core != null) for(Item item : Vars.content.items().copy().filter(item-> core.items.has(item))){
                    itemTable.stack(
                        new Table(ttt -> {
                            ttt.image(item.uiIcon).size(iconSmall).tooltip(tttt -> tttt.background(Styles.black6).add(item.localizedName).style(Styles.outlineLabel).margin(2f));
                            ttt.add(UI.formatAmount(core.items.get(item))).minWidth(5 * 8f).left();
                        }),
                        new Table(ttt -> {
                            ttt.bottom().right();
                            int amount = updateItems.get(team).isEmpty()?0:Mathf.floor(updateItems.get(team).get(item.id).amount);
                            Label label = new Label((amount > 0 ? "[green]+" : amount == 0 ? "[orange]" : "[red]") + amount + "/s[]");
                            label.setFontScale(0.65f);
                            ttt.add(label).bottom().right().padTop(16f);
                            ttt.pack();
                        })).padRight(3).left();
                    if(++i[0] % 5 == 0) itemTable.row();
                }
            }).row();

            table.table().update(unitTable -> {
                unitTable.clear();

                final int[] i = {0};
                for(UnitType unit : Vars.content.units()){
                    if(unit != UnitTypes.block && Groups.unit.contains(u -> u.type == unit && u.team == team)){
                        unitTable.table(tt -> {
                            tt.center();
                            tt.image(unit.uiIcon).size(iconSmall).padRight(3).tooltip(ttt -> ttt.background(Styles.black6).add(unit.localizedName).style(Styles.outlineLabel).margin(2f));
                            tt.add(UI.formatAmount(Groups.unit.count(u -> u.team == team && u.type == unit))).padRight(3).minWidth(5 * 8f).left();
                        });
                        if(++i[0] % 5 == 0) unitTable.row();
                    }
                }
            });
        });
    }
}
