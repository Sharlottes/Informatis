package unitinfo.ui.windows;

import unitinfo.ui.EditorTool;
import unitinfo.ui.OverScrollPane;
import unitinfo.ui.Updatable;
import arc.Core;
import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.scene.utils.Elem;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.Vars.*;

public class MapEditorDisplay extends Window implements Updatable {
    Vec2 scrollPos = new Vec2(0, 0);
    TextField search;
    Table window;
    float heat;
    float brushSize = -1;
    unitinfo.ui.EditorTool tool;

    public static Team drawTeam = Team.sharded;
    public static Block selected = Blocks.router;

    public MapEditorDisplay()  {
        super(Icon.map, "editor");
    }

    @Override
    public void build(Table table) {
        scrollPos = new Vec2(0, 0);
        search = Elem.newField(null, f->{});
        search.setMessageText(Core.bundle.get("players.search")+"...");
        window = table;

        table.top().background(Styles.black8);
        table.table(t->{
            t.left().background(Tex.underline2);
            t.label(()->"[accent]"+selected.localizedName+"[] "+selected.emoji());
            t.add(search).growX().pad(8).name("search");
        }).growX().row();
        table.add(new OverScrollPane(rebuild(), Styles.nonePane, scrollPos).disableScroll(true, false)).grow().name("editor-pane");
    }

    boolean hold = false;
    int pastX, pastY;
    @Override
    public void update() {
        heat += Time.delta;
        if(heat >= 60f) {
            heat = 0f;
            resetPane();
        }
        if(tool != null) {
            if(Core.input.isTouched()) {
                if(!(!mobile&&Core.input.keyDown(KeyCode.mouseLeft))) return;
                if(tool== EditorTool.line) {
                    if(!hold) {
                        pastX = (int) player.mouseX / 8;
                        pastY = (int) player.mouseY / 8;
                    }
                    hold = true;
                }
                else {
                    pastX = (int) player.mouseX / 8;
                    pastY = (int) player.mouseY / 8;
                }

                tool.touched(pastX, pastY);
            }
            else if(tool== EditorTool.line) {
                if(hold&&pastX>=0&&pastY>=0) tool.touchedLine(pastX, pastY, (int) player.mouseX/8, (int) player.mouseY/8);
                hold = false;
                pastX = -1;
                pastY = -1;
            }
        }
    }

    void resetPane() {
        ScrollPane pane = find("editor-pane");
        pane.setWidget(rebuild());
    }

    public Table rebuild() {
        return new Table(table-> {
            table.top();
            Seq<Block> blocks = Vars.content.blocks().copy();
            if(search.getText().length() > 0){
                blocks.filter(p -> p.name.toLowerCase().contains(search.getText().toLowerCase())||p.localizedName.toLowerCase().contains(search.getText().toLowerCase()));
            }
            table.table(select->buildTable(Blocks.boulder, select, blocks, ()->selected, block->selected=block, false)).marginTop(16f).marginBottom(16f).row();
            table.image().height(4f).color(Pal.gray).growX().row();
            table.table(body-> {
                body.table(tools -> {
                    tools.top().left();
                    tools.table(title -> title.left().background(Tex.underline2).add("Tools [accent]"+(tool==null?"":tool.name())+"[]")).growX().row();
                    tools.table(bt->{
                        Cons<unitinfo.ui.EditorTool> addTool = tool -> {
                            ImageButton button = new ImageButton(ui.getIcon(tool.name()), Styles.clearTogglei);
                            button.clicked(() -> {
                                button.toggle();
                                if(this.tool==tool) this.tool = null;
                                else this.tool = tool;
                                resetPane();
                            });
                            button.update(()->button.setChecked(this.tool == tool));

                            Label mode = new Label("");
                            mode.setColor(Pal.remove);
                            mode.update(() -> mode.setText(tool.mode == -1 ? "" : "M" + (tool.mode + 1) + " "));
                            mode.setAlignment(Align.bottomRight, Align.bottomRight);
                            mode.touchable = Touchable.disabled;

                            bt.stack(button, mode);
                        };

                        addTool.get(unitinfo.ui.EditorTool.line);
                        addTool.get(unitinfo.ui.EditorTool.pencil);
                        addTool.get(unitinfo.ui.EditorTool.eraser);
                        addTool.get(unitinfo.ui.EditorTool.fill);
                        addTool.get(unitinfo.ui.EditorTool.spray);
                    });
                    tools.row();
                    Slider slider = new Slider(1, 16, 1, false);
                    slider.moved(size->brushSize=size);
                    slider.setValue(brushSize);
                    Label label = new Label("Brush: "+brushSize);
                    label.touchable = Touchable.disabled;
                    tools.stack(slider, label).width(window.getWidth()/5).center();
                }).left().width(window.getWidth() / 2).margin(8f).growY();
                body.image().width(4f).height(body.getHeight()).color(Pal.gray).growY();
                body.table(options -> {
                    options.top().left();
                    options.table(title -> title.left().background(Tex.underline2).add("Options [accent]"+(tool!=null&&tool.mode>=0&&tool.mode<tool.altModes.length?tool.altModes[tool.mode]:"")+"[]")).growX().row();
                    options.table(option-> {
                        option.defaults().size(300f, 70f).left();
                        if(tool==null) return;

                        for (int i = 0; i < tool.altModes.length; i++) {
                            int mode = i;
                            String name = tool.altModes[i];

                            option.button(b -> {
                                b.left().marginLeft(6);
                                b.setStyle(Styles.clearTogglet);
                                b.add(Core.bundle.get("toolmode." + name)).left().row();
                                b.add(Core.bundle.get("toolmode." + name + ".description")).color(Color.lightGray).left();
                            }, () -> {
                                tool.mode = (tool.mode == mode ? -1 : mode);
                            }).update(b -> b.setChecked(tool.mode == mode));
                            option.row();
                        }
                    });
                }).left().width(window.getWidth() / 2).margin(8f).growY();
            }).grow();
        });
    }

    <T extends Block> void buildTable(@Nullable Block block, Table table, Seq<T> items, Prov<T> holder, Cons<T> consumer, boolean closeSelect){
        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        Table cont = new Table();
        cont.defaults().size(40);

        int i = 0;
        int max = Math.max(4, Math.round(window.getWidth()/64));

        for(T item : items){
            if(!item.unlockedNow()) continue;

            ImageButton button = cont.button(Tex.whiteui, Styles.clearToggleTransi, 24, () -> {
                if(closeSelect) control.input.frag.config.hideConfig();
            }).group(group).tooltip(t->t.background(Styles.black8).add(item.localizedName.replace(search.getText(), "[accent]"+search.getText()+"[]"))).get();
            button.changed(() -> consumer.get(button.isChecked() ? item : null));
            button.getStyle().imageUp = new TextureRegionDrawable(item.uiIcon);
            button.update(() -> button.setChecked(holder.get() == item));

            if(i++ % max == max-1){
                cont.row();
            }
        }

        //add extra blank spaces so it looks nice
        if(i % max != 0){
            int remaining = max - (i % max);
            for(int j = 0; j < remaining; j++){
                cont.image(Styles.black6);
            }
        }

        ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
        pane.setScrollingDisabled(true, false);

        if(block != null){
            pane.setScrollYForce(block.selectScroll);
            pane.update(() -> {
                block.selectScroll = pane.getScrollY();
            });
        }

        pane.setOverscroll(false, false);
        table.add(pane).maxHeight(Scl.scl(40 * 5));
    }

    public void drawBlocksReplace(int x, int y){
        drawBlocks(x, y, tile -> tile.block() != Blocks.air || selected.isFloor());
    }

    public void drawBlocks(int x, int y){
        drawBlocks(x, y, false, tile -> true);
    }

    public void drawBlocks(int x, int y, Boolf<Tile> tester){
        drawBlocks(x, y, false, tester);
    }

    int rotation = 0;
    public void drawBlocks(int x, int y, boolean square, Boolf<Tile> tester){
        if(selected.isMultiblock()){
            x = Mathf.clamp(x, (selected.size - 1) / 2, world.width() - selected.size / 2 - 1);
            y = Mathf.clamp(y, (selected.size - 1) / 2, world.height() - selected.size / 2 - 1);
            if(!hasOverlap(x, y)){
                world.tile(x, y).setBlock(selected, drawTeam, rotation);
            }
        }else{
            boolean isFloor = selected.isFloor() && selected != Blocks.air;

            Cons<Tile> drawer = tile -> {
                if(!tester.get(tile)) return;

                if(isFloor){
                    tile.setFloor(selected.asFloor());
                }else if(!(tile.block().isMultiblock() && !selected.isMultiblock())){
                    tile.setBlock(selected, drawTeam, rotation);
                }
            };

            if(square){
                drawSquare(x, y, drawer);
            }else{
                drawCircle(x, y, drawer);
            }
        }
    }

    public void drawCircle(int x, int y, Cons<Tile> drawer){
        int clamped = (int)brushSize;
        for(int rx = -clamped; rx <= clamped; rx++){
            for(int ry = -clamped; ry <= clamped; ry++){
                if(Mathf.within(rx, ry, brushSize - 0.5f + 0.0001f)){
                    int wx = x + rx, wy = y + ry;

                    if(wx < 0 || wy < 0 || wx >= world.width() || wy >= world.height()){
                        continue;
                    }

                    drawer.get(world.tile(wx, wy));
                }
            }
        }
    }

    public void drawSquare(int x, int y, Cons<Tile> drawer){
        int clamped = (int)brushSize;
        for(int rx = -clamped; rx <= clamped; rx++){
            for(int ry = -clamped; ry <= clamped; ry++){
                int wx = x + rx, wy = y + ry;

                if(wx < 0 || wy < 0 || wx >= world.width() || wy >= world.height()){
                    continue;
                }

                drawer.get(world.tile(wx, wy));
            }
        }
    }

    boolean hasOverlap(int x, int y){
        Tile tile = world.tile(x, y);
        //allow direct replacement of blocks of the same size
        if(tile != null && tile.isCenter() && tile.block() != selected && tile.block().size == selected.size && tile.x == x && tile.y == y){
            return false;
        }

        //else, check for overlap
        int offsetx = -(selected.size - 1) / 2;
        int offsety = -(selected.size - 1) / 2;
        for(int dx = 0; dx < selected.size; dx++){
            for(int dy = 0; dy < selected.size; dy++){
                int worldx = dx + offsetx + x;
                int worldy = dy + offsety + y;
                Tile other = world.tile(worldx, worldy);

                if(other != null && other.block().isMultiblock()){
                    return true;
                }
            }
        }

        return false;
    }
}
