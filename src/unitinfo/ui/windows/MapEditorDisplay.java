package unitinfo.ui.windows;

import arc.Events;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Geometry;
import mindustry.editor.MapEditor;
import mindustry.game.EventType;
import mindustry.graphics.Layer;
import unitinfo.ui.*;
import arc.Core;
import arc.func.*;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.Elem;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.world.*;

import static mindustry.Vars.*;

public class MapEditorDisplay extends Window implements Updatable {
    Vec2 scrollPos = new Vec2(0, 0);
    Table window;
    TextField search;
    EditorTool tool;
    final Vec2[][] brushPolygons = new Vec2[MapEditor.brushSizes.length][0];
    float heat;
    float brushSize = -1;

    public static Team drawTeam = Team.sharded;
    public static Block selected = Blocks.router;

    public MapEditorDisplay()  {
        super(Icon.map, "editor");

        for(int i = 0; i < MapEditor.brushSizes.length; i++){
            float size = MapEditor.brushSizes[i];
            float mod = size % 1f;
            brushPolygons[i] = Geometry.pixelCircle(size, (index, x, y) -> Mathf.dst(x, y, index - mod, index - mod) <= size - 0.5f);
        }
        Events.run(EventType.Trigger.draw, ()->{
            float cx = Core.camera.position.x, cy = Core.camera.position.y;
            float scaling = 8;

            Draw.z(Layer.max);

            if(Core.settings.getBool("grid")){
                Lines.stroke(1f);
                Draw.color(Pal.accent);
                for(int i = (int)(-0.5f*Core.camera.height/8); i < (int)(0.5f*Core.camera.height/8); i++) {
                    Lines.line(Mathf.floor((cx-0.5f*Core.camera.width)/8)*8+4, Mathf.floor((cy + i*8)/8)*8+4, Mathf.floor((cx+0.5f*Core.camera.width)/8)*8+4,Mathf.floor((cy + i*8)/8)*8+4);
                }
                for(int i = (int)(-0.5f*Core.camera.width/8); i < (int)(0.5f*Core.camera.width/8); i++) {
                    Lines.line(Mathf.floor((cx + i*8)/8)*8+4, Mathf.floor((cy+0.5f*Core.camera.height)/8)*8+4, Mathf.floor((cx + i*8)/8)*8+4,Mathf.floor((cy-0.5f*Core.camera.height)/8)*8+4);
                }
                Draw.reset();
            }


            if(tool==null) return;
            int index = 0;
            for(int i = 0; i < MapEditor.brushSizes.length; i++){
                if(brushSize == MapEditor.brushSizes[i]){
                    index = i;
                    break;
                }
            }
            Lines.stroke(Scl.scl(2f), Pal.accent);

            if((!selected.isMultiblock() || tool == EditorTool.eraser) && tool != EditorTool.fill){
                if(tool == EditorTool.line && hold){
                    Vec2 v = Core.input.mouseWorld();
                    Lines.poly(brushPolygons[index], pastX, pastY, scaling);
                    float vx = Mathf.floor(v.x/8)*8-4, vy = Mathf.floor(v.y/8)*8-4;
                    Lines.poly(brushPolygons[index], vx, vy, scaling);
                }

                if((tool.edit || (tool == EditorTool.line && !hold)) && (!mobile || hold)){
                    //pencil square outline
                    Vec2 v = Core.input.mouseWorld();
                    float vx = Mathf.floor(v.x/8)*8-4, vy = Mathf.floor(v.y/8)*8-4;
                    if(tool == EditorTool.pencil && tool.mode == 1){
                        Lines.square(vx, vy, scaling * (brushSize + 0.5f));
                    }else{
                        Lines.poly(brushPolygons[index], vx, vy, scaling);
                    }
                }
            }else{
                if((tool.edit || tool == EditorTool.line) && (!mobile || hold)){
                    Vec2 v = Core.input.mouseWorld();
                    float vx = Mathf.floor(v.x/8)*8-4, vy = Mathf.floor(v.y/8)*8-4;
                    float offset = (selected.size % 2 == 0 ? scaling / 2f : 0f);
                    Lines.square(
                            vx + scaling / 2f + offset,
                            vy + scaling / 2f + offset,
                            scaling * selected.size / 2f);
                }
            }
        });
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
            t.label(()->selected==null?"[gray]None[]":"[accent]"+selected.localizedName+"[] "+selected.emoji());
            t.add(search).growX().pad(8).name("search");
        }).growX().row();
        table.add(new OverScrollPane(rebuild(), Styles.nonePane, scrollPos).disableScroll(true, false)).grow().name("editor-pane").row();
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
        if(tool != null && selected != null && !hasMouse()) {
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

    Table rebuild() {
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

                        ImageButton grid = new ImageButton(Icon.grid, Styles.clearTogglei);
                        grid.clicked(() -> {
                            grid.toggle();
                            Core.settings.put("grid", !Core.settings.getBool("grid"));
                        });
                        grid.update(()->grid.setChecked(Core.settings.getBool("grid")));
                        bt.add(grid);
                    });
                    tools.row();
                    Slider slider = new Slider(0, MapEditor.brushSizes.length - 1, 1, false);
                    slider.moved(f -> brushSize = MapEditor.brushSizes[(int)f]);
                    for(int j = 0; j < MapEditor.brushSizes.length; j++){
                        if(MapEditor.brushSizes[j] == brushSize){
                            slider.setValue(j);
                        }
                    }
                    Label label = new Label("Brush: "+brushSize);
                    label.touchable = Touchable.disabled;
                    tools.stack(slider, label).width(window.getWidth()/5).center();
                }).left().width(window.getWidth() / 2).margin(8f).growY();
                body.image().width(4f).height(body.getHeight()).color(Pal.gray).growY();
                body.table(options -> {
                    options.top().left();
                    options.table(title -> title.left().background(Tex.underline2).add("Options [accent]"+(tool!=null&&tool.mode>=0&&tool.mode<tool.altModes.length?tool.altModes[tool.mode]:"")+"[]")).growX().row();
                    options.table(option-> {
                        if(tool==null) return;

                        option.top().left();
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
                            }).update(b -> b.setChecked(tool.mode == mode)).margin(12f).growX().row();
                        }
                    }).grow();
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
