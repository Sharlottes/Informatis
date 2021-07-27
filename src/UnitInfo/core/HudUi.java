package UnitInfo.core;

import UnitInfo.ui.*;
import arc.*;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.*;
import arc.math.geom.Geometry;
import arc.scene.*;
import arc.scene.event.HandCursorListener;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.Units;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.logic.Ranged;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.ControlBlock;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.storage.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class HudUi {
    Seq<Element> bars = new Seq<>();
    Table weapon = new Table();
    Table mainTable = new Table();
    Table baseTable = new Table();
    Table unitTable = new Table();
    Table waveTable = new Table();
    Table coreTable = new Table();
    Table tileTable = new Table();
    Table itemTable = new Table();
    float waveScrollPos;
    float coreScrollPos;
    float tileScrollPos;
    float itemScrollPos;

    Teamc lockedTarget;
    ImageButton lockButton;
    boolean locked = false;

    float charge;
    float a;
    int uiIndex = 0;

    //to update tables
    int waveamount;
    int coreamount;

    //is this rly good idea?
    Seq<String> strings = Seq.with("","","","","","");
    FloatSeq numbers = FloatSeq.with(0f,0f,0f,0f,0f,0f);
    Seq<Color> colors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);
    Seq<Color> lastColors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);
    CoresItemsDisplay coreItems = new CoresItemsDisplay(Team.baseTeams);


    @Nullable Teamc target;

    @SuppressWarnings("unchecked")
    public <T extends Teamc> T getTarget(){
        if(locked &&
            (lockedTarget instanceof Unit && ((Unit) lockedTarget).dead) ||
            (lockedTarget instanceof Building && ((Building) lockedTarget).dead)) {
            lockedTarget = null;
            locked = false;
        }
        if(locked && lockedTarget != null)
            return (T) lockedTarget; //if there is locked target, return it first.

        Seq<Unit> units = Groups.unit.intersect(Core.input.mouseWorldX(), Core.input.mouseWorldY(), 4, 4); // well, 0.5tile is enough to search them........ maybe?
        if(units.size > 0)
            return (T) units.peek(); //if there is unit, return it.
        else if(getTile() != null && getTile().build != null)
            return (T) getTile().build; //if there isn't unit but there is build, return it.
        else if(player.unit() instanceof BlockUnitUnit && ((BlockUnitUnit)player.unit()).tile() != null)
            return (T)((BlockUnitUnit)player.unit()).tile();
        return (T) player.unit(); //if there are not unit and not build, return player.
    }

    public @Nullable Tile getTile(){
        return Vars.world.tileWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());
    }

    public void setEvent(){
        Events.run(EventType.Trigger.draw, () -> {
            if(getTarget() == null || !Core.settings.getBool("select")) return;

            Posc entity = getTarget();
            for(int i = 0; i < 4; i++){
                float rot = i * 90f + 45f + (-Time.time) % 360f;
                float length = (entity instanceof Unit ? ((Unit)entity).hitSize : entity instanceof Building ? ((Building)entity).block.size * tilesize : 0) * 1.5f + 2.5f;
                Draw.color(Tmp.c1.set(locked ? Color.orange : Color.darkGray).lerp(locked ? Color.scarlet : Color.gray, Mathf.absin(Time.time, 2f, 1f)).a(settings.getInt("selectopacity") / 100f));
                Draw.rect("select-arrow", entity.x() + Angles.trnsx(rot, length), entity.y() + Angles.trnsy(rot, length), length / 1.9f, length / 1.9f, rot - 135f);
                Draw.reset();
            }
        });
        Events.on(EventType.ResetEvent.class, e -> {
            if(settings.getBool("allTeam")) coreItems.teams = Team.all;
            else coreItems.teams = Team.baseTeams;
            coreItems.resetUsed();
            coreItems.tables.each(Group::clear);
        });
        Events.run(EventType.Trigger.update, ()->{
            if((Core.input.keyDown(KeyCode.shiftRight) || Core.input.keyDown(KeyCode.shiftLeft)) && Core.input.keyTap(KeyCode.r)){
                lockButton.change();
            }
            if(!settings.getBool("autoShooting")) return;
            Unit unit = player.unit();
            if(unit.type == null) return;
            boolean omni = unit.type.omniMovement;
            boolean validHealTarget = unit.type.canHeal && target instanceof Building && ((Building)target).isValid() && target.team() == unit.team && ((Building)target).damaged() && target.within(unit, unit.type.range);
            boolean boosted = (unit instanceof Mechc && unit.isFlying());
            if((unit.type != null && Units.invalidateTarget(target, unit, unit.type.range) && !validHealTarget) || state.isEditor()){
                target = null;
            }


            float mouseAngle = unit.angleTo(unit.aimX(), unit.aimY());
            boolean aimCursor = omni && player.shooting && unit.type.hasWeapons() && unit.type.faceTarget && !boosted && unit.type.rotateShooting;
            if(aimCursor){
                unit.lookAt(mouseAngle);
            }else{
                unit.lookAt(unit.prefRotation());
            }

            //update shooting if not building + not mining
            if(!player.unit().activelyBuilding() && player.unit().mineTile == null){
                //autofire targeting
                if(input.keyDown(KeyCode.mouseLeft)) {
                    player.shooting = !boosted;
                    unit.aim(player.mouseX = Core.input.mouseWorldX(), player.mouseY = Core.input.mouseWorldY());
                } else if(target == null){
                    player.shooting = false;
                    if(unit instanceof BlockUnitUnit){
                        if(((BlockUnitUnit)unit).tile() instanceof ControlBlock && !((ControlBlock)((BlockUnitUnit)unit).tile()).shouldAutoTarget()){
                            Building build = ((BlockUnitUnit)unit).tile();
                            float range = build instanceof Ranged ? ((Ranged)build).range() : 0f;
                            boolean targetGround = build instanceof Turret.TurretBuild && ((Turret) build.block).targetAir;
                            boolean targetAir = build instanceof Turret.TurretBuild && ((Turret) build.block).targetGround;
                            target = Units.closestTarget(build.team, build.x, build.y, range, u -> u.checkTarget(targetAir, targetGround), u -> targetGround);
                        }
                        else target = null;
                    } else if(unit.type != null){
                        float range = unit.hasWeapons() ? unit.range() : 0f;
                        target = Units.closestTarget(unit.team, unit.x, unit.y, range, u -> u.checkTarget(unit.type.targetAir, unit.type.targetGround), u -> unit.type.targetGround);

                        if(unit.type.canHeal && target == null){
                            target = Geometry.findClosest(unit.x, unit.y, indexer.getDamaged(Team.sharded));
                            if(target != null && !unit.within(target, range)){
                                target = null;
                            }
                        }
                    }
                }else {
                    player.shooting = !boosted;
                    unit.rotation(Angles.angle(unit.x, unit.y, target.x(), target.y()));
                    unit.aim(target.x(), target.y());
                }
            }
            unit.controlWeapons(player.shooting && !boosted);
        });
    }

    public void reset(int index, Seq<Button> buttons, Label label, Table table, Table labelTable, String hud){
        uiIndex = index;
        buttons.each(b -> b.setChecked(buttons.indexOf(b) == index));
        label.setText(Core.bundle.get(hud));
        addBars();
        addWeapon();
        addUnitTable();
        addWaveTable();
        addCoreTable();
        addTileTable();
        addItemTable();
        table.removeChild(baseTable);
        labelTable.setPosition(buttons.items[uiIndex].x, buttons.items[uiIndex].y);
        baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, tileTable, itemTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
        a = 1f;
    }
    public void addTable(){
        mainTable = new Table(table -> {
            table.left();

            Label label = new Label("");
            label.setColor(Pal.stat);
            label.update(() -> {
                a = Mathf.lerpDelta(a, 0f, 0.025f);
                label.color.a = a;
            });
            Label.LabelStyle style = new Label.LabelStyle(){{
                font = Fonts.outline;
                fontColor = Color.white;
                background = Styles.black8;
            }};
            label.setStyle(style);

            Table labelTable = new Table(t -> t.add(label).scaling(Scaling.fit).left().padRight(40 * 8f));

            table.table(t -> {
                Seq<Button> buttons = Seq.with(null, null, null, null, null, null);
                Seq<String> strs = Seq.with("hud.unit", "hud.wave", "hud.core", "hud.tile", "hud.item", "hud.cancel");
                Seq<TextureRegionDrawable> icons = Seq.with(Icon.units, Icon.fileText, Icon.commandRally, Icon.grid, Icon.copy, Icon.cancel);
                for(int i = 0; i < buttons.size; i++){
                    int finalI = i;
                    buttons.set(i, t.button(icons.get(i), Styles.clearToggleTransi, () -> {
                        reset(finalI, buttons, label, table, labelTable, strs.get(finalI));
                    }).size(5*8f).get());
                    t.row();
                }
            });
            baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, tileTable, itemTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
            table.fillParent = true;

            table.visibility = () -> (ui.hudfrag.shown && !ui.minimapfrag.shown()
                    && (!Vars.mobile ||
                        !(Vars.control.input.block != null || !Vars.control.input.selectRequests.isEmpty()
                            && !(Vars.control.input.lastSchematic != null && !Vars.control.input.selectRequests.isEmpty()))));
        });
        ui.hudGroup.addChild(mainTable);
    }

    public void addBars(){
        bars.clear();
        lastColors.set(2, colors.get(2));
        {
            int i = 0;
            bars.add(new SBar(
                    () -> strings.get(i),
                    () -> {
                        if (colors.get(i) != Color.clear) lastColors.set(i, colors.get(i));
                        return lastColors.get(i);
                    },
                    () -> numbers.get(i)
            ));
        }
        {
            int i = 1;
            bars.add(new SBar(
                    () -> strings.get(i),
                    () -> {
                        if (colors.get(i) != Color.clear) lastColors.set(i, colors.get(i));
                        return lastColors.get(i);
                    },
                    () -> numbers.get(i)
            ));
        }
        bars.add(new Stack(){{
            add(new Table(t -> {
                t.top().defaults().width(Scl.scl(23 * 8f)).height(Scl.scl(4f * 8f));
                int i = 2;
                t.add(new SBar(
                    () -> BarInfo.strings.get(i),
                    () -> {
                        if(BarInfo.colors.get(i) != Color.clear) lastColors.set(i, BarInfo.colors.get(i));
                        return lastColors.get(i);
                    },
                    () -> BarInfo.numbers.get(i)
                )).growX().left();
            }));
            add(new Table(){{
                left();
                update(() -> {
                    if(!(getTarget() instanceof ItemTurret.ItemTurretBuild) && !(getTarget() instanceof LiquidTurret.LiquidTurretBuild) && !(getTarget() instanceof PowerTurret.PowerTurretBuild)){
                        clearChildren();
                        return;
                    }
                    if(getTarget() instanceof Turret.TurretBuild){
                        Element image = new Element();
                        if(getTarget() instanceof ItemTurret.ItemTurretBuild){
                            ItemTurret.ItemTurretBuild turretBuild = getTarget();
                            if(turretBuild.hasAmmo()) image = new Image(((ItemTurret)turretBuild.block).ammoTypes.findKey(turretBuild.peekAmmo(), true).uiIcon);
                            else {MultiReqImage itemReq = new MultiReqImage();
                                for(Item item : ((ItemTurret) turretBuild.block).ammoTypes.keys())
                                    itemReq.add(new ReqImage(item.uiIcon, turretBuild::hasAmmo));
                                image = itemReq;
                            }
                        }
                        else if(getTarget() instanceof LiquidTurret.LiquidTurretBuild){
                            LiquidTurret.LiquidTurretBuild entity = getTarget();
                            MultiReqImage liquidReq = new MultiReqImage();
                            for(Liquid liquid : ((LiquidTurret) ((LiquidTurret.LiquidTurretBuild) getTarget()).block).ammoTypes.keys())
                                liquidReq.add(new ReqImage(liquid.uiIcon, () -> ((LiquidTurret.LiquidTurretBuild) getTarget()).hasAmmo()));
                            image = liquidReq;

                            if(((LiquidTurret.LiquidTurretBuild) getTarget()).hasAmmo())
                                image = new Image(entity.liquids.current().uiIcon).setScaling(Scaling.fit);
                        }
                        else if(getTarget() instanceof PowerTurret.PowerTurretBuild){
                            image = new Image(Icon.power.getRegion()){
                                @Override
                                public void draw(){
                                    Building entity = getTarget();
                                    float max = entity.block.consumes.getPower().usage;
                                    float v = entity.power.status * entity.power.graph.getLastScaledPowerIn();

                                    super.draw();
                                    Lines.stroke(Scl.scl(2f), Pal.removeBack);
                                    Draw.alpha(1 - v/max);
                                    Lines.line(x, y - 2f + height, x + width, y - 2f);
                                    Draw.color(Pal.remove);
                                    Draw.alpha(1 - v/max);
                                    Lines.line(x, y + height, x + width, y);
                                    Draw.reset();
                                }
                            };
                        }

                        clearChildren();
                        add(image).size(iconSmall).padBottom(2 * 8f).padRight(3 * 8f);
                    }
                });
                pack();
            }});
            add(new Table(t -> {
                t.left();
                t.add(new Image(){{
                        update(() -> {
                            if(getTarget() instanceof Unit && ((Unit) getTarget()).stack().item != null && ((Unit) getTarget()).stack.amount > 0)
                                setDrawable(((Unit) getTarget()).stack().item.uiIcon);
                            else setDrawable(Core.atlas.find("clear"));
                        });
                        visibility = () -> getTarget() instanceof Unit;
                    }}.setScaling(Scaling.fit)).size(Scl.scl(30f)).padBottom(Scl.scl(4 * 8f)).padRight(Scl.scl(6 * 8f));
                t.pack();
            }));
        }});


        {
            int i = 3;
            bars.add(new SBar(
                    () -> strings.get(i),
                    () -> {
                        if (colors.get(i) != Color.clear) lastColors.set(i, colors.get(i));
                        return lastColors.get(i);
                    },
                    () -> numbers.get(i)
            ));
        }


        {
            int i = 4;
            bars.add(new SBar(
                    () -> strings.get(i),
                    () -> {
                        if (colors.get(i) != Color.clear) lastColors.set(i, colors.get(i));
                        return lastColors.get(i);
                    },
                    () -> numbers.get(i)
            ));
        }

        bars.add(new Stack(){{
            add(new Table(t -> {
                t.top().defaults().width(Scl.scl(23 * 8f)).height(Scl.scl(4f * 8f));

                int i = 5;
                t.add(new SBar(
                        () -> strings.get(i),
                        () -> {
                            if (colors.get(i) != Color.clear) lastColors.set(i, colors.get(i));
                            return lastColors.get(i);
                        },
                        () -> numbers.get(i)
                )).growX().left();
            }));
            add(new Table(t -> {
                t.left();
                t.add(new Image(){{
                    update(() -> {
                        TextureRegion region = Core.atlas.find("clear");

                        if(Vars.state.rules.unitAmmo && getTarget() instanceof Unit && ((Unit) getTarget()).type() != null){
                            UnitType type = ((Unit) getTarget()).type();
                            if(type.ammoType == AmmoTypes.copper) region = Items.copper.uiIcon;
                            else if(type.ammoType == AmmoTypes.thorium) region = Items.thorium.uiIcon;
                            else if(type.ammoType == AmmoTypes.power || type.ammoType == AmmoTypes.powerLow || type.ammoType == AmmoTypes.powerHigh) region = Icon.powerSmall.getRegion();
                        }
                        setDrawable(region);
                    });
                }}.setScaling(Scaling.fit)).size(Scl.scl(30f)).padBottom(Scl.scl(4 * 8f)).padRight(Scl.scl(6 * 8f));
                t.pack();
            }));
        }});
    }

    public void addWeapon(){
        weapon = new Table(tx -> {
            tx.left().defaults().minSize(Scl.scl(12 * 8f));

            tx.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, tt -> {
                tt.left().top().defaults().width(Scl.scl(24/3f * 8f)).minHeight(Scl.scl(12/3f * 8f));

                if(getTarget() instanceof Unit && ((Unit) getTarget()).type != null) {
                    UnitType type = ((Unit) getTarget()).type;
                    for(int r = 0; r < type.weapons.size; r++){
                        Weapon weapon = type.weapons.get(r);
                        WeaponMount mount = ((Unit) getTarget()).mounts[r];
                        TextureRegion region = !weapon.name.equals("") && weapon.outlineRegion.found() ? weapon.outlineRegion : type.uiIcon;
                        if(type.weapons.size > 1 && r % 3 == 0) tt.row();
                        else if(r % 3 == 0) tt.row();
                        tt.table(weapontable -> {
                            weapontable.left();
                            weapontable.add(new Stack(){{
                                add(new Table(o -> {
                                    o.left();
                                    o.add(new Image(region){
                                        @Override
                                        public void draw(){
                                            validate();
                                            float x = this.x;
                                            float y = this.y;
                                            float scaleX = this.scaleX;
                                            float scaleY = this.scaleY;
                                            Draw.color(color);
                                            Draw.alpha(parentAlpha * color.a);

                                            if(getDrawable() instanceof TransformDrawable){
                                                float rotation = getRotation();
                                                if(scaleX != 1 || scaleY != 1 || rotation != 0){
                                                    getDrawable().draw(x + imageX, y + imageY,
                                                            originX - imageX, originY - imageY,
                                                            imageWidth, imageHeight,
                                                            scaleX, scaleY, rotation);
                                                    return;
                                                }
                                            }

                                            float recoil = -((mount.reload) / weapon.reload * weapon.recoil);
                                            y += recoil;
                                            if(getDrawable() != null) getDrawable().draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
                                        }
                                    }.setScaling(Scaling.fit)).size(Scl.scl(6 * 8f)).scaling(Scaling.fit);
                                }));

                                add(new Table(h -> {
                                    h.defaults().growX().height(Scl.scl(9)).width(Scl.scl(31.5f)).padTop(Scl.scl(9*2f));
                                    Bar reloadBar = new Bar(
                                            () -> "",
                                            () -> Pal.accent.cpy().lerp(Color.orange, mount.reload / weapon.reload),
                                            () -> mount.reload / weapon.reload);
                                    h.add(reloadBar).padLeft(Scl.scl(8f));
                                    h.pack();
                                }));
                            }}).left();
                        }).left();
                        tt.center();
                    }
                }
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Color color = this.color;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                }
            }).padRight(Scl.scl(24 * 8f));
            tx.setColor(tx.color.cpy().a(1f));
        });
    }

    public void addUnitTable(){
        if(uiIndex != 0) return;
        unitTable = new Table(table -> {
            table.left();
            addBars();
            table.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.defaults().width(Scl.scl(25 * 8f)).scaling(Scaling.bounded);

                t.table(Tex.underline2, tt -> {
                    Stack stack = new Stack(){{
                        add(new Table(ttt -> ttt.add(new Image(){{
                            update(() -> {
                                TextureRegion region = atlas.find("clear");
                                if(getTarget() instanceof Unit && ((Unit) getTarget()).type() != null) region = ((Unit) getTarget()).type().uiIcon;
                                if(getTarget() instanceof Building && ((Building) getTarget()).block != null) {
                                    if(getTarget() instanceof ConstructBlock.ConstructBuild) region = ((ConstructBlock.ConstructBuild) getTarget()).current.uiIcon;
                                    else region = ((Building) getTarget()).block.uiIcon;
                                }
                                setDrawable(region);
                            });
                        }}.setScaling(Scaling.fit)).size(Scl.scl(4f * 8f))));
                        add(new Table(ttt -> {
                            ttt.add(new Stack(){{
                                add(new Table(temp -> temp.add(new Image(){{
                                    update(()->{
                                        TextureRegion region = atlas.find("clear");
                                        if(getTarget() instanceof Unit) region = Icon.defenseSmall.getRegion();
                                        setDrawable(region);
                                    });
                                }}.setScaling(Scaling.fit))));

                                add(new Table(temp -> {
                                    if(getTarget() instanceof Unit) {
                                        Label label = new Label(() -> (getTarget() instanceof Unit && ((Unit) getTarget()).type() != null ? (int) ((Unit) getTarget()).type().armor + "" : ""));
                                        label.setColor(Pal.surge);
                                        label.setFontScale(0.5f);
                                        temp.add(label).center();
                                    }
                                    temp.pack();
                                }));
                            }}).padLeft(Scl.scl(2 * 8f)).padBottom(Scl.scl(2 * 8f));
                        }));
                    }};

                    Label label = new Label(() -> {
                        String name = "";
                        if(getTarget() instanceof Unit && ((Unit) getTarget()).type() != null)
                            name = "[accent]" + ((Unit) getTarget()).type().localizedName + "[]";
                        if(getTarget() instanceof Building && ((Building) getTarget()).block() != null) {
                            if(getTarget() instanceof ConstructBlock.ConstructBuild) name = "[accent]" + ((ConstructBlock.ConstructBuild) getTarget()).current.localizedName + "[]";
                            else name = "[accent]" + ((Building) getTarget()).block.localizedName + "[]";
                        }
                        return name;
                    });

                    label.setFontScale(Scl.scl());
                    TextButton button = Elem.newButton("?", Styles.clearPartialt, () -> {
                        if(getTarget() instanceof Unit && ((Unit) getTarget()).type() != null)
                            ui.content.show(((Unit) getTarget()).type);
                        if(getTarget() instanceof Building && ((Building) getTarget()).block != null) {
                            ui.content.show(((Building) getTarget()).block);
                        }
                    });
                    button.visibility = () -> getTarget() != null;

                    lockButton = Elem.newImageButton(Styles.clearPartiali, Icon.lock.tint(locked ? Pal.accent : Color.white), 3 * 8f, () -> {
                        locked = !locked;
                        if(locked) lockedTarget = getTarget();
                        else lockedTarget = null;
                    });
                    button.update(()->{
                        lockButton.getStyle().imageUp = Icon.lock.tint(locked ? Pal.accent : Color.white);
                        lockButton.getStyle().imageDown = Icon.lock.tint(locked ? Pal.accent : Color.white);
                    });
                    lockButton.visibility = () -> getTarget() != null;

                    tt.top();
                    tt.add(stack);
                    tt.add(label);
                    tt.add(button).size(Scl.scl(5 * 8f));
                    tt.add(lockButton).size(Scl.scl(3 * 8f));
                });
                t.row();
                t.table(tt -> {
                    tt.defaults().width(Scl.scl(23 * 8f)).height(Scl.scl(4f * 8f)).top();
                    for(Element bar : bars){
                        bar.setWidth(bar.getWidth());
                        bar.setHeight(bar.getHeight());
                        tt.add(bar).growX().left();
                        tt.row();
                    }
                });
                t.setColor(t.color.cpy().a(1f));
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Color color = this.color;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                }
            }).padRight(Scl.scl(24 * 8f));
            table.row();
            table.update(() -> {
                BarInfo.getInfo(getTarget());
                strings = BarInfo.strings;
                numbers = BarInfo.numbers;
                colors = BarInfo.colors;

                if(getTarget() instanceof Turret.TurretBuild){
                    if(((Turret.TurretBuild) getTarget()).charging) charge += Time.delta;
                    else charge = 0f;
                }
                table.removeChild(weapon);
                if(settings.getBool("weaponui") && getTarget() instanceof Unit && ((Unit) getTarget()).type != null) {
                    addWeapon();
                    table.row();
                    table.add(weapon);
                }
            });

            table.fillParent = true;
            table.visibility = () -> uiIndex == 0;
        });
    }

    public void setWave(Table table){
        int winWave = state.isCampaign() && state.rules.winWave > 0 ? state.rules.winWave : Integer.MAX_VALUE;
        waveamount = settings.getInt("wavemax");
        for(int i = settings.getBool("pastwave") ? 0 : state.wave - 1; i <= Math.min(state.wave + waveamount, winWave - 2); i++){
            final int j = i;
            if(!settings.getBool("emptywave") && state.rules.spawns.find(g -> g.getSpawned(j) > 0) == null) continue;
            table.table(t -> {
                table.center();
                final int jj = j+1;
                Label label = new Label(() -> "[#" + (state.wave == j+1 ? Color.red.toString() : Pal.accent.toString()) + "]" + jj + "[]");
                label.setFontScale(Scl.scl());
                t.add(label);
            }).size(Scl.scl(32f));

            table.table(Tex.underline, tx -> {
                if(settings.getBool("emptywave") && state.rules.spawns.find(g -> g.getSpawned(j) > 0) == null) {
                    tx.center();
                    tx.add("[lightgray]<Empty>[]");
                    return;
                }
                int row = 0;
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

                for(SpawnGroup group : groupsTmp.keys()){
                    int amount = groupsTmp.get(group);
                    row ++;

                    tx.table(tt -> {
                        tt.right();
                        Image image = new Image(group.type.uiIcon).setScaling(Scaling.fit);
                        tt.add(new Stack(){{
                            add(new Table(ttt -> {
                                ttt.center();
                                ttt.add(image).size(iconLarge);
                                ttt.pack();
                            }));

                            add(new Table(ttt -> {
                                ttt.bottom().left();
                                Label label = new Label(() -> amount + "");
                                label.setFontScale(Scl.scl());
                                ttt.add(label);
                                ttt.pack();
                            }));

                            add(new Table(ttt -> {
                                ttt.top().right();
                                Image image = new Image(Icon.warning.getRegion()).setScaling(Scaling.fit);
                                image.update(() -> image.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f))));
                                ttt.add(image).size(Scl.scl(12f));
                                ttt.visible(() -> group.effect == StatusEffects.boss);
                                ttt.pack();
                            }));
                        }}).pad(2f);
                        tt.clicked(() -> {
                            if(Core.input.keyDown(KeyCode.shiftLeft) && Fonts.getUnicode(group.type.name) != 0){
                                Core.app.setClipboardText((char)Fonts.getUnicode(group.type.name) + "");
                                ui.showInfoFade("@copied");
                            }else{
                                ui.content.show(group.type);
                            }
                        });
                        if(!mobile){
                            HandCursorListener listener1 = new HandCursorListener();
                            tt.addListener(listener1);
                            tt.update(() -> image.color.lerp(!listener1.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta)));
                        }
                        tt.addListener(new Tooltip(t -> t.background(Tex.button).table(to -> {
                            to.left();
                            to.table(Tex.underline2, tot -> tot.add("[stat]" + group.type.localizedName + "[]"));
                            to.row();
                            to.add(bundle.format("shar-stat-waveAmount", amount));
                            to.row();
                            to.add(bundle.format("shar-stat-waveShield", group.getShield(j)));
                            to.row();
                            if(group.effect != null) {
                                if(group.effect == StatusEffects.none) return;
                                Image status = new Image(group.effect.uiIcon).setScaling(Scaling.fit);
                                if(group.effect == StatusEffects.boss){
                                    status = new Image(Icon.warning.getRegion()).setScaling(Scaling.fit);
                                    Image finalStatus = status;
                                    status.update(() -> finalStatus.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f))));
                                }
                                Image finalStatus = status;
                                to.table(tot -> {
                                    tot.left();
                                    tot.add(bundle.get("shar-stat.waveStatus"));
                                    tot.add(finalStatus).size(8 * 3);
                                    if(!mobile){
                                        HandCursorListener listener = new HandCursorListener();
                                        finalStatus.addListener(listener);
                                        finalStatus.update(() -> finalStatus.color.lerp(!listener.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta)));
                                    }
                                    tot.add("[stat]" + group.effect.localizedName);
                                }).size(iconMed);
                                to.row();
                            }
                            if(group.items != null) {
                                to.table(tot -> {
                                    tot.left();
                                    ItemStack stack = group.items;
                                    tot.add(bundle.get("shar-stat.waveItem"));
                                    tot.add(new ItemImage(stack)).size(8 * 3);
                                    if(!mobile){
                                        HandCursorListener listener = new HandCursorListener();
                                        tot.addListener(listener);
                                        tot.update(() -> tot.color.lerp(!listener.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta)));
                                    }
                                    tot.add("[stat]" + stack.item.localizedName);
                                }).size(iconMed);
                                to.row();
                            }
                        })));
                    });
                    if(row % 4 == 0) tx.row();
                }
            });
            table.row();
        }
    }

    public void addWaveTable(){
        if(uiIndex != 1) return;
        ScrollPane wavePane = new ScrollPane(new Image(Core.atlas.find("clear")).setScaling(Scaling.fit), Styles.smallPane);
        wavePane.setScrollingDisabled(true, false);
        wavePane.setScrollYForce(waveScrollPos);
        wavePane.update(() -> {
            if(wavePane.hasScroll()){
                Element result = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if(result == null || !result.isDescendantOf(wavePane)){
                    Core.scene.setScrollFocus(null);
                }
            }
            waveScrollPos = wavePane.getScrollY();
            if(waveamount != settings.getInt("wavemax"))
                wavePane.setWidget(new Table(tx -> tx.table(this::setWave).left()));
        });
        wavePane.setOverscroll(false, false);
        wavePane.setWidget(new Table(tx -> tx.table(this::setWave).left()));
        waveTable = new Table(table -> {
            table.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.defaults().minWidth(Scl.scl(25 * 8f)).scaling(Scaling.fit).left();
                t.add(wavePane).maxHeight(Scl.scl(32 * 8f));
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                    Draw.reset();
                }
            }).padRight(Scl.scl(39 * 8f));

            table.fillParent = true;
            table.visibility = () -> uiIndex == 1;
        });
    }

    public void setCore(Table table){
        table.table(t -> {
            if(Vars.player.unit() == null) return;

            for(int i = 0; i < coreItems.tables.size; i++){
                coreamount = coreItems.teams[i].cores().size;
                if(coreItems.teams[i].cores().isEmpty()) continue;
                if(state.rules.pvp && coreItems.teams[i] != player.team()) continue;
                int finalI = i;
                t.table(Tex.underline2, head -> {
                    head.table(label -> {
                        label.center();
                        label.label(() -> "[#" + coreItems.teams[finalI].color.toString() + "]" + coreItems.teams[finalI].name + "[]");
                    });
                });
                t.row();
                for(int r = 0; r < coreamount; r++) {
                    CoreBlock.CoreBuild core = coreItems.teams[i].cores().get(r);

                    if(coreamount > 1 && r % 3 == 0) t.row();
                    else if(r % 3 == 0) t.row();

                    t.table(tt -> {
                        tt.add(new Stack(){{
                            add(new Table(s -> {
                                s.left();
                                Image image = new Image(core.block.uiIcon);
                                image.clicked(() -> {
                                    if (control.input instanceof DesktopInput)
                                        ((DesktopInput) control.input).panning = true;
                                    Core.camera.position.set(core.x, core.y);
                                });
                                if(!mobile) {
                                    HandCursorListener listener1 = new HandCursorListener();
                                    image.addListener(listener1);
                                    image.update(() -> image.color.lerp(!listener1.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta)));
                                }
                                image.addListener(new Tooltip(t -> t.background(Tex.button).label(() -> "([#" + Tmp.c1.set(Color.green).lerp(Color.red, 1 - core.healthf()).toString() + "]" + Strings.fixed(core.health, 2) + "[]/" + Strings.fixed(core.block.health, 2) + ")")));
                                s.add(image).size(iconLarge).scaling(Scaling.fit);
                            }));

                            add(new Table(s -> {
                                s.bottom().defaults().growX().height(Scl.scl(9)).pad(4);
                                s.add(new Bar(() -> "", () -> Pal.health, core::healthf));
                                s.pack();
                            }));
                        }});
                        tt.row();
                        tt.label(() -> "(" + (int) core.x / 8 + ", " + (int) core.y / 8 + ")");
                    });
                }
                t.row();
            }
        });
    }

    public void addCoreTable(){
        if(uiIndex != 2) return;
        ScrollPane corePane = new ScrollPane(new Table(tx -> tx.table(this::setCore).left()), Styles.smallPane);
        corePane.setScrollingDisabled(true, false);
        corePane.setScrollYForce(coreScrollPos);
        corePane.update(() -> {
            if(corePane.hasScroll()){
                Element result = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if(result == null || !result.isDescendantOf(corePane)){
                    Core.scene.setScrollFocus(null);
                }
            }
            coreScrollPos = corePane.getScrollY();
        });
        corePane.setWidget(new Table(tx -> tx.table(this::setCore).left()));
        corePane.setOverscroll(false, false);

        coreTable = new Table(table -> {
            table.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.defaults().minWidth(Scl.scl(25 * 8f)).scaling(Scaling.fit).left();
                t.add(corePane).maxHeight(Scl.scl(32 * 8f));
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                    Draw.reset();
                }
            }).padRight(Scl.scl(39 * 8f));

            table.fillParent = true;
            table.visibility = () -> uiIndex == 2;
        });
    }

    public void setTile(Table table){
        table.table(t -> {
                Tile tile = getTile();
            t.table(Tex.underline2, head -> {
                head.table(image -> {
                    image.left();
                    if(tile == null) return;
                    if(tile.floor().uiIcon != Core.atlas.find("error")) image.image(tile.floor().uiIcon);
                    if(tile.overlay().uiIcon != Core.atlas.find("error")) image.image(tile.overlay().uiIcon);
                    if(tile.block().uiIcon != Core.atlas.find("error")) image.image(tile.block().uiIcon);
                });
                head.label(() -> tile == null ? "(null, null)" : "(" + tile.x + ", " + tile.y + ")").center();
            });
        });
    }

    public void addTileTable(){
        if(uiIndex != 3) return;
        ScrollPane tilePane = new ScrollPane(new Image(Core.atlas.find("clear")).setScaling(Scaling.fit), Styles.smallPane);
        tilePane.setScrollingDisabled(true, false);
        tilePane.setScrollYForce(tileScrollPos);
        tilePane.update(() -> {
            if(tilePane.hasScroll()){
                Element result = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if(result == null || !result.isDescendantOf(tilePane)){
                    Core.scene.setScrollFocus(null);
                }
            }
            tileScrollPos = tilePane.getScrollY();
            tilePane.setWidget(new Table(tx -> tx.table(this::setTile).left()));
        });

        tilePane.setOverscroll(false, false);
        tileTable = new Table(table -> {
            table.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.defaults().minWidth(Scl.scl(25 * 8f)).scaling(Scaling.fit).left();
                t.add(tilePane).maxHeight(Scl.scl(32 * 8f));
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                    Draw.reset();
                }
            }).padRight(Scl.scl(39 * 8f));

            table.fillParent = true;
            table.visibility = () -> uiIndex == 3;
        });
    }

    public void setItem(Table table){
        table.table(t -> {
            for(int i = 0; i < coreItems.tables.size; i++){
                if((state.rules.pvp && coreItems.teams[i] != player.team()) || coreItems.teams[i].cores().isEmpty()) continue;
                int finalI = i;
                t.background(Tex.underline2).label(() -> "[#" + coreItems.teams[finalI].color.toString() + "]" + coreItems.teams[finalI].name + "[]").center();
                t.row();
                t.add(coreItems.tables.get(finalI)).left();
                t.row();
            }
        });
    }

    public void addItemTable(){
        if(uiIndex != 4) return;
        ScrollPane tilePane = new ScrollPane(new Image(Core.atlas.find("clear")).setScaling(Scaling.fit), Styles.smallPane);
        tilePane.setScrollingDisabled(true, false);
        tilePane.setScrollYForce(tileScrollPos);
        tilePane.update(() -> {
            if(tilePane.hasScroll()){
                Element result = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if(result == null || !result.isDescendantOf(tilePane)){
                    Core.scene.setScrollFocus(null);
                }
            }
            itemScrollPos = tilePane.getScrollY();
        });
        tilePane.setWidget(new Table(this::setItem).left());

        tilePane.setOverscroll(false, false);
        itemTable = new Table(table -> {
            table.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.defaults().minWidth(Scl.scl(25 * 8f)).scaling(Scaling.fit).left();
                t.add(tilePane).maxHeight(Scl.scl(32 * 8f));
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                    Draw.reset();
                }
            }).padRight(Scl.scl(39 * 8f));

            table.fillParent = true;
            table.visibility = () -> uiIndex == 4;
        });
    }
}
