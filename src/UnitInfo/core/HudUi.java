package UnitInfo.core;

import UnitInfo.ui.*;
import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;

import static UnitInfo.SVars.*;
import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.gen.Tex.scrollKnobVerticalThin;

public class HudUi {
    Seq<Element> bars = new Seq<>();
    Table mainTable = new Table();
    Table baseTable = new Table();
    Table unitTable = new Table();
    Table waveTable = new Table();
    Table itemTable = new Table();
    Table waveInfoTable = new Table();
    float waveScrollPos;
    float itemScrollPos;

    Teamc lockedTarget;
    ImageButton lockButton;
    boolean locked = false;

    float a;
    int uiIndex = 0;

    //to update tables
    int waveamount;
    int enemyamount;

    //is this rly good idea?
    Seq<String> strings = Seq.with("","","","","","");
    FloatSeq numbers = FloatSeq.with(0f,0f,0f,0f,0f,0f);
    Seq<Color> colors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);
    Seq<Color> lastColors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);
    CoresItemsDisplay coreItems = new CoresItemsDisplay(Team.baseTeams);

    public final Rect scissor = new Rect();

    @SuppressWarnings("unchecked")
    public <T extends Teamc> T getTarget(){
        if(locked && lockedTarget != null) {
            if(settings.getBool("deadTarget") && !Groups.all.contains(e -> e == lockedTarget)) {
                lockedTarget = null;
                locked = false;
            }
            else return (T) lockedTarget; //if there is locked target, return it first.
        }


        Seq<Unit> units = Groups.unit.intersect(input.mouseWorldX(), input.mouseWorldY(), 4, 4); // well, 0.5tile is enough to search them
        if(units.size > 0)
            return (T) units.peek(); //if there is unit, return it.
        else if(getTile() != null && getTile().build != null)
            return (T) getTile().build; //if there isn't unit but there is build, return it.
        else if(player.unit() instanceof BlockUnitUnit b && b.tile() != null)
            return (T)b.tile();
        return (T) player.unit(); //if there aren't unit and not build, return player.
    }

    public @Nullable Tile getTile(){
        return Vars.world.tileWorld(input.mouseWorldX(), input.mouseWorldY());
    }

    public void setEvents() {

        Events.run(EventType.Trigger.update, ()->{
            if(settings.getBool("deadTarget") && locked && lockedTarget != null && !Groups.all.contains(e -> e == lockedTarget)) {
                lockedTarget = null;
                locked = false;
            }

            if(Scl.scl(modUiScale) != settings.getInt("infoUiScale") / 100f){
                modUiScale = settings.getInt("infoUiScale") / 100f;
                mainTable.clearChildren();
                addTable();
                coreItems.rebuild();
            }

            if((input.keyDown(KeyCode.shiftRight) || input.keyDown(KeyCode.shiftLeft))){
                if(input.keyTap(KeyCode.r)) lockButton.change();
            }
        });

        Events.on(EventType.BlockDestroyEvent.class, e -> {
            if(e.tile.block() instanceof CoreBlock)coreItems.resetUsed();
        });
        Events.on(EventType.CoreChangeEvent.class, e -> coreItems.resetUsed());
        Events.on(EventType.ResetEvent.class, e -> coreItems.resetUsed());
    }

    public void setLeftUnitTable(Table table) {
        table.table(t -> {
            t.center();
            int[] i = {0};
            enemyamount = Groups.unit.count(u -> u.team == state.rules.waveTeam);
            content.units().each(type -> Groups.unit.contains(u -> u.type == type && u.team == state.rules.waveTeam && u.isBoss()), type -> {
                t.table(tt -> {
                    tt.add(new Stack() {{
                        add(new Table(ttt -> {
                            ttt.image(type.uiIcon).size(iconSmall);
                        }));
                        add(new Table(ttt -> {
                            ttt.right().bottom();
                            Label label = new Label(() -> Groups.unit.count(u -> u.type == type && u.team == state.rules.waveTeam && u.isBoss()) + "");
                            label.setFontScale(0.75f);
                            ttt.add(label);
                            ttt.pack();
                        }));
                        add(new Table(ttt -> {
                            ttt.top().right();
                            Image image = new Image(Icon.warning.getRegion()).setScaling(Scaling.fit);
                            image.update(() -> image.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f))));
                            ttt.add(image).size(Scl.scl(modUiScale) * 12f);
                            ttt.pack();
                        }));
                    }}).pad(6);
                });
                if(++i[0] % 6 == 0) t.row();
            });
            t.row();
            i[0] = 0;
            content.units().each(type -> Groups.unit.contains(u -> u.type == type && u.team == state.rules.waveTeam && !u.isBoss()), type -> {
                t.table(tt -> {
                    tt.add(new Stack() {{
                        add(new Table(ttt -> {
                            ttt.add(new Image(type.uiIcon)).size(iconSmall);
                        }));
                        add(new Table(ttt -> {
                            ttt.right().bottom();
                            Label label = new Label(() -> Groups.unit.count(u -> u.type == type && u.team == state.rules.waveTeam && !u.isBoss()) + "");
                            label.setFontScale(0.75f);
                            ttt.add(label);
                            ttt.pack();
                        }));
                    }}).pad(6);
                });
                if(++i[0] % 6 == 0) t.row();
            });
        });
    }

    public void setTile(Table table){
        table.table(t -> {
            t.table(Tex.underline2, head -> {
                head.table(image -> {
                    image.left();
                    image.image(() -> getTile() == null ? clear : getTile().floor().uiIcon == error ? clear : getTile().floor().uiIcon).size(iconSmall);
                    image.image(() -> getTile() == null ? clear : getTile().overlay().uiIcon == error ? clear : getTile().overlay().uiIcon).size(iconSmall);
                    image.image(() -> getTile() == null ? clear : getTile().block().uiIcon == error ? clear : getTile().block().uiIcon).size(iconSmall);
                });
                Label label = new Label(() -> getTile() == null ? "(null, null)" : "(" + getTile().x + ", " + getTile().y + ")");
                head.add(label).center();
            });
        });
    }

    public void addWaveInfoTable() {
        waveInfoTable = new Table(Tex.buttonEdge4, t -> {
            t.defaults().width(34 * 8f).center();
            t.table().update(tt -> {
                tt.clear();
                setTile(tt);
                tt.row();
                setLeftUnitTable(tt);
            });
        });

        Table pathlineTable = new Table(t -> {
            t.right();

            Button pathBtn = new ImageButton(new ScaledNinePatchDrawable(new NinePatch(Icon.grid.getRegion()), 0.5f), Styles.clearToggleTransi);
            Button unitBtn = new ImageButton(new ScaledNinePatchDrawable(new NinePatch(Icon.grid.getRegion()), 0.5f), Styles.clearToggleTransi);
            Button logicBtn = new ImageButton(new ScaledNinePatchDrawable(new NinePatch(Icon.grid.getRegion()), 0.5f), Styles.clearToggleTransi);

            pathBtn.addListener(new Tooltip(l -> l.label(() -> "PathLine " + (pathLine ? "[accent]Enabled[]" : "[gray]Disabled[]"))));
            pathBtn.clicked(() -> {
                pathLine = !pathLine;
                pathBtn.setChecked(pathLine);
            });

            unitBtn.addListener(new Tooltip(l -> l.label(() -> "UnitLine " + (unitLine ? "[accent]Enabled[]" : "[gray]Disabled[]"))));
            unitBtn.clicked(() -> {
                unitLine = !unitLine;
                unitBtn.setChecked(unitLine);
            });

            logicBtn.addListener(new Tooltip(l -> l.label(() -> "LogicLine " + (logicLine ? "[accent]Enabled[]" : "[gray]Disabled[]"))));
            logicBtn.clicked(() -> {
                logicLine = !logicLine;
                logicBtn.setChecked(logicLine);
            });

            t.add(pathBtn).padLeft(4 * 8f).size(3 * 8f).row();
            t.add(unitBtn).padLeft(4 * 8f).size(3 * 8f).row();
            t.add(logicBtn).padLeft(4 * 8f).size(3 * 8f).row();
        });

        Table waveTable = (Table)((Group)((Group)ui.hudGroup.getChildren().get(5)) //HudFragment#118, name: overlaymarker
                .getChildren().get(mobile ? 2 : 0)) //HudFragment#192, name: wave/editor
                .getChildren().get(0); //HudFragment#196, name: waves
        Table table = (Table)waveTable.getChildren().get(0); //HudFragment#198, name: x
        Table statusTable = (Table) waveTable.getChildren().get(1);
        waveTable.removeChild(statusTable);
        table.row();

        table.stack(waveInfoTable, statusTable.top(), pathlineTable);
    }

    public void reset(int index, Seq<Button> buttons, Label label, Table table, Table labelTable, String hud){
        uiIndex = index;
        buttons.each(b -> b.setChecked(buttons.indexOf(b) == index));
        label.setText(bundle.get(hud));
        addBars();
        addUnitTable();
        addWaveTable();
        addItemTable();
        table.removeChild(baseTable);
        labelTable.setPosition(buttons.items[uiIndex].x, buttons.items[uiIndex].y);
        baseTable = table.table(tt -> tt.stack(unitTable, waveTable, itemTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
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
            label.setStyle(new Label.LabelStyle(){{
                font = Fonts.outline;
                fontColor = Color.white;
                background = Styles.black8;
            }});
            label.setFontScale(Scl.scl(modUiScale));
            Table labelTable = new Table(t -> t.add(label).left().padRight(Scl.scl(modUiScale) * 40 * 8f));

            table.table(t -> {
                Seq<Button> buttons = Seq.with(null, null, null, null);
                Seq<String> strs = Seq.with("hud.unit", "hud.wave", "hud.item", "hud.cancel");
                Seq<TextureRegionDrawable> icons = Seq.with(Icon.units, Icon.fileText, Icon.copy, Icon.cancel);
                for(int i = 0; i < buttons.size; i++){
                    int finalI = i;
                    buttons.set(i, t.button(new ScaledNinePatchDrawable(new NinePatch(icons.get(i).getRegion()), modUiScale), Styles.clearToggleTransi, () ->
                        reset(finalI, buttons, label, table, labelTable, strs.get(finalI))).size(Scl.scl(modUiScale) * 5 * 8f).get());
                    t.row();
                }
            });
            baseTable = table.table(tt -> tt.stack(unitTable, waveTable, itemTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
            table.fillParent = true;

            table.visibility = () -> ui.hudfrag.shown && !ui.minimapfrag.shown();
        });
        ui.hudGroup.addChild(mainTable);
    }

    public TextureRegion getRegions(int i){
        Teamc target = getTarget();

        TextureRegion region = clear;
        if(i == 0){
            if(target instanceof Healthc) region = SIcons.health;
        } else if(i == 1){
            if(target instanceof Turret.TurretBuild ||
                    target instanceof MassDriver.MassDriverBuild){
                region = SIcons.reload;
            } else if((target instanceof Unit unit && unit.type != null) ||
                    target instanceof ForceProjector.ForceBuild){
                region = SIcons.shield;
            } else if(target instanceof MendProjector.MendBuild ||
                    target instanceof OverdriveProjector.OverdriveBuild ||
                    target instanceof ConstructBlock.ConstructBuild ||
                    target instanceof UnitFactory.UnitFactoryBuild ||
                    target instanceof Reconstructor.ReconstructorBuild ||
                    target instanceof Drill.DrillBuild ||
                    target instanceof GenericCrafter.GenericCrafterBuild){
                //region = SIcons.progress;
            } else if(target instanceof PowerNode.PowerNodeBuild ||
                    target instanceof PowerGenerator.GeneratorBuild){
                region = SIcons.power;
            }
        } else if(i == 2){
            if(target instanceof ItemTurret.ItemTurretBuild){
                region = SIcons.ammo;
            } else if(target instanceof LiquidTurret.LiquidTurretBuild){
                region = SIcons.liquid;
            } else if(target instanceof PowerTurret.PowerTurretBuild ||
                    target instanceof PowerNode.PowerNodeBuild){
                region = SIcons.power;
            } else if((target instanceof Building b && b.block.hasItems) ||
                    (target instanceof Unit unit && unit.type != null)){
                region = SIcons.item;
            }
        } else if(i == 3){
            if(target instanceof Unit unit && unit.type != null ||
                    target instanceof UnitFactory.UnitFactoryBuild ||
                    target instanceof Reconstructor.ReconstructorBuild){
                //region = SIcons.unit;
            } else if(target instanceof AttributeCrafter.AttributeCrafterBuild ||
                    target instanceof SolidPump.SolidPumpBuild ||
                    target instanceof ThermalGenerator.ThermalGeneratorBuild){
                //region = SIcons.attr;
            } else if(target instanceof PowerNode.PowerNodeBuild){
                region = SIcons.power;
            } else if(target instanceof OverdriveProjector.OverdriveBuild){
                //region = SIcons.boost;
            }
        } else if(i == 4){
            if(target instanceof Unit unit && target instanceof Payloadc && unit.type != null){

            } else if(target instanceof PowerNode.PowerNodeBuild){
                region = SIcons.power;
            } else if(target instanceof Building b && b.block.hasLiquids){
                region = SIcons.liquid;
            }
        } else if(i == 5){
            if(target instanceof Unit unit && state.rules.unitAmmo && unit.type != null){
                region = SIcons.ammo;
            }else if(target instanceof PowerNode.PowerNodeBuild ||
                    (target instanceof Building b && b.block.consumes.hasPower())){
                region = SIcons.power;
            }
        }

        return region;
    }
    public Element addBar(int i){
        return new Stack(){{
            add(new Table(t -> {
                t.add(new SBar(
                        () -> BarInfo.strings.get(i),
                        () -> {
                            if (BarInfo.colors.get(i) != Color.clear) lastColors.set(i, BarInfo.colors.get(i));
                            return lastColors.get(i);
                        },
                        () -> BarInfo.numbers.get(i)
                )).width(Scl.scl(modUiScale) * 24 * 8f).height(Scl.scl(modUiScale) * 4 * 8f).growX().left();
            }));
            add(new Table(t -> {
                t.right();
                t.add(new Image(){
                    @Override
                    public void draw() {
                        validate();

                        float x = this.x;
                        float y = this.y;
                        float scaleX = this.scaleX;
                        float scaleY = this.scaleY;
                        Draw.color(Color.white);
                        Draw.alpha(parentAlpha * color.a);

                        TextureRegionDrawable region = new TextureRegionDrawable(getRegions(i));
                        float rotation = getRotation();
                        if(scaleX != 1 || scaleY != 1 || rotation != 0){
                            region.draw(x + imageX, y + imageY, originX - imageX, originY - imageY,
                                    imageWidth, imageHeight, scaleX, scaleY, rotation);
                            return;
                        }
                        region.draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);

                        Draw.color(colors.get(i));
                        if(ScissorStack.push(scissor.set(x, y, imageWidth * scaleX, imageHeight * scaleY * numbers.get(i)))){
                            region.draw(x, y, imageWidth * scaleX, imageHeight * scaleY);
                            ScissorStack.pop();
                        }
                        Draw.reset();
                    }
                }).size(iconMed * Scl.scl(modUiScale)).padRight(Scl.scl(modUiScale) * 8f);
            }));
        }};
    }
    public void addBars(){
        bars.clear();
        for(int i = 0; i < 6; i++) bars.add(addBar(i));
    }

    public void addWeaponTable(Table table){
        table.table().update(tt -> {
            tt.clear();
            if(getTarget() instanceof Unit u && u.type != null) {
                for(int r = 0; r < u.type.weapons.size; r++){
                    Weapon weapon = u.type.weapons.get(r);
                    WeaponMount mount = u.mounts[r];
                    int finalR = r;
                    tt.table(ttt -> {
                        ttt.left();
                        if((1 + finalR) % 4 == 0) ttt.row();
                        ttt.stack(
                            new Table(o -> {
                                o.left();
                                o.add(new Image(!weapon.name.equals("") && weapon.outlineRegion.found() ? weapon.outlineRegion : u.type.uiIcon){
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
                                                getDrawable().draw(x + imageX, y + imageY, originX - imageX, originY - imageY, imageWidth, imageHeight, scaleX, scaleY, rotation);
                                                return;
                                            }
                                        }
                                        y -= (mount.reload) / weapon.reload * weapon.recoil;
                                        if(getDrawable() != null)
                                            getDrawable().draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
                                    }
                                }).size(Scl.scl(modUiScale) * iconLarge);
                            }),
                            new Table(h -> {
                                h.defaults().growX().height(Scl.scl(modUiScale) * 9f).width(Scl.scl(modUiScale) * iconLarge).padTop(Scl.scl(modUiScale) * 18f);
                                h.add(new SBar(
                                        () -> "",
                                        () -> Pal.accent.cpy().lerp(Color.orange, mount.reload / weapon.reload),
                                        () -> mount.reload / weapon.reload).rect().init());
                                h.pack();
                            })
                        );
                    }).pad(4);
                }
            }
        });
    }

    public Table addInfoTable(Table table){
        return table.table(table1 -> {
            table1.left().top();

            float[] count = new float[]{-1};
            table1.table().update(t -> {
                if(!Core.settings.getBool("infoui")) return;
                if(getTarget() instanceof Payloadc payload){
                    if(count[0] != payload.payloadUsed()){
                        t.clear();
                        t.top().left();

                        float pad = 0;
                        float items = payload.payloads().size;
                        if(8 * 2 * items + pad * items > 275f){
                            pad = (275f - (8 * 2) * items) / items;
                        }
                        int i = 0;
                        for(Payload p : payload.payloads()){
                            t.image(p.icon()).size(8 * 2).padRight(pad);
                            if(++i % 12 == 0) t.row();
                        }

                        count[0] = payload.payloadUsed();
                    }
                }else{
                    count[0] = -1;
                    t.clear();
                }
            }).growX().visible(() -> getTarget() instanceof Payloadc p && p.payloadUsed() > 0).colspan(2);
            table1.row();

            Bits statuses = new Bits();
            table1.table().update(t -> {
                if(!Core.settings.getBool("infoui")) return;
                t.left();
                if(getTarget() instanceof Statusc st){
                    Bits applied = st.statusBits();
                    if(!statuses.equals(applied)){
                        t.clear();

                        if(applied != null){
                            for(StatusEffect effect : content.statusEffects()){
                                if(applied.get(effect.id) && !effect.isHidden()){
                                    t.image(effect.uiIcon).size(iconSmall).get().addListener(new Tooltip(l -> l.label(() ->
                                        effect.localizedName + " [lightgray]" + UI.formatTime(st.getDuration(effect))).style(Styles.outlineLabel)));
                                }
                            }
                            statuses.set(applied);
                        }
                    }
                }
            }).left();
        }).get();
    }

    public void addUnitTable(){
        if(uiIndex != 0) return;
        unitTable = new Table(table -> {
            table.left().defaults().width(Scl.scl(modUiScale) * 27 * 8f).maxHeight(Scl.scl(modUiScale) * 35 * 8f);
            addBars();
            Table table1 = new Table(Tex.button, t -> {
                t.table(Tex.underline2, tt -> {
                    Stack stack = new Stack(){{
                        add(new Table(ttt -> {
                            ttt.image(() -> {
                                TextureRegion region = clear;
                                if(getTarget() instanceof Unit u && u.type != null) region = u.type.uiIcon;
                                else if(getTarget() instanceof Building b) {
                                    if(getTarget() instanceof ConstructBlock.ConstructBuild cb) region = cb.current.uiIcon;
                                    else if(b.block != null) region = b.block.uiIcon;
                                }
                                return region;
                            }).size(Scl.scl(modUiScale) * 4 * 8f);
                        }));

                        add(new Table(ttt -> {
                            ttt.add(new Stack(){{
                                add(new Table(temp -> {
                                    temp.image(new ScaledNinePatchDrawable(new NinePatch(Icon.defenseSmall.getRegion()), modUiScale));
                                    temp.visibility = () -> getTarget() instanceof Unit;
                                }));

                                add(new Table(temp -> {
                                    Label label = new Label(() -> (getTarget() instanceof Unit u && u.type != null ? (int) u.type.armor + "" : ""));
                                    label.setColor(Pal.surge);
                                    label.setFontScale(Scl.scl(modUiScale) * 0.5f);
                                    temp.add(label).center();
                                    temp.pack();
                                }));
                            }}).padLeft(Scl.scl(modUiScale) * 2 * 8f).padBottom(Scl.scl(modUiScale) * 2 * 8f);
                        }));
                    }};

                    Label label = new Label(() -> {
                        String name = "";
                        if(getTarget() instanceof Unit u && u.type != null)
                            name = u.type.localizedName;
                        if(getTarget() instanceof Building b && b.block != null) {
                            if(getTarget() instanceof ConstructBlock.ConstructBuild cb) name = cb.current.localizedName;
                            else name = b.block.localizedName;
                        }
                        return "[accent]" + (name.length() > 12 ? name.substring(0, 12) + "..." : name) + "[]";
                    });
                    label.setFontScale(Scl.scl(modUiScale));

                    TextButton button = Elem.newButton("?", Styles.clearPartialt, () -> {
                        if(getTarget() instanceof Unit u && u.type != null)
                            ui.content.show(u.type);
                        if(getTarget() instanceof Building b && b.block != null) {
                            ui.content.show(b.block);
                        }
                    });
                    button.visibility = () -> getTarget() != null;
                    button.update(() -> {
                        if(!Core.settings.getBool("infoui")) return;
                        lockButton.getStyle().imageUp = Icon.lock.tint(locked ? Pal.accent : Color.white);
                    });
                    button.getLabel().setFontScale(Scl.scl(modUiScale));

                    lockButton = Elem.newImageButton(Styles.clearPartiali, Icon.lock.tint(locked ? Pal.accent : Color.white), 3 * 8f * Scl.scl(modUiScale), () -> {
                        locked = !locked;
                        lockedTarget = locked ? getTarget() : null;
                    });
                    lockButton.visibility = () -> !getTarget().isNull();

                    tt.top();
                    tt.add(stack);
                    tt.add(label);
                    tt.add(button).size(Scl.scl(modUiScale) * 5 * 8f);
                    tt.add(lockButton);

                    tt.clicked(()->{
                        if(getTarget() == null) return;
                        if(control.input instanceof DesktopInput d) d.panning = true;
                        Core.camera.position.set(getTarget().x(), getTarget().y());
                    });
                    tt.addListener(new Tooltip(tool -> tool.background(Tex.button).table(to -> {
                        to.table(Tex.underline2, tool2 -> {
                            Label label2 = new Label(()->{
                                if(getTarget() instanceof Unit u){
                                    if(u.isPlayer()) return u.getPlayer().name;
                                    if(u.type != null) return u.type.localizedName;
                                }
                                else if(getTarget() instanceof Building b) return b.block.localizedName;
                                return "";
                            });
                            label2.setFontScale(Scl.scl(modUiScale));
                            tool2.add(label2);
                        });
                        to.row();
                        Label label2 = new Label(()->getTarget() == null ? "(" + 0 + ", " + 0 + ")" : "(" + Strings.fixed(getTarget().x() / tilesize, 2) + ", " + Strings.fixed(getTarget().y() / tilesize, 2) + ")");
                        label2.setFontScale(Scl.scl(modUiScale));
                        to.add(label2);
                    })));
                    tt.update(() -> {
                        if(!Core.settings.getBool("infoui")) return;
                        tt.setBackground(((NinePatchDrawable)Tex.underline2).tint(getTarget().isNull() ? Color.gray : getTarget().team().color));
                    });
                });
                t.row();
                t.table(tt -> {
                    tt.defaults().width(Scl.scl(modUiScale) * 30f * 8f).height(Scl.scl(modUiScale) * 4f * 8f).pad(0,4,0,4).top();
                    for(Element bar : bars){
                        bar.setScale(Scl.scl(modUiScale));
                        tt.add(bar).growX().left();
                        tt.row();
                    }
                });
                t.row();
                addWeaponTable(t);
                t.update(() -> {
                    if(!Core.settings.getBool("infoui")) return;
                    NinePatchDrawable patch = (NinePatchDrawable)Tex.button;
                    t.setBackground(patch.tint(Tmp.c1.set(patch.getPatch().getColor()).a(settings.getInt("uiopacity") / 100f)));
                });
            });
            table.table(t -> t.stack(table1, addInfoTable(t))).padLeft(3f * 8f);

            table.update(() -> {
                if(!Core.settings.getBool("infoui")) return;
                try {
                    BarInfo.getInfo(getTarget());
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
                strings = BarInfo.strings;
                numbers = BarInfo.numbers;
                colors = BarInfo.colors;
            });

            table.visibility = () -> uiIndex == 0;
        });
    }

    public void setWave(Table table){
        table.defaults().minWidth(Scl.scl(modUiScale) * 46 * 8f);
        int winWave = state.isCampaign() && state.rules.winWave > 0 ? state.rules.winWave : Integer.MAX_VALUE;
        waveamount = settings.getInt("wavemax");
        for(int i = settings.getBool("pastwave") ? 0 : state.wave - 1; i <= Math.min(state.wave + waveamount, winWave - 2); i++){
            final int j = i;
            if(!settings.getBool("emptywave") && state.rules.spawns.find(g -> g.getSpawned(j) > 0) == null) continue;
            table.table(table1 -> {
                table1.left();
                table1.table(t -> {
                    Label label = new Label(() -> "[#" + (state.wave == j+1 ? Color.red.toString() : Pal.accent.toString()) + "]" + (j+1) + "[]");
                    label.setFontScale(Scl.scl(modUiScale));
                    t.add(label);
                });
                table1.table(Tex.underline, tx -> {
                    tx.defaults().marginRight(2 * 8f);
                    tx.fillParent = true;
                    if(settings.getBool("emptywave") && state.rules.spawns.find(g -> g.getSpawned(j) > 0) == null) {
                        tx.center();
                        Label label = new Label("[lightgray]<Empty>[]");
                        label.setFontScale(Scl.scl(modUiScale));
                        tx.add(label);
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
                        int amount = groupsTmp.get(group);
                        tx.table(tt -> {
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
                                    label.setFontScale(Scl.scl(modUiScale) * 0.85f);
                                    ttt.add(label);
                                    ttt.pack();
                                }),

                                new Table(ttt -> {
                                    ttt.top().right();
                                    Image image1 = new Image(Icon.warning.getRegion()).setScaling(Scaling.fit);
                                    image1.update(() -> {
                                        if(!Core.settings.getBool("infoui")) return;
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
                                HandCursorListener listener1 = new HandCursorListener();
                                tt.addListener(listener1);
                                tt.update(() -> {
                                    if(!Core.settings.getBool("infoui")) return;
                                    image.color.lerp(!listener1.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta));
                                });
                            }
                            tt.addListener(new Tooltip(t -> t.background(Tex.button).table(to -> {
                                to.left();
                                to.table(Tex.underline2, tot -> tot.add("[stat]" + group.type.localizedName + "[]")).row();
                                to.add(bundle.format("shar-stat-waveAmount", amount)).row();
                                to.add(bundle.format("shar-stat-waveShield", group.getShield(j))).row();
                                if(group.effect != null) {
                                    if(group.effect == StatusEffects.none) return;
                                    Image status = new Image(group.effect.uiIcon).setScaling(Scaling.fit);
                                    if(group.effect == StatusEffects.boss){
                                        status = new Image(Icon.warning.getRegion()).setScaling(Scaling.fit);
                                        Image finalStatus = status;
                                        status.update(() -> {
                                            if(!Core.settings.getBool("infoui")) return;
                                            finalStatus.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f)));
                                        });
                                    }
                                    Image finalStatus = status;
                                    to.table(tot -> {
                                        tot.left();
                                        tot.add(bundle.get("shar-stat.waveStatus"));
                                        tot.add(finalStatus).size(Scl.scl(modUiScale) * 3 * 8f);
                                        if(!mobile){
                                            HandCursorListener listener = new HandCursorListener();
                                            finalStatus.addListener(listener);
                                            finalStatus.update(() -> {
                                                if(!Core.settings.getBool("infoui")) return;
                                                finalStatus.color.lerp(!listener.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta));
                                            });
                                        }
                                        tot.add("[stat]" + group.effect.localizedName);
                                    }).size(iconMed * Scl.scl(modUiScale));
                                    to.row();
                                }
                                if(group.items != null) {
                                    to.table(tot -> {
                                        tot.left();
                                        ItemStack stack = group.items;
                                        tot.add(bundle.get("shar-stat.waveItem"));
                                        tot.add(new ItemImage(stack)).size(Scl.scl(modUiScale) * 3 * 8f);
                                        if(!mobile){
                                            HandCursorListener listener = new HandCursorListener();
                                            tot.addListener(listener);
                                            tot.update(() -> tot.color.lerp(!listener.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta)));
                                        }
                                        tot.add("[stat]" + stack.item.localizedName);
                                    }).size(iconMed * Scl.scl(modUiScale));
                                    to.row();
                                }
                            })));
                        });
                        if(++row % 8 == 0) tx.row();
                    }
                });
            });
            table.row();
        }
    }

    public void addWaveTable(){
        if(uiIndex != 1) return;
        waveTable = new Table(table -> {
            table.defaults().width(Scl.scl(modUiScale) * 54 * 8f).height(unitTable.getHeight());
            table.add(new Table(Tex.button, t -> {
                ScrollPane pane = t.pane(new ScrollPane.ScrollPaneStyle(){{
                    vScroll = Tex.clear;
                    vScrollKnob = new ScaledNinePatchDrawable(new NinePatch(((TextureRegionDrawable) scrollKnobVerticalThin).getRegion()), modUiScale);
                }}, new Table(this::setWave)).get();
                pane.update(() -> {
                    if(!Core.settings.getBool("infoui")) return;
                    if(pane.hasScroll()){
                        Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                        if(result == null || !result.isDescendantOf(pane)){
                            scene.setScrollFocus(null);
                        }
                    }
                    waveScrollPos = pane.getScrollY();
                    if(waveamount != settings.getInt("wavemax"))
                        pane.setWidget(new Table(this::setWave));
                });
                pane.setOverscroll(false, false);
                pane.setScrollingDisabled(true, false);
                pane.setScrollYForce(waveScrollPos);

                t.update(() -> {
                    NinePatchDrawable patch = (NinePatchDrawable)Tex.button;
                    t.setBackground(patch.tint(Tmp.c1.set(patch.getPatch().getColor()).a(settings.getInt("uiopacity") / 100f)));
                });
            })).padRight(Scl.scl(modUiScale) * 39 * 8f);

            table.fillParent = true;
            table.visibility = () -> uiIndex == 1;
        });
    }

    public void setItem(Table table){
        table.table().update(t -> {
            t.clear();
            for(int i = 0; i < coreItems.tables.size; i++){
                if((state.rules.pvp && coreItems.teams[i] != player.team()) || coreItems.teams[i].cores().isEmpty()) continue;
                int finalI = i;
                t.table(tt -> {
                    tt.center().defaults().width(Scl.scl(modUiScale) * 46 * 8f);
                    coreItems.tables.get(finalI).setBackground(((NinePatchDrawable)Tex.underline2).tint(coreItems.teams[finalI].color));
                    tt.add(coreItems.tables.get(finalI)).left();
                }).pad(4);
                t.row();
            }
        });
    }

    public void addItemTable(){
        if(uiIndex != 2) return;
        itemTable = new Table(table -> {
            table.left().defaults().width(Scl.scl(modUiScale) * 54 * 8f).height(unitTable.getHeight());
            table.table(Tex.button, t -> {
                ScrollPane pane = t.pane(new ScrollPane.ScrollPaneStyle(){{
                    vScroll = Tex.clear;
                    vScrollKnob = new ScaledNinePatchDrawable(new NinePatch(((TextureRegionDrawable) scrollKnobVerticalThin).getRegion()), modUiScale);
                }}, new Table(this::setItem).left()).get();
                pane.update(() -> {
                    Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                    if(pane.hasScroll() && (result == null || !result.isDescendantOf(pane)))
                        scene.setScrollFocus(null);
                    itemScrollPos = pane.getScrollY();
                });
                pane.setOverscroll(false, false);
                pane.setScrollingDisabled(true, false);
                pane.setScrollYForce(itemScrollPos);

                t.update(() -> {
                    NinePatchDrawable patch = (NinePatchDrawable)Tex.button;
                    t.setBackground(patch.tint(Tmp.c1.set(patch.getPatch().getColor()).a(settings.getInt("uiopacity") / 100f)));
                });
            }).padRight(Scl.scl(modUiScale) * 39 * 8f);

            table.fillParent = true;
            table.visibility = () -> uiIndex == 2;
        });
    }
}
