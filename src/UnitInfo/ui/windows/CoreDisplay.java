package UnitInfo.ui.windows;

import UnitInfo.ui.*;
import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Element;
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
    ObjectMap<Team, ItemData> itemData = new ObjectMap<>();
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
            ScrollPane pane = new OverScrollPane(rebuild(), Styles.nonePane, scrollPos).disableScroll(true, false);
            t.add(pane).name("core-pane");
        }).top().right().grow().get().parent = null;

        resizeButton();
    }

    @Override
    public void update() {
        heat += Time.delta;
        if(heat >= 60f) {
            heat = 0f;
            ScrollPane pane = find("core-pane");
            pane.setWidget(rebuild());
            for(Team team : getTeams()) {
                if(!itemData.containsKey(team)) itemData.put(team, new ItemData());
                itemData.get(team).updateItems(team);
            }
        }
    }

    public Table rebuild() {
        return new Table(table -> {
            for(Team team : getTeams()) {
                table.add(setTable(team).background(((NinePatchDrawable)Tex.underline2).tint(team.color))).row();
            }
        });
    }
    public Seq<Team> getTeams(){
        return Seq.with(Team.all).filter(Team::active);
    }

    public void resetUsed(){
        for(Team team : getTeams()) {
            itemData.put(team, new ItemData());
        }
    }

    public Table setTable(Team team){
        return new Table(table -> {
            table.add(team.name).color(team.color).row();
            table.table(coretable -> {
                int row = 0;

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
                                HandCursorListener listener1 = new HandCursorListener();
                                image.addListener(listener1);
                                image.update(() -> {
                                    image.color.lerp(!listener1.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta));
                                });
                                Tooltip.Tooltips option = new Tooltip.Tooltips();
                                option.animations=false;
                                s.add(image).size(iconLarge).get().addListener(new Tooltip(tool -> {
                                    tool.background(Styles.black6).label(() -> "([#" + Tmp.c1.set(Color.green).lerp(Color.red, 1 - core.healthf()).toString() + "]" + Strings.fixed(core.health, 2) + "[]/" + Strings.fixed(core.block.health, 2) + ")");
                                }, option));
                            }),
                            new Table(h -> {
                                h.bottom().defaults().height(9f).width(iconLarge * 1.5f).growX();
                                h.add(new SBar(() -> "", () -> Pal.health, () -> core.health / core.block.health).rect().init());
                                h.pack();
                            })
                        ).row();
                        Label label = new Label(Strings.format("(@, @)",core.tileX(), core.tileY()));
                        label.setFontScale(0.75f);
                        tt.add(label);
                    }).padTop(2).padLeft(4).padRight(4);
                    if(++row % 5 == 0) coretable.row();
                }
            }).row();

            table.table(itemTable -> {
                int row = 0;

                CoreBlock.CoreBuild core = team.core();
                for(int i = 0; i < Vars.content.items().size; i++){
                    Item item = Vars.content.item(i);
                    if(!team.items().has(item)) return;
                    itemTable.stack(
                        new Table(ttt -> {
                            ttt.image(item.uiIcon).size(iconSmall).tooltip(tttt -> tttt.background(Styles.black6).add(item.localizedName).style(Styles.outlineLabel).margin(2f));
                            ttt.add(UI.formatAmount(core.items.get(item))).minWidth(5 * 8f).left();
                        }),
                        new Table(ttt -> {
                            ttt.bottom().right();
                            int amount = itemData.get(team).updateItems.isEmpty()?0:Mathf.floor(itemData.get(team).updateItems.get(item.id).amount);
                            Label label = new Label(amount + "/s");
                            label.setFontScale(0.65f);
                            label.setColor(amount > 0 ? Color.green : amount == 0 ? Color.orange : Color.red);
                            ttt.add(label).bottom().right().padTop(16f);
                            ttt.pack();
                        })).padRight(3).left();
                    if(++row % 5 == 0) itemTable.row();
                }
            }).row();

            table.table(unitTable -> {
                int row = 0;

                for(UnitType unit : Vars.content.units()){
                    if(unit != UnitTypes.block && Groups.unit.contains(u -> u.type == unit && u.team == team)){
                        unitTable.table(tt -> {
                            tt.center();
                            tt.image(unit.uiIcon).size(iconSmall).padRight(3).tooltip(ttt -> ttt.background(Styles.black6).add(unit.localizedName).style(Styles.outlineLabel).margin(2f));
                            tt.add(UI.formatAmount(Groups.unit.count(u -> u.team == team && u.type == unit))).padRight(3).minWidth(5 * 8f).left();
                        });
                        if(++row % 5 == 0) unitTable.row();
                    }
                }
            });
        });
    }

    static class ItemData {
        Seq<ItemStack> prevItems = new Seq<>();
        Seq<ItemStack> updateItems = new Seq<>();

        ItemData() {
            resetItems();
        }

        public void resetItems(){
            Seq<ItemStack> stacks = Vars.content.items().map(item -> new ItemStack(item, 0));
            updateItems.clear().addAll(stacks);
            prevItems.clear().addAll(stacks);
        }

        public void updateItems(Team team){
            CoreBlock.CoreBuild core = team.core();
            if (core != null) {
                Seq<ItemStack> stack = updateItems;
                if(stack.isEmpty()) Vars.content.items().each(i -> stack.add(new ItemStack(i, 0)));
                for (Item item : Vars.content.items()) {
                    stack.get(item.id).set(item, core.items.get(item) - (prevItems != null ? prevItems.get(item.id).amount : 0));
                    if (prevItems != null) prevItems.get(item.id).set(item, core.items.get(item));
                }

                if(prevItems != null) prevItems.clear().addAll(Vars.content.items().map(i -> new ItemStack(i, core.items.get(i))));
            }
        }
    }
}
