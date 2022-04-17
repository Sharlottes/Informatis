package UnitInfo.core;

import UnitInfo.ui.windows.*;
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
import mindustry.logic.Ranged;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.*;

import static UnitInfo.SVars.*;
import static arc.Core.*;
import static mindustry.Vars.*;

public class HudUi {
    public Table waveInfoTable = new Table();
    public SchemDisplay schemTable;

    public Teamc shotTarget;
    public Teamc lockedTarget;
    public boolean locked = false;

    public boolean waveShown;

    public float a;

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
        Events.run(EventType.Trigger.update, ()->{
            target = getTarget();
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
        Table table = ((Table) scene.find("minimap/position")).row();
        schemTable = new SchemDisplay();
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

        Table waveTable = (Table) scene.find("waves");
        Table infoTable = (Table) scene.find("infotable");
        waveTable.removeChild(infoTable);
        waveTable.row();
        waveTable.stack(
            new Table(tt -> tt.collapser(t -> t.stack(waveInfoTable, infoTable).growX(), true, () -> waveShown).growX()).top(),
            new Table(tt -> tt.button(Icon.downOpen, Styles.clearToggleTransi, () -> waveShown = !waveShown).size(4 * 8f).checked(b -> {
                b.getImage().setDrawable(waveShown ? Icon.upOpen : Icon.downOpen);
                return waveShown;
            })).left().top()).fillX();
    }
}
