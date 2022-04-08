package UnitInfo.core;

import UnitInfo.ui.*;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;

import mindustry.*;
import mindustry.entities.Units;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.Ranged;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.storage.*;

import static UnitInfo.SVars.*;
import static arc.Core.*;
import static mindustry.Vars.*;

public class HudUi {
    public Table mainTable = new Table();
    public Table baseTable = new Table();
    public Table waveInfoTable = new Table();
    public UnitDisplay unitTable;
    public WaveDisplay waveTable;
    public CoreDisplay itemTable;
    public SchemDisplay schemTable;

    public Teamc shotTarget;
    public Teamc lockedTarget;
    public boolean locked = false;

    public boolean waveShown;

    public float a;
    public int uiIndex = 3;

    CoresItemsDisplay coreItems = new CoresItemsDisplay();

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

    float heat = 0;
    public void setEvents() {
        Events.on(EventType.WaveEvent.class, e -> waveTable.rebuild());
        Events.on(EventType.WorldLoadEvent.class, e -> itemTable.rebuild());
        Events.run(EventType.Trigger.update, ()->{
            if(unitTable!=null) unitTable.setEvent();
            itemTable.setEvent();
            OverDrawer.target = getTarget();
            OverDrawer.locked = locked;
            if(settings.getBool("deadTarget") && locked && lockedTarget != null && !Groups.all.contains(e -> e == lockedTarget)) {
                lockedTarget = null;
                locked = false;
            }
            heat+=Time.delta;
            if(heat>60) {
                heat=0;
                schemTable.setSchemTable();
            }

            if(Scl.scl(modUiScale) != settings.getInt("infoUiScale") / 100f){
                modUiScale = settings.getInt("infoUiScale") / 100f;
                if(modUiScale <= 0) {
                    Log.warn("ui scaling reached zero");
                    modUiScale = 0.25f;
                }
                mainTable.clearChildren();
                addTable();
                coreItems.rebuild();
            }

            if(settings.getBool("autoShooting")) {
                Unit unit = player.unit();
                if (unit.type == null) return;
                boolean omni = unit.type.omniMovement;
                boolean validHealTarget = unit.type.canHeal && shotTarget instanceof Building b && b.isValid() && b.damaged() && shotTarget.team() == unit.team && shotTarget.within(unit, unit.type.range);
                boolean boosted = (unit instanceof Mechc && unit.isFlying());
                if ((unit.type != null && Units.invalidateTarget(shotTarget, unit, unit.type.range) && !validHealTarget) || state.isEditor()) {
                    shotTarget = null;
                }

                float mouseAngle = unit.angleTo(unit.aimX(), unit.aimY());
                boolean aimCursor = omni && player.shooting && unit.type.hasWeapons() && unit.type.faceTarget && !boosted && unit.type.rotateShooting;
                unit.lookAt(aimCursor ? mouseAngle : unit.prefRotation());

                //update shooting if not building + not mining
                if(!player.unit().activelyBuilding() && player.unit().mineTile == null) {
                    if(input.keyDown(KeyCode.mouseLeft)) {
                        player.shooting = !boosted;
                        unit.aim(player.mouseX = input.mouseWorldX(), player.mouseY = input.mouseWorldY());
                    } else if(shotTarget == null) {
                        player.shooting = false;
                        if(unit instanceof BlockUnitUnit b) {
                            if(b.tile() instanceof ControlBlock c && !c.shouldAutoTarget()) {
                                Building build = b.tile();
                                float range = build instanceof Ranged ? ((Ranged) build).range() : 0f;
                                boolean targetGround = build instanceof Turret.TurretBuild && ((Turret) build.block).targetAir;
                                boolean targetAir = build instanceof Turret.TurretBuild && ((Turret) build.block).targetGround;
                                shotTarget = Units.closestTarget(build.team, build.x, build.y, range, u -> u.checkTarget(targetAir, targetGround), u -> targetGround);
                            }
                            else shotTarget = null;
                        } else if(unit.type != null) {
                            float range = unit.hasWeapons() ? unit.range() : 0f;
                            shotTarget = Units.closestTarget(unit.team, unit.x, unit.y, range, u -> u.checkTarget(unit.type.targetAir, unit.type.targetGround), u -> unit.type.targetGround);

                            if(unit.type.canHeal && shotTarget == null) {
                                shotTarget = Geometry.findClosest(unit.x, unit.y, indexer.getDamaged(Team.sharded));
                                if (shotTarget != null && !unit.within(shotTarget, range)) {
                                    shotTarget = null;
                                }
                            }
                        }
                    } else {
                        player.shooting = !boosted;
                        unit.rotation(Angles.angle(unit.x, unit.y, shotTarget.x(), shotTarget.y()));
                        unit.aim(shotTarget.x(), shotTarget.y());
                    }
                }
                unit.controlWeapons(player.shooting && !boosted);
            }
        });

        Events.on(EventType.BlockDestroyEvent.class, e -> {
            if(e.tile.block() instanceof CoreBlock) coreItems.resetUsed();
        });
        Events.on(EventType.CoreChangeEvent.class, e -> coreItems.resetUsed());
        Events.on(EventType.ResetEvent.class, e -> coreItems.resetUsed());
    }

    public void setLeftUnitTable(Table table) {
        table.table(t -> {
            t.center();
            int[] i = {0};

            content.units().each(type -> Groups.unit.contains(u -> u.type == type && (state.rules.pvp ? (u.team != player.team()) : (u.team == state.rules.waveTeam)) && u.isBoss()), type -> {
                t.table(tt ->
                    tt.stack(
                        new Table(ttt -> ttt.image(type.uiIcon).size(iconSmall)),
                        new Table(ttt -> {
                            ttt.right().bottom();
                            Label label = new Label(() -> Groups.unit.count(u -> u.type == type && (state.rules.pvp ? (u.team != player.team()) : (u.team == state.rules.waveTeam)) && u.isBoss()) + "");
                            label.setFontScale(0.75f);
                            ttt.add(label);
                            ttt.pack();
                        }),
                        new Table(ttt -> {
                            ttt.top().right();
                            Image image = new Image(Icon.warning.getRegion()).setScaling(Scaling.fit);
                            image.update(() -> image.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f))));
                            ttt.add(image).size(Scl.scl(modUiScale) * 12f);
                            ttt.pack();
                        })
                    ).pad(6)
                );
                if(++i[0] % 6 == 0) t.row();
            });
            t.row();
            i[0] = 0;
            content.units().each(type -> Groups.unit.contains(u -> u.type == type && (state.rules.pvp ? (u.team != player.team()) : (u.team == state.rules.waveTeam)) && !u.isBoss()), type -> {
                t.table(tt ->
                    tt.add(new Stack() {{
                        add(new Table(ttt -> ttt.add(new Image(type.uiIcon)).size(iconSmall)));
                        add(new Table(ttt -> {
                            ttt.right().bottom();
                            Label label = new Label(() -> Groups.unit.count(u -> u.type == type &&(state.rules.pvp ? (u.team != player.team()) : (u.team == state.rules.waveTeam)) && !u.isBoss()) + "");
                            label.setFontScale(0.75f);
                            ttt.add(label);
                            ttt.pack();
                        }));
                    }}).pad(6)
                );
                if(++i[0] % 6 == 0) t.row();
            });
        });
    }

    public void setTile(Table table){
        table.table(t ->
            t.table(Tex.underline2, head -> {
                head.table(image -> {
                    image.left();
                    image.image(() -> getTile() == null ? clear : getTile().floor().uiIcon == error ? clear : getTile().floor().uiIcon).size(iconSmall);
                    image.image(() -> getTile() == null ? clear : getTile().overlay().uiIcon == error ? clear : getTile().overlay().uiIcon).size(iconSmall);
                    image.image(() -> getTile() == null ? clear : getTile().block().uiIcon == error ? clear : getTile().block().uiIcon).size(iconSmall);
                });
                Label label = new Label(() -> getTile() == null ? "(null, null)" : "(" + getTile().x + ", " + getTile().y + ")");
                head.add(label).center();
            })
        );
    }

    public void addSchemTable() {
        Table table = (Table) scene.find("minimap/position");
        table.row();
        schemTable=new SchemDisplay();
        table.add(schemTable);
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

            pathBtn.addListener(new Tooltip(l -> l.label(() -> bundle.get("hud.pathline") + " " + (pathLine ? bundle.get("hud.enabled") : bundle.get("hud.disabled")))));
            pathBtn.clicked(() -> {
                pathLine = !pathLine;
                pathBtn.setChecked(pathLine);
            });

            unitBtn.addListener(new Tooltip(l -> l.label(() -> bundle.get("hud.unitline") + " " + (unitLine ? bundle.get("hud.enabled") : bundle.get("hud.disabled")))));
            unitBtn.clicked(() -> {
                unitLine = !unitLine;
                unitBtn.setChecked(unitLine);
            });

            logicBtn.addListener(new Tooltip(l -> l.label(() -> bundle.get("hud.logicline") + " " + (logicLine ? bundle.get("hud.enabled") : bundle.get("hud.disabled")))));
            logicBtn.clicked(() -> {
                logicLine = !logicLine;
                logicBtn.setChecked(logicLine);
            });

            t.add(pathBtn).padLeft(4 * 8f).size(3 * 8f).row();
            t.add(unitBtn).padLeft(4 * 8f).size(3 * 8f).row();
            t.add(logicBtn).padLeft(4 * 8f).size(3 * 8f).row();
        });

        Table waveTable = (Table) scene.find("waves");
        Table infoTable = (Table) scene.find("infotable");
        waveTable.removeChild(infoTable);
        waveTable.row();
        waveTable.stack(
            new Table(tt -> tt.collapser(t -> t.stack(waveInfoTable, infoTable, pathlineTable).growX(), true, () -> waveShown).growX()).top(),
            new Table(tt -> tt.button(Icon.downOpen, Styles.clearToggleTransi, () -> waveShown = !waveShown).size(4 * 8f).checked(b -> {
                b.getImage().setDrawable(waveShown ? Icon.upOpen : Icon.downOpen);
                return waveShown;
            })).left().top()).fillX();
    }

    public void reset(int index, Seq<Button> buttons, Label label, Table table, Table labelTable, String hud){
        uiIndex = index;
        buttons.each(b -> b.setChecked(buttons.indexOf(b) == index));
        label.setText(bundle.get(hud));
        table.removeChild(baseTable);
        labelTable.setPosition(buttons.items[uiIndex].x, buttons.items[uiIndex].y);
        waveTable = new WaveDisplay();
        itemTable = new CoreDisplay();
        baseTable = table.table(tt -> tt.stack(waveTable, itemTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).left().get();
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
            waveTable = new WaveDisplay();
            itemTable = new CoreDisplay();
            baseTable = table.table(tt -> tt.stack(waveTable, itemTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).left().get();

            table.fillParent = true;
            table.visibility = () -> ui.hudfrag.shown && !ui.minimapfrag.shown();
        });
        ui.hudGroup.addChild(mainTable);
    }
}
