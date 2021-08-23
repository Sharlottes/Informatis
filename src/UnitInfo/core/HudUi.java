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
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.type.ammo.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadMassDriver;
import mindustry.world.blocks.power.PowerNode;

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
    Table coreTable = new Table();
    Table itemTable = new Table();
    Table waveInfoTable = new Table();
    float waveScrollPos;
    float itemScrollPos;

    Teamc lockedTarget;
    ImageButton lockButton;
    boolean locked = false;

    float charge;
    float a;
    int uiIndex = 0;

    //to update tables
    int waveamount;
    int coreamounts;
    int enemyamount;

    //is this rly good idea?
    Seq<String> strings = Seq.with("","","","","","");
    FloatSeq numbers = FloatSeq.with(0f,0f,0f,0f,0f,0f);
    Seq<Color> colors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);
    Seq<Color> lastColors = Seq.with(Color.clear,Color.clear,Color.clear,Color.clear,Color.clear,Color.clear);
    CoresItemsDisplay coreItems = new CoresItemsDisplay(Team.baseTeams);
    @Nullable Teamc target;

    public Seq<MassDriver.MassDriverBuild> linkedMasses = new Seq<>();
    public Seq<PayloadMassDriver.PayloadDriverBuild> linkedPayloadMasses = new Seq<>();
    public Seq<Building> linkedNodes = new Seq<>();

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

    public void drawMassPayloadLink(PayloadMassDriver.PayloadDriverBuild from){
        Groups.build.each(b -> b instanceof PayloadMassDriver.PayloadDriverBuild fromMass &&
                world.build(fromMass.link) == from &&
                from.within(fromMass.x, fromMass.y, ((PayloadMassDriver)fromMass.block).range) &&
                !linkedPayloadMasses.contains(from), b -> {
            linkedPayloadMasses.add((PayloadMassDriver.PayloadDriverBuild) b);
            drawMassPayloadLink((PayloadMassDriver.PayloadDriverBuild) b);
        });

        if(world.build(from.link) instanceof PayloadMassDriver.PayloadDriverBuild to && from != to &&
                to.within(from.x, from.y, ((PayloadMassDriver)from.block).range)){
            float sin = Mathf.absin(Time.time, 6f, 1f);
            Tmp.v1.set(from.x + from.block.offset, from.y + from.block.offset).sub(to.x, to.y).limit(from.block.size * tilesize + sin + 0.5f);
            float x2 = from.x - Tmp.v1.x, y2 = from.y - Tmp.v1.y,
                    x1 = to.x + Tmp.v1.x, y1 = to.y + Tmp.v1.y;
            int segs = (int)(to.dst(from.x, from.y)/tilesize);

            Lines.stroke(4f, Pal.gray);
            Lines.dashLine(x1, y1, x2, y2, segs);
            Lines.stroke(2f, Pal.placing);
            Lines.dashLine(x1, y1, x2, y2, segs);
            Lines.stroke(1f, Pal.accent);
            Drawf.circles(from.x, from.y, (from.tile.block().size / 2f + 1) * tilesize + sin - 2f, Pal.accent);

            for(var shooter : from.waitingShooters){
                Drawf.circles(shooter.x, shooter.y, (from.tile.block().size / 2f + 1) * tilesize + sin - 2f);
                Drawf.arrow(shooter.x, shooter.y, from.x, from.y, from.block.size * tilesize + sin, 4f + sin);
            }
            if(from.link != -1 && world.build(from.link) instanceof PayloadMassDriver.PayloadDriverBuild other && other.block == from.block && other.team == from.team && from.within(other, ((PayloadMassDriver)from.block).range)){
                Building target = world.build(from.link);
                Drawf.circles(target.x, target.y, (target.block().size / 2f + 1) * tilesize + sin - 2f);
                Drawf.arrow(from.x, from.y, target.x, target.y, from.block.size * tilesize + sin, 4f + sin);
            }
            if(world.build(to.link) instanceof PayloadMassDriver.PayloadDriverBuild newTo && to != newTo &&
                    newTo.within(to.x, to.y, ((PayloadMassDriver)to.block).range) && !linkedPayloadMasses.contains(to)){
                linkedPayloadMasses.add(to);
                drawMassPayloadLink(to);
            }
        }
    }

    public void drawMassLink(MassDriver.MassDriverBuild from){
        Groups.build.each(b -> b instanceof MassDriver.MassDriverBuild fromMass &&
                world.build(fromMass.link) == from &&
                from.within(fromMass.x, fromMass.y, ((MassDriver)fromMass.block).range) &&
                !linkedMasses.contains(from), b -> {
            linkedMasses.add((MassDriver.MassDriverBuild) b);
            drawMassLink((MassDriver.MassDriverBuild) b);
        });

        if(world.build(from.link) instanceof MassDriver.MassDriverBuild to && from != to && to.within(from.x, from.y, ((MassDriver)from.block).range)){
            float sin = Mathf.absin(Time.time, 6f, 1f);
            Tmp.v1.set(from.x + from.block.offset, from.y + from.block.offset).sub(to.x, to.y).limit(from.block.size * tilesize + sin + 0.5f);
            float x2 = from.x - Tmp.v1.x, y2 = from.y - Tmp.v1.y,
                    x1 = to.x + Tmp.v1.x, y1 = to.y + Tmp.v1.y;
            int segs = (int)(to.dst(from.x, from.y)/tilesize);

            Lines.stroke(4f, Pal.gray);
            Lines.dashLine(x1, y1, x2, y2, segs);
            Lines.stroke(2f, Pal.placing);
            Lines.dashLine(x1, y1, x2, y2, segs);
            Lines.stroke(1f, Pal.accent);
            Drawf.circles(from.x, from.y, (from.tile.block().size / 2f + 1) * tilesize + sin - 2f, Pal.accent);

            for(var shooter : from.waitingShooters){
                Drawf.circles(shooter.x, shooter.y, (from.tile.block().size / 2f + 1) * tilesize + sin - 2f);
                Drawf.arrow(shooter.x, shooter.y, from.x, from.y, from.block.size * tilesize + sin, 4f + sin);
            }
            if(from.link != -1 && world.build(from.link) instanceof MassDriver.MassDriverBuild other && other.block == from.block && other.team == from.team && from.within(other, ((MassDriver)from.block).range)){
                Building target = world.build(from.link);
                Drawf.circles(target.x, target.y, (target.block().size / 2f + 1) * tilesize + sin - 2f);
                Drawf.arrow(from.x, from.y, target.x, target.y, from.block.size * tilesize + sin, 4f + sin);
            }
            if(world.build(to.link) instanceof MassDriver.MassDriverBuild newTo && to != newTo &&
                    newTo.within(to.x, to.y, ((MassDriver)to.block).range) && !linkedMasses.contains(to)){
                linkedMasses.add(to);
                drawMassLink(to);
            }
        }
    }

    public Seq<Building> getPowerLinkedBuilds(Building build) {
        Seq<Building> linkedBuilds = new Seq<>();
        build.power.links.each(i -> linkedBuilds.add(world.build(i)));
        build.proximity().each(linkedBuilds::add);
        linkedBuilds.filter(b -> b != null && b.power != null);
        if(!build.block.outputsPower && !(build instanceof PowerNode.PowerNodeBuild))
            linkedBuilds.filter(b -> b.block.outputsPower || b instanceof PowerNode.PowerNodeBuild);
        return linkedBuilds;
    }

    public void drawNodeLink(Building node) {
        if(node.power == null) return;
        if(!linkedNodes.contains(node)) {
            linkedNodes.add(node);
        getPowerLinkedBuilds(node).each(other -> {
                float angle1 = Angles.angle(node.x, node.y, other.x, other.y),
                        vx = Mathf.cosDeg(angle1), vy = Mathf.sinDeg(angle1),
                        len1 = node.block.size * tilesize / 2f - 1.5f, len2 = other.block.size * tilesize / 2f - 1.5f;

                Draw.color(Color.white, Color.valueOf("98ff98"), (1f - node.power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
                Draw.alpha(Renderer.laserOpacity);
                Drawf.laser(node.team, atlas.find("unitinfo-Slaser"), atlas.find("unitinfo-Slaser-end"), node.x + vx*len1, node.y + vy*len1, other.x - vx*len2, other.y - vy*len2, 0.25f);

                if(other.power != null) getPowerLinkedBuilds(other).each(this::drawNodeLink);
            });
        }
    }
    public void setEvent(){
        Events.run(EventType.Trigger.draw, () -> {
            if(settings.getBool("deadTarget") && locked && lockedTarget != null && !Groups.all.contains(e -> e == lockedTarget)) {
                lockedTarget = null;
                locked = false;
            }

            if(settings.getBool("linkedMass")){
                if(getTarget() instanceof MassDriver.MassDriverBuild mass) {
                    linkedMasses.clear();
                    drawMassLink(mass);
                }
                else if(getTarget() instanceof PayloadMassDriver.PayloadDriverBuild mass) {
                    linkedPayloadMasses.clear();
                    drawMassPayloadLink(mass);
                }
            }

            if(settings.getBool("linkedNode") && getTarget() instanceof Building node){
                linkedNodes.clear();
                drawNodeLink(node);
            }

            if(settings.getBool("select") && getTarget() != null) {
                Posc entity = getTarget();
                for(int i = 0; i < 4; i++){
                    float rot = i * 90f + 45f + (-Time.time) % 360f;
                    float length = (entity instanceof Unit ? ((Unit)entity).hitSize : entity instanceof Building ? ((Building)entity).block.size * tilesize : 0) * 1.5f + 2.5f;
                    Draw.color(Tmp.c1.set(locked ? Color.orange : Color.darkGray).lerp(locked ? Color.scarlet : Color.gray, Mathf.absin(Time.time, 2f, 1f)).a(settings.getInt("selectopacity") / 100f));
                    Draw.rect("select-arrow", entity.x() + Angles.trnsx(rot, length), entity.y() + Angles.trnsy(rot, length), length / 1.9f, length / 1.9f, rot - 135f);
                }
                if(settings.getBool("distanceLine") && player.unit() != null && !player.unit().dead && getTarget() != player.unit()) { //need selected other unit with living player
                    Teamc from = player.unit();
                    Teamc to = getTarget();
                    float sin = Mathf.absin(Time.time, 6f, 1f);
                    if(player.unit() instanceof BlockUnitUnit bu) Tmp.v1.set(bu.x() + bu.tile().block.offset, bu.y() + bu.tile().block.offset).sub(to.x(), to.y()).limit(bu.tile().block.size * tilesize + sin + 0.5f);
                    else Tmp.v1.set(from.x(), from.y()).sub(to.x(), to.y()).limit(player.unit().hitSize + sin + 0.5f);

                    float x2 = from.x() - Tmp.v1.x, y2 = from.y() - Tmp.v1.y,
                            x1 = to.x() + Tmp.v1.x, y1 = to.y() + Tmp.v1.y;
                    int segs = (int) (to.dst(from.x(), from.y()) / tilesize);

                    Lines.stroke(2.5f, Pal.gray);
                    Lines.dashLine(x1, y1, x2, y2, segs);
                    Lines.stroke(1f, Pal.placing);
                    Lines.dashLine(x1, y1, x2, y2, segs);

                    Fonts.outline.draw(Strings.fixed(to.dst(from.x(), from.y()), 2) + " (" + segs + "tiles)",
                            from.x() + Angles.trnsx(Angles.angle(from.x(), from.y(), to.x(), to.y()), player.unit().hitSize() + 40),
                            from.y() + Angles.trnsy(Angles.angle(from.x(), from.y(), to.x(), to.y()), player.unit().hitSize() + 40) - 3,
                            Pal.accent, 0.25f, false, Align.center);
                }
            }

            Draw.reset();
        });

        Events.run(EventType.Trigger.update, ()->{
            if(Scl.scl(modUiScale) != settings.getInt("infoUiScale") / 100f){
                modUiScale = settings.getInt("infoUiScale") / 100f;
                mainTable.clearChildren();
                addTable();
                coreItems.rebuild();
            }
            if((input.keyDown(KeyCode.shiftRight) || input.keyDown(KeyCode.shiftLeft))){
                if(input.keyTap(KeyCode.r)) lockButton.change();
            }

            if(settings.getBool("autoShooting")) {
                Unit unit = player.unit();
                if (unit.type == null) return;
                boolean omni = unit.type.omniMovement;
                boolean validHealTarget = unit.type.canHeal && target instanceof Building && ((Building) target).isValid() && target.team() == unit.team && ((Building) target).damaged() && target.within(unit, unit.type.range);
                boolean boosted = (unit instanceof Mechc && unit.isFlying());
                if ((unit.type != null && Units.invalidateTarget(target, unit, unit.type.range) && !validHealTarget) || state.isEditor()) {
                    target = null;
                }

                float mouseAngle = unit.angleTo(unit.aimX(), unit.aimY());
                boolean aimCursor = omni && player.shooting && unit.type.hasWeapons() && unit.type.faceTarget && !boosted && unit.type.rotateShooting;
                unit.lookAt(aimCursor ? mouseAngle : unit.prefRotation());

                //update shooting if not building + not mining
                if(!player.unit().activelyBuilding() && player.unit().mineTile == null) {
                    if(input.keyDown(KeyCode.mouseLeft)) {
                        player.shooting = !boosted;
                        unit.aim(player.mouseX = input.mouseWorldX(), player.mouseY = input.mouseWorldY());
                    } else if(target == null) {
                        player.shooting = false;
                        if(unit instanceof BlockUnitUnit b) {
                            if(b.tile() instanceof ControlBlock c && !c.shouldAutoTarget()) {
                                Building build = b.tile();
                                float range = build instanceof Ranged ? ((Ranged) build).range() : 0f;
                                boolean targetGround = build instanceof Turret.TurretBuild && ((Turret) build.block).targetAir;
                                boolean targetAir = build instanceof Turret.TurretBuild && ((Turret) build.block).targetGround;
                                target = Units.closestTarget(build.team, build.x, build.y, range, u -> u.checkTarget(targetAir, targetGround), u -> targetGround);
                            }
                            else target = null;
                        } else if(unit.type != null) {
                            float range = unit.hasWeapons() ? unit.range() : 0f;
                            target = Units.closestTarget(unit.team, unit.x, unit.y, range, u -> u.checkTarget(unit.type.targetAir, unit.type.targetGround), u -> unit.type.targetGround);

                            if(unit.type.canHeal && target == null) {
                                target = Geometry.findClosest(unit.x, unit.y, indexer.getDamaged(Team.sharded));
                                if (target != null && !unit.within(target, range)) {
                                    target = null;
                                }
                            }
                        }
                    } else {
                        player.shooting = !boosted;
                        unit.rotation(Angles.angle(unit.x, unit.y, target.x(), target.y()));
                        unit.aim(target.x(), target.y());
                    }
                }
                unit.controlWeapons(player.shooting && !boosted);
            }
        });

        Events.on(EventType.BlockDestroyEvent.class, e -> coreItems.resetUsed());
        Events.on(EventType.CoreChangeEvent.class, e -> coreItems.resetUsed());
        Events.on(EventType.ResetEvent.class, e -> coreItems.resetUsed());
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
        baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, itemTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
        a = 1f;
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
                            ttt.image(type.uiIcon).size(iconMed);
                        }));
                        add(new Table(ttt -> {
                            ttt.right().bottom();
                            ttt.label(() -> Groups.unit.count(u -> u.type == type && u.team == state.rules.waveTeam && u.isBoss()) + "");
                        }));
                        add(new Table(ttt -> {
                            ttt.top().right();
                            Image image = new Image(Icon.warning.getRegion()).setScaling(Scaling.fit);
                            image.update(() -> image.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f))));
                            ttt.add(image).size(Scl.scl(modUiScale) * 12f);
                            ttt.pack();
                        }));
                    }}).pad(6);
                    if(++i[0] % 6 == 0) tt.row();
                });
            });
            t.row();
            i[0] = 0;
            content.units().each(type -> Groups.unit.contains(u -> u.type == type && u.team == state.rules.waveTeam && !u.isBoss()), type -> {
                t.table(tt -> {
                    tt.add(new Stack() {{
                        add(new Table(ttt -> {
                            ttt.add(new Image(type.uiIcon)).size(iconMed);
                        }));
                        add(new Table(ttt -> {
                            ttt.right().bottom();
                            ttt.add(new Label(() -> Groups.unit.count(u -> u.type == type && u.team == state.rules.waveTeam && !u.isBoss()) + ""));
                        }));
                    }}).pad(6);
                    if(++i[0] % 6 == 0) tt.row();
                });
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
            setTile(t);
            t.row();
            setLeftUnitTable(t);
            t.update(() -> {
                if(enemyamount != Groups.unit.count(u -> u.team == state.rules.waveTeam)) {
                    t.clearChildren();
                    setTile(t);
                    t.row();
                    setLeftUnitTable(t);
                }
            });
        });
        Table waveTable = (Table)((Group)((Group)ui.hudGroup.getChildren().get(5)) //HudFragment#118, name: overlaymarker
                .getChildren().get(mobile ? 2 : 0)) //HudFragment#192, name: wave/editor
                .getChildren().get(0); //HudFragment#196, name: waves
        Table table = (Table)waveTable.getChildren().get(0); //HudFragment#198, name: x
        Table statusTable = (Table) waveTable.getChildren().get(1);
        waveTable.removeChild(statusTable);
        table.row();
        statusTable.top();
        table.stack(waveInfoTable, statusTable);
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
            label.setFontScale(modUiScale);
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
            baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, itemTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
            table.fillParent = true;

            table.visibility = () -> ui.hudfrag.shown && !ui.minimapfrag.shown();
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
                t.top().defaults().width(Scl.scl(modUiScale) * 23 * 8f).height(Scl.scl(modUiScale) * 4f * 8f);
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
                    Element image = new Element();
                    if(getTarget() instanceof ItemTurret.ItemTurretBuild turret){
                        if(turret.hasAmmo()) image = new Image(((ItemTurret)turret.block).ammoTypes.findKey(turret.peekAmmo(), true).uiIcon);
                        else {MultiReqImage itemReq = new MultiReqImage();
                            for(Item item : ((ItemTurret) turret.block).ammoTypes.keys())
                                itemReq.add(new ReqImage(item.uiIcon, turret::hasAmmo));
                            image = itemReq;
                        }
                    }
                    else if(getTarget() instanceof LiquidTurret.LiquidTurretBuild turret){
                        MultiReqImage liquidReq = new MultiReqImage();
                        for(Liquid liquid : ((LiquidTurret) turret.block).ammoTypes.keys())
                            liquidReq.add(new ReqImage(liquid.uiIcon, turret::hasAmmo));
                        image = liquidReq;

                        if(((LiquidTurret.LiquidTurretBuild) getTarget()).hasAmmo())
                            image = new Image(turret.liquids.current().uiIcon).setScaling(Scaling.fit);
                    }
                    else if(getTarget() instanceof PowerTurret.PowerTurretBuild){
                        image = new Image(Icon.power.getRegion()){
                            @Override
                            public void draw(){
                                Building entity = getTarget();
                                float max = entity.block.consumes.getPower().usage;
                                float v = entity.power.status * entity.power.graph.getLastScaledPowerIn();

                                super.draw();
                                Lines.stroke(Scl.scl(modUiScale) * 2f, Pal.removeBack);
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
                    add(image).size(iconSmall * Scl.scl(modUiScale)).padBottom(2 * 8f).padRight(3 * 8f);
                });
                pack();
            }});
            add(new Table(t -> {
                t.left();
                t.add(new Image(){{
                        update(() -> {
                            if(getTarget() instanceof Unit u && u.stack.item != null && u.stack.amount > 0)
                                setDrawable(u.stack.item.uiIcon);
                            else setDrawable(clear);
                        });
                        visibility = () -> getTarget() instanceof Unit;
                    }}.setScaling(Scaling.fit)).size(Scl.scl(modUiScale) * 30f).padBottom(Scl.scl(modUiScale) * 4 * 8f).padRight(Scl.scl(modUiScale) * 6 * 8f);
                t.pack();
            }));
        }});


        {
            int i = 3;
            bars.add(new SBar(
                    () -> strings.get(i),
                    () -> {
                        if(colors.get(i) != Color.clear) lastColors.set(i, colors.get(i));
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
                        if(colors.get(i) != Color.clear) lastColors.set(i, colors.get(i));
                        return lastColors.get(i);
                    },
                    () -> numbers.get(i)
            ));
        }

        bars.add(new Stack(){{
            add(new Table(t -> {
                t.top().defaults().width(Scl.scl(modUiScale) * 23 * 8f).height(Scl.scl(modUiScale) * 4 * 8f);

                int i = 5;
                t.add(new SBar(
                        () -> strings.get(i),
                        () -> {
                            if(colors.get(i) != Color.clear) lastColors.set(i, colors.get(i));
                            return lastColors.get(i);
                        },
                        () -> numbers.get(i)
                )).growX().left();
            }));
            add(new Table(t -> {
                t.left();
                t.add(new Image(){{
                    update(() -> {
                        TextureRegion region = clear;

                        if(Vars.state.rules.unitAmmo && getTarget() instanceof Unit u && u.type != null){
                            UnitType type = u.type;
                            if(type.ammoType instanceof ItemAmmoType ammo) region = ammo.item.uiIcon;
                            else if(type.ammoType instanceof PowerAmmoType) region = Icon.powerSmall.getRegion();
                        }
                        setDrawable(region);
                    });
                }}.setScaling(Scaling.fit)).size(Scl.scl(modUiScale) * 30f).padBottom(Scl.scl(modUiScale) * 4 * 8f).padRight(Scl.scl(modUiScale) * 6 * 8f);
                t.pack();
            }));
        }});
    }

    public void addWeaponTable(Table table){
        table.table().update(t -> {
            t.clear();
            t.add(new Table(((NinePatchDrawable)Tex.button).tint(Tmp.c1.set(((NinePatchDrawable)Tex.button).getPatch().getColor()).a(settings.getInt("uiopacity") / 100f)), tt -> {
                tt.defaults().width(Scl.scl(modUiScale) * 8 * 8f).minHeight(Scl.scl(modUiScale) * 4 * 8f).align(Align.left);
                tt.visibility = () -> settings.getBool("weaponui") && getTarget() instanceof Unit u && u.type != null && u.type.weapons.size > 0;
                if(settings.getBool("weaponui") && getTarget() instanceof Unit u && u.type != null) {
                    UnitType type = u.type;
                    for(int r = 0; r < type.weapons.size; r++){
                        Weapon weapon = type.weapons.get(r);
                        WeaponMount mount = u.mounts[r];
                        TextureRegion region = !weapon.name.equals("") && weapon.outlineRegion.found() ? weapon.outlineRegion : type.uiIcon;
                        int finalR = r;
                        tt.table(ttt -> {
                            if(type.weapons.size > 1 && finalR % 3 == 0) tt.row();
                            else if(finalR % 3 == 0) tt.row();
                            ttt.stack(
                                new Table(o -> {
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
                                            if(getDrawable() != null)
                                                getDrawable().draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
                                        }
                                    }).scaling(Scaling.fill).size(Scl.scl(modUiScale) * iconLarge);
                                })
                                , new Table(h -> {
                                    h.defaults().growX().height(Scl.scl(modUiScale) * 9f).width(Scl.scl(modUiScale) * iconLarge).padTop(Scl.scl(modUiScale) * 18f);
                                    h.add(new SBar(
                                            () -> "",
                                            () -> Pal.accent.cpy().lerp(Color.orange, mount.reload / weapon.reload),
                                            () -> mount.reload / weapon.reload).rect().init());
                                    h.pack();
                                })
                            );
                        });
                    }
                }
            }));
        });
    }

    public Table addInfoTable(Table table){
        return table.table(table1 -> {
            table1.left().top();

            float[] count = new float[]{-1};
            table1.table().update(t -> {
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
                t.left();
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
                        return "[accent]" + (name.length() > 9 ? name.substring(0, 9) + "..." : name) + "[]";
                    });
                    label.setFontScale(modUiScale);

                    TextButton button = Elem.newButton("?", Styles.clearPartialt, () -> {
                        if(getTarget() instanceof Unit u && u.type != null)
                            ui.content.show(u.type);
                        if(getTarget() instanceof Building b && b.block != null) {
                            ui.content.show(b.block);
                        }
                    });
                    button.visibility = () -> getTarget() != null;
                    button.update(()->lockButton.getStyle().imageUp = Icon.lock.tint(locked ? Pal.accent : Color.white));
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
                        if(control.input instanceof DesktopInput)
                            ((DesktopInput) control.input).panning = true;
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
                            label2.setFontScale(modUiScale);
                            tool2.add(label2);
                        });
                        to.row();
                        Label label2 = new Label(()->getTarget() == null ? "(" + 0 + ", " + 0 + ")" : "(" + Strings.fixed(getTarget().x() / tilesize, 2) + ", " + Strings.fixed(getTarget().y() / tilesize, 2) + ")");
                        label2.setFontScale(modUiScale);
                        to.add(label2);
                    })));
                    tt.update(()->tt.setBackground(((NinePatchDrawable)Tex.underline2).tint(getTarget().isNull() ? Color.gray : getTarget().team().color)));
                });
                t.row();
                t.table(tt -> {
                    tt.defaults().height(Scl.scl(modUiScale) * 4f * 8f).pad(0,4,0,4).top();
                    for(Element bar : bars){
                        bar.setScale(Scl.scl(modUiScale));
                        tt.add(bar).growX().left();
                        tt.row();
                    }
                });
                t.setColor(t.color.cpy().a(1f));

                t.background(Tex.button);

                t.update(() -> {
                    NinePatchDrawable patch = (NinePatchDrawable)Tex.button;
                    t.setBackground(patch.tint(Tmp.c1.set(patch.getPatch().getColor()).a(settings.getInt("uiopacity") / 100f)));
                });
            });
            table.table(t -> t.stack(table1, addInfoTable(t)));
            table.row();
            table.table(this::addWeaponTable);

            table.update(() -> {
                try {
                    BarInfo.getInfo(getTarget());
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
                strings = BarInfo.strings;
                numbers = BarInfo.numbers;
                colors = BarInfo.colors;

                if(getTarget() instanceof Turret.TurretBuild tb){
                    if(tb.charging) charge += Time.delta;
                    else charge = 0f;
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
                Label label = new Label(() -> "[#" + (state.wave == j+1 ? Color.red.toString() : Pal.accent.toString()) + "]" + (j+1) + "[]");
                label.setFontScale(modUiScale);
                t.add(label);
            }).size(Scl.scl(modUiScale) * 4 * 8f);

            table.table(Tex.underline, tx -> {
                if(settings.getBool("emptywave") && state.rules.spawns.find(g -> g.getSpawned(j) > 0) == null) {
                    tx.center();
                    tx.add("[lightgray]<Empty>[]");
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
                        tt.right();
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
                                image1.update(() -> image1.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f))));
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
                            tt.update(() -> image.color.lerp(!listener1.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta)));
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
                                    status.update(() -> finalStatus.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f))));
                                }
                                Image finalStatus = status;
                                to.table(tot -> {
                                    tot.left();
                                    tot.add(bundle.get("shar-stat.waveStatus"));
                                    tot.add(finalStatus).size(Scl.scl(modUiScale) * 3 * 8f);
                                    if(!mobile){
                                        HandCursorListener listener = new HandCursorListener();
                                        finalStatus.addListener(listener);
                                        finalStatus.update(() -> finalStatus.color.lerp(!listener.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta)));
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
                    if(++row % 4 == 0) tx.row();
                }
            });
            table.row();
        }
    }

    public void addWaveTable(){
        if(uiIndex != 1) return;
        ScrollPane wavePane = new ScrollPane(new Image(clear).setScaling(Scaling.fit), new ScrollPane.ScrollPaneStyle(){{
            vScroll = Tex.clear;
            vScrollKnob = new ScaledNinePatchDrawable(new NinePatch(((TextureRegionDrawable) scrollKnobVerticalThin).getRegion()), modUiScale);
        }});
        wavePane.setScrollingDisabled(true, false);
        wavePane.setScrollYForce(waveScrollPos);
        wavePane.update(() -> {
            if(wavePane.hasScroll()){
                Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                if(result == null || !result.isDescendantOf(wavePane)){
                    scene.setScrollFocus(null);
                }
            }
            waveScrollPos = wavePane.getScrollY();
            if(waveamount != settings.getInt("wavemax"))
                wavePane.setWidget(new Table(tx -> tx.table(this::setWave).left()));
        });
        wavePane.setOverscroll(false, false);
        wavePane.setWidget(new Table(tx -> tx.table(this::setWave).left()));

        waveTable = new Table(table -> {
            table.left().defaults().width(Scl.scl(modUiScale) * 35 * 8f).maxHeight(Scl.scl(modUiScale) * 32 * 8f).align(Align.left);
            table.add(new Table(Tex.button, t -> {
                t.add(wavePane);
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
                Label label = new Label(() -> "[#" + coreItems.teams[finalI].color.toString() + "]" + coreItems.teams[finalI].name + "[]");
                label.setFontScale(modUiScale);
                t.background(Tex.underline2).add(label).center();
                t.row();
                t.add(coreItems.tables.get(i)).left();
                t.row();
            }
        });
    }

    public void addItemTable(){
        if(uiIndex != 2) return;
        ScrollPane itemPane = new ScrollPane(new Table(this::setItem).left(), new ScrollPane.ScrollPaneStyle(){{
            vScroll = Tex.clear;
            vScrollKnob = new ScaledNinePatchDrawable(new NinePatch(((TextureRegionDrawable) scrollKnobVerticalThin).getRegion()), modUiScale);
        }});
        itemPane.setScrollingDisabled(true, false);
        itemPane.setScrollYForce(itemScrollPos);
        itemPane.setOverscroll(false, false);
        itemPane.update(() -> {
            if(itemPane.hasScroll()){
                Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                if(result == null || !result.isDescendantOf(itemPane)){
                    scene.setScrollFocus(null);
                }
            }
            itemScrollPos = itemPane.getScrollY();
        });

        itemTable = new Table(table -> {
            table.left().defaults().width(Scl.scl(modUiScale) * 54 * 8f).height(Scl.scl(modUiScale) * 32 * 8f).align(Align.left);
            table.table(Tex.button, t -> {
                t.add(itemPane);
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
