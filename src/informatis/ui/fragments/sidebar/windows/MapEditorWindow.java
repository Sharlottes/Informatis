package informatis.ui.fragments.sidebar.windows;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.struct.*;
import mindustry.editor.*;
import mindustry.game.*;
import mindustry.graphics.*;
import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.input.*;
import arc.math.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;

import static informatis.ui.fragments.sidebar.windows.WindowManager.mapEditorWindow;
import static mindustry.Vars.*;

public class MapEditorWindow extends Window {
    TextField search;
    EditorTool tool;
    final Vec2[][] brushPolygons = new Vec2[MapEditor.brushSizes.length][0];
    float heat;
    float brushSize = -1;

    boolean drawing;
    int lastx, lasty;
    float lastw, lasth;

    public static Team drawTeam = Team.sharded;
    public static Block drawBlock = Blocks.router;

    public MapEditorWindow()  {
        super(Icon.map, "editor");
        height = 300;
        width = 300;

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

            Tile tile = world.tileWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());
            if(tile == null || tool == null || brushSize < 1) return;

            int index = 0;
            for(int i = 0; i < MapEditor.brushSizes.length; i++){
                if(brushSize == MapEditor.brushSizes[i]){
                    index = i;
                    break;
                }
            }
            Lines.stroke(Scl.scl(2f), Pal.accent);

            if((!drawBlock.isMultiblock() || tool == EditorTool.eraser) && tool != EditorTool.fill){
                if(tool == EditorTool.line && drawing){
                    Lines.poly(brushPolygons[index], lastx, lasty, scaling);
                    Lines.poly(brushPolygons[index], tile.x*8, tile.y*8, scaling);
                }

                if((tool.edit || (tool == EditorTool.line && !drawing)) && (!mobile || drawing)){
                    if(tool == EditorTool.pencil && tool.mode == 1){
                        Lines.square(tile.x*8, tile.y*8, scaling * (brushSize + 0.5f));
                    }else{
                        Lines.poly(brushPolygons[index], tile.x*8-4, tile.y*8-4, scaling);
                    }
                }
            }else{
                if((tool.edit || tool == EditorTool.line) && (!mobile || drawing)){
                    float offset = (drawBlock.size % 2 == 0 ? scaling / 2f : 0f);
                    Lines.square(
                            tile.x*8 + scaling / 2f + offset,
                            tile.y*8 + scaling / 2f + offset,
                            scaling * drawBlock.size / 2f);
                }
            }
        });

        Events.run(EventType.Trigger.update, () -> {

            //TODO make it more responsive, time -> width delta detect
            heat += Time.delta;
            if(heat >= 60f) {
                heat = 0f;

                if(lastw != getWidth() || lasth != getHeight()) resetPane();
                lastw = width;
                lasth = height;
            }

            Tile tile = world.tileWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());
            if(tile == null || tool == null || brushSize < 1 || drawBlock == null || hasMouse()) return;
            if(Core.input.isTouched()) {
                if((tool == EditorTool.line && drawing) || (!mobile && !Core.input.keyDown(KeyCode.mouseLeft))) return;
                drawing = true;
                lastx = tile.x;
                lasty = tile.y;
                tool.touched(lastx, lasty);
            }
            else {
                if(tool == EditorTool.line && drawing) tool.touchedLine(lastx, lasty, tile.x, tile.y);
                drawing = false;
                lastx = -1;
                lasty = -1;
            }
        });
    }

    @Override
    public void buildBody(Table table) {
        search = Elem.newField(null, f->{});
        search.setMessageText(Core.bundle.get("players.search")+"...");

        table.left();
        table.top().background(Styles.black8);

        ObjectMap<Drawable, Element> displays = new ObjectMap<>();
        displays.put(Icon.map, new Table(display -> {
            display.table(t->{
                t.left().background(Tex.underline2);
                t.label(()-> drawBlock == null ? "[gray]None[]" : "[accent]" + drawBlock.localizedName + "[] "+ drawBlock.emoji());
                t.add(search).growX().pad(8).name("search");
            }).growX().row();
            display.pane(Styles.noBarPane, rebuildEditor()).grow().name("editor-pane").get().setScrollingDisabled(true, false);
            display.row();
        }));
        displays.put(Icon.settings, new Table(display -> {
            display.pane(Styles.noBarPane, rebuildRule()).grow().name("rule-pane").get().setScrollingDisabled(true, false);
            display.row();
        }));

        table.table(buttons -> {
            buttons.top().left();

            displays.each((icon, display) -> {
                buttons.button(icon, Styles.clearTogglei, ()->{
                    if(table.getChildren().size > 1) table.getChildren().get(table.getChildren().size-1).remove();
                    table.add(display).grow();
                }).row();
            });
        }).growY();
    }

    void resetPane() {
        ScrollPane pane = find("editor-pane");
        if(pane != null) pane.setWidget(rebuildEditor());
    }

    Table rebuildRule() {
        return new Table(table -> {
            table.top().left();

            table.table(rules -> {
                rules.top().left();

                Label label = rules.add("Block Health: ").get();
                Slider slider = new Slider(0, 100, 1, false);
                slider.changed(() -> {
                    label.setText("Block Health: "+(int)slider.getValue()+"%");
                });
                slider.change();
                slider.moved(hp->Groups.build.each(b->b.health(b.block.health*hp/100)));
                rules.add(slider);
            }).grow();
        });
    }

    Table rebuildEditor() {
        return new Table(table-> {
            table.top();
            Seq<Block> blocks = Vars.content.blocks().copy();
            if(!search.getText().isEmpty()){
                blocks.filter(p -> p.name.toLowerCase().contains(search.getText().toLowerCase())||p.localizedName.toLowerCase().contains(search.getText().toLowerCase()));
            }
            table.table(select-> this.buildBlockSelection(null, select, blocks, ()-> drawBlock, block-> drawBlock =block, false)).marginTop(16f).marginBottom(16f).row();
            table.image().height(4f).color(Pal.gray).growX().row();
            table.table(select-> this.buildTeamSelection(player.team(), select, Seq.with(Team.all), ()->drawTeam, block->drawTeam=block, false)).marginTop(16f).marginBottom(16f).row();
            table.image().height(4f).color(Pal.gray).growX().row();
            table.table(body-> {
                body.table(tools -> {
                    tools.top().left();
                    tools.table(title -> title.left().background(Tex.underline2).add("Tools [accent]"+(tool==null?"":tool.name())+"[]")).growX().row();
                    tools.table(bt->{
                        Cons<EditorTool> addTool = tool -> {
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

                        addTool.get(EditorTool.line);
                        addTool.get(EditorTool.pencil);
                        addTool.get(EditorTool.eraser);
                        addTool.get(EditorTool.fill);
                        addTool.get(EditorTool.spray);

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
                    tools.stack(slider, label).width(getDisplayWidth()/5).center();
                }).left().width(getDisplayWidth() / 2).margin(8f).growY();
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
                                b.setStyle(Styles.clearTogglei);
                                b.add(Core.bundle.get("toolmode." + name)).left().row();
                                b.add(Core.bundle.get("toolmode." + name + ".description")).color(Color.lightGray).left();
                            }, () -> tool.mode = (tool.mode == mode ? -1 : mode)).update(b -> b.setChecked(tool.mode == mode)).margin(12f).growX().row();
                        }
                    }).grow();
                }).left().width(getDisplayWidth() / 2).margin(8f).growY();
            }).grow();
        });
    }

    <T extends Block> void buildBlockSelection(@Nullable Block block, Table table, Seq<T> items, Prov<T> holder, Cons<T> consumer, boolean closeSelect){
        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        Table cont = new Table();
        cont.defaults().size(40);

        int i = 0;
        int row = 4;
        int max = Math.max(row, Math.round(getDisplayWidth()/2/8/row));

        for(T item : items){
            if(!item.unlockedNow()) continue;

            ImageButton button = cont.button(Tex.whiteui, Styles.clearTogglei, 24, () -> {
                if(closeSelect) control.input.config.hideConfig();
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
        pane.setScrollYForce(blockScroll);
        pane.update(() -> blockScroll = pane.getScrollY());
        pane.setOverscroll(false, false);
        table.add(pane).maxHeight(Scl.scl(row * 10 * 5));
    }
    float blockScroll;

    <T extends Team> void buildTeamSelection(@Nullable Team team, Table table, Seq<T> items, Prov<T> holder, Cons<T> consumer, boolean closeSelect){
        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        Table cont = new Table();
        cont.defaults().size(40);

        int i = 0;
        int row = 2;
        int max = Math.max(row, Math.round(getDisplayWidth()/2/8/row/2));

        for(T item : items){
            ImageButton button = cont.button(Tex.whiteui, Styles.clearTogglei, 24, () -> {
                if(closeSelect) control.input.config.hideConfig();
            }).group(group).tooltip(t->t.background(Styles.black8).add(item.localized().replace(search.getText(), "[accent]"+search.getText()+"[]"))).with(img -> img.getStyle().imageUpColor = item.color).get();
            button.changed(() -> consumer.get(button.isChecked() ? item : null));
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
        pane.setScrollYForce(teamScroll);
        pane.update(() -> teamScroll = pane.getScrollY());
        pane.setOverscroll(false, false);
        table.add(pane).maxHeight(Scl.scl(row * 10 * 5));
    }
    float teamScroll;

    float getDisplayWidth() {
        return getWidth() - (find("buttons") == null ? 1 : find("buttons").getWidth());
    }
    
    public void drawBlocksReplace(int x, int y){
        drawBlocks(x, y, tile -> tile.block() != Blocks.air || drawBlock.isFloor());
    }

    public void drawBlocks(int x, int y){
        drawBlocks(x, y, false, tile -> true);
    }

    public void drawBlocks(int x, int y, Boolf<Tile> tester){
        drawBlocks(x, y, false, tester);
    }

    final int rotation = 0;
    public void drawBlocks(int x, int y, boolean square, Boolf<Tile> tester){
        if(drawBlock.isMultiblock()){
            x = Mathf.clamp(x, (drawBlock.size - 1) / 2, world.width() - drawBlock.size / 2 - 1);
            y = Mathf.clamp(y, (drawBlock.size - 1) / 2, world.height() - drawBlock.size / 2 - 1);
            Tile tile = world.tile(x, y);
            if(tile != null && !hasOverlap(tile)){
                tile.setBlock(drawBlock, drawTeam, rotation);
            }
        }else{
            boolean isFloor = drawBlock.isFloor() && drawBlock != Blocks.air;

            Cons<Tile> drawer = tile -> {
                if(!tester.get(tile)) return;

                if(isFloor){
                    tile.setFloor(drawBlock.asFloor());
                }else if(!(tile.block().isMultiblock() && !drawBlock.isMultiblock())){
                    tile.setBlock(drawBlock, drawTeam, rotation);
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

    boolean hasOverlap(Tile tile){
        //allow direct replacement of blocks of the same size
        if(tile.isCenter() && tile.block() != drawBlock && tile.block().size == drawBlock.size && tile.x == x && tile.y == y){
            return false;
        }

        //else, check for overlap
        int offsetX = -(drawBlock.size - 1) / 2;
        int offsetY = -(drawBlock.size - 1) / 2;
        for(int dx = 0; dx < drawBlock.size; dx++){
            for(int dy = 0; dy < drawBlock.size; dy++){
                int worldX = dx + offsetX + tile.x;
                int worldY = dy + offsetY + tile.y;
                Tile other = world.tile(worldX, worldY);

                if(other != null && other.block().isMultiblock()){
                    return true;
                }
            }
        }
        return false;
    }

    enum EditorTool{
        zoom(KeyCode.v),
        pick(KeyCode.i){
            public void touched(int x, int y){
                if(!Structs.inBounds(x, y, world.width(), world.height())) return;
                Tile tile = world.tile(x, y);
                if(tile == null) return;

                drawBlock = tile.block() == Blocks.air || !tile.block().inEditor ? tile.overlay() == Blocks.air ? tile.floor() : tile.overlay() : tile.block();
            }
        },
        line(KeyCode.l, "replace", "orthogonal"){

            @Override
            public void touchedLine(int x1, int y1, int x2, int y2){
                //straight
                if(mode == 1){
                    if(Math.abs(x2 - x1) > Math.abs(y2 - y1)){
                        y2 = y1;
                    }else{
                        x2 = x1;
                    }
                }

                Bresenham2.line(x1, y1, x2, y2, (x, y) -> {
                    if(mode == 0){
                        //replace
                        mapEditorWindow.drawBlocksReplace(x, y);
                    }else{
                        //normal
                        mapEditorWindow.drawBlocks(x, y);
                    }
                });
            }
        },
        pencil(KeyCode.b, "replace", "square", "drawteams"){
            {
                edit = true;
                draggable = true;
            }

            @Override
            public void touched(int x, int y){
                if(mode == -1){
                    //normal mode
                    mapEditorWindow.drawBlocks(x, y);
                }else if(mode == 0){
                    //replace mode
                    mapEditorWindow.drawBlocksReplace(x, y);
                }else if(mode == 1){
                    //square mode
                    mapEditorWindow.drawBlocks(x, y, true, tile -> true);
                }else if(mode == 2){
                    //draw teams
                    mapEditorWindow.drawCircle(x, y, tile -> tile.setTeam(drawTeam));
                }

            }
        },
        eraser(KeyCode.e, "eraseores"){
            {
                edit = true;
                draggable = true;
            }

            @Override
            public void touched(int x, int y){
                mapEditorWindow.drawCircle(x, y, tile -> {
                    if(mode == -1){
                        //erase block
                        tile.remove();
                    }else if(mode == 0){
                        //erase ore
                        tile.clearOverlay();
                    }
                });
            }
        },
        fill(KeyCode.g, "replaceall", "fillteams"){
            {
                edit = true;
            }

            IntSeq stack = new IntSeq();

            @Override
            public void touched(int x, int y){
                if(!Structs.inBounds(x, y, world.width(), world.height())) return;
                Tile tile = world.tile(x, y);
                if(tile == null) return;

                //mode 0 or 1, fill everything with the floor/tile or replace it
                if(mode == 0 || mode == -1){
                    if(tile.block().isMultiblock()) return;

                    Boolf<Tile> tester;
                    Cons<Tile> setter;
                    Block drawBlock = MapEditorWindow.drawBlock;

                    if(drawBlock.isOverlay()){
                        Block dest = tile.overlay();
                        if(dest == drawBlock) return;
                        tester = t -> t.overlay() == dest && (t.floor().hasSurface() || !t.floor().needsSurface);
                        setter = t -> t.setOverlay(drawBlock);
                    }else if(drawBlock.isFloor()){
                        Block dest = tile.floor();
                        if(dest == drawBlock) return;
                        tester = t -> t.floor() == dest;
                        setter = t -> t.setFloorUnder(drawBlock.asFloor());
                    }else{
                        Block dest = tile.block();
                        if(dest == drawBlock) return;
                        tester = t -> t.block() == dest;
                        setter = t -> t.setBlock(drawBlock, drawTeam);
                    }

                    //replace only when the mode is 0 using the specified functions
                    fill(x, y, mode == 0, tester, setter);
                } else if(mode == 1){ //mode 1 is team fill
                    //only fill synthetic blocks, it's meaningless otherwise
                    if(tile.synthetic()){
                        Team dest = tile.team();
                        if(dest == drawTeam) return;
                        fill(x, y, false, t -> t.getTeamID() == dest.id && t.synthetic(), t -> t.setTeam(drawTeam));
                    }
                }
            }

            void fill(int x, int y, boolean replace, Boolf<Tile> tester, Cons<Tile> filler){
                int width = world.width(), height = world.height();

                if(replace){
                    //just do it on everything
                    for(int cx = 0; cx < width; cx++){
                        for(int cy = 0; cy < height; cy++){
                            Tile tile = world.tile(cx, cy);
                            if(tester.get(tile)){
                                filler.get(tile);
                            }
                        }
                    }

                }else{
                    //perform flood fill
                    int x1;

                    stack.clear();
                    stack.add(Point2.pack(x, y));

                    try{
                        while(stack.size > 0 && stack.size < width*height){
                            int popped = stack.pop();
                            x = Point2.x(popped);
                            y = Point2.y(popped);

                            x1 = x;
                            while(x1 >= 0 && tester.get(world.tile(x1, y))) x1--;
                            x1++;
                            boolean spanAbove = false, spanBelow = false;
                            while(x1 < width && tester.get(world.tile(x1, y))){
                                filler.get(world.tile(x1, y));

                                if(!spanAbove && y > 0 && tester.get(world.tile(x1, y - 1))){
                                    stack.add(Point2.pack(x1, y - 1));
                                    spanAbove = true;
                                }else if(spanAbove && !tester.get(world.tile(x1, y - 1))){
                                    spanAbove = false;
                                }

                                if(!spanBelow && y < height - 1 && tester.get(world.tile(x1, y + 1))){
                                    stack.add(Point2.pack(x1, y + 1));
                                    spanBelow = true;
                                }else if(spanBelow && y < height - 1 && !tester.get(world.tile(x1, y + 1))){
                                    spanBelow = false;
                                }
                                x1++;
                            }
                        }
                        stack.clear();
                    }catch(OutOfMemoryError e){
                        //hack
                        stack = null;
                        System.gc();
                        stack = new IntSeq();
                        Log.err(e);
                    }
                }
            }
        },
        spray(KeyCode.r, "replace"){
            final double chance = 0.012;

            {
                edit = true;
                draggable = true;
            }

            @Override
            public void touched(int x, int y){
                //floor spray
                if(drawBlock.isFloor()){
                    mapEditorWindow.drawCircle(x, y, tile -> {
                        if(Mathf.chance(chance)){
                            tile.setFloor(drawBlock.asFloor());
                        }
                    });
                }else if(mode == 0){ //replace-only mode, doesn't affect air
                    mapEditorWindow.drawBlocks(x, y, tile -> Mathf.chance(chance) && tile.block() != Blocks.air);
                }else{
                    mapEditorWindow.drawBlocks(x, y, tile -> Mathf.chance(chance));
                }
            }
        };

        public static final EditorTool[] all = values();

        /** All the internal alternate placement modes of this tool. */
        public final String[] altModes;
        /** Key to activate this tool. */
        public KeyCode key = KeyCode.unset;
        /** The current alternate placement mode. -1 is the standard mode, no changes.*/
        public int mode = -1;
        /** Whether this tool causes canvas changes when touched.*/
        public boolean edit;
        /** Whether this tool should be dragged across the canvas when the mouse moves.*/
        public boolean draggable;

        EditorTool(){
            this(new String[]{});
        }

        EditorTool(KeyCode code){
            this(new String[]{});
            this.key = code;
        }

        EditorTool(String... altModes){
            this.altModes = altModes;
        }

        EditorTool(KeyCode code, String... altModes){
            this.altModes = altModes;
            this.key = code;
        }

        public void touched(int x, int y){}

        public void touchedLine(int x1, int y1, int x2, int y2){}
    }
}
