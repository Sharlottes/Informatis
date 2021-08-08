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
import mindustry.world.blocks.payloads.PayloadMassDriver;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.storage.*;

import static UnitInfo.SVars.clear;
import static UnitInfo.SVars.modUiScale;
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

        if(world.build(from.link) instanceof MassDriver.MassDriverBuild to && from != to &&
                to.within(from.x, from.y, ((MassDriver)from.block).range)){
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
            if(modUiScale != settings.getInt("infoUiScale") / 100f && settings.getInt("infoUiScale") / 100f != 0){
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

        Events.on(EventType.ResetEvent.class, e -> {
            if(settings.getBool("allTeam")) coreItems.teams = Team.all;
            else coreItems.teams = Team.baseTeams;
            coreItems.resetUsed();
            coreItems.tables.each(Group::clear);
        });
    }

    public void reset(int index, Seq<Button> buttons, Label label, Table table, Table labelTable, String hud){
        uiIndex = index;
        buttons.each(b -> b.setChecked(buttons.indexOf(b) == index));
        label.setText(bundle.get(hud));
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
            label.setStyle(new Label.LabelStyle(){{
                font = Fonts.outline;
                fontColor = Color.white;
                background = Styles.black8;
            }});
            if(modUiScale < 1) label.setFontScale(Scl.scl(modUiScale));
            Table labelTable = new Table(t -> t.add(label).left().padRight(Scl.scl(modUiScale) * 40 * 8f));

            table.table(t -> {
                Seq<Button> buttons = Seq.with(null, null, null, null, null, null);
                Seq<String> strs = Seq.with("hud.unit", "hud.wave", "hud.core", "hud.tile", "hud.item", "hud.cancel");
                Seq<TextureRegionDrawable> icons = Seq.with(Icon.units, Icon.fileText, Icon.commandRally, Icon.grid, Icon.copy, Icon.cancel);
                for(int i = 0; i < buttons.size; i++){
                    int finalI = i;
                    buttons.set(i, t.button(icons.get(i), Styles.clearToggleTransi, () ->
                        reset(finalI, buttons, label, table, labelTable, strs.get(finalI))).size(Scl.scl(modUiScale) * 5 * 8f).get());
                    t.row();
                }
            });
            baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, tileTable, itemTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
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
                                Lines.stroke(2f, Pal.removeBack);
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

    public void addWeapon(){
        weapon = new Table(tx -> {
            tx.left().defaults().minSize(Scl.scl(modUiScale) * 12 * 8f);

            tx.add(new Table(Tex.button, tt -> {
                tt.left().top().defaults().width(Scl.scl(modUiScale) * 8 * 8f).minHeight(Scl.scl(modUiScale) * 4 * 8f);

                if(getTarget() instanceof Unit u && u.type != null) {
                    UnitType type = u.type;
                    for(int r = 0; r < type.weapons.size; r++){
                        Weapon weapon = type.weapons.get(r);
                        WeaponMount mount = u.mounts[r];
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
                                            if(getDrawable() != null)
                                                getDrawable().draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
                                        }
                                    }.setScaling(Scaling.fit)).size(Scl.scl(modUiScale) * 6 * 8f);
                                }));

                                add(new Table(h -> {
                                    h.defaults().growX().height(Scl.scl(modUiScale) * 9f).width(Scl.scl(modUiScale) * 31.5f).padTop(Scl.scl(modUiScale) * 18f);
                                    h.add(new Bar(
                                        () -> "",
                                        () -> Pal.accent.cpy().lerp(Color.orange, mount.reload / weapon.reload),
                                        () -> mount.reload / weapon.reload)).padLeft(Scl.scl(modUiScale) * 8f);
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
            }).padRight(Scl.scl(modUiScale) * 24 * 8f);
            tx.setColor(tx.color.cpy().a(1f));
        });
    }

    public void addUnitTable(){
        if(uiIndex != 0) return;
        unitTable = new Table(table -> {
            table.left();
            addBars();
            table.add(new Table(Tex.button, t -> {
                t.defaults().width(Scl.scl(modUiScale) * 25 * 8f);

                t.table(Tex.underline2, tt -> {
                    Stack stack = new Stack(){{
                        add(new Table(ttt -> ttt.add(new Image(){{
                            update(() -> {
                                TextureRegion region = clear;
                                if(getTarget() instanceof Unit u && u.type != null) region = ((Unit) getTarget()).type().uiIcon;
                                else if(getTarget() instanceof Building b && b.block != null) {
                                    if(getTarget() instanceof ConstructBlock.ConstructBuild cb) region = cb.current.uiIcon;
                                    else region = b.block.uiIcon;
                                }
                                setDrawable(region);
                            });
                        }}.setScaling(Scaling.fit)).size(Scl.scl(modUiScale) * 4f * 8f)));
                        add(new Table(ttt -> {
                            ttt.add(new Stack(){{
                                add(new Table(temp -> temp.add(new Image(){{
                                    update(()-> setDrawable(getTarget() instanceof Unit ? Icon.defenseSmall.getRegion() : clear));
                                }}.setScaling(Scaling.fit))));

                                add(new Table(temp -> {
                                    if(getTarget() instanceof Unit) {
                                        Label label = new Label(() -> (getTarget() instanceof Unit u && u.type != null ? (int) u.type.armor + "" : ""));
                                        label.setColor(Pal.surge);
                                        label.setFontScale(Scl.scl(modUiScale) * 0.5f);
                                        temp.add(label).center();
                                    }
                                    temp.pack();
                                }));
                            }}).padLeft(Scl.scl(modUiScale) * 2 * 8f).padBottom(Scl.scl(modUiScale) * 2 * 8f);
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
                    if(modUiScale < 1) label.setFontScale(Scl.scl(modUiScale));

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
                    tt.add(button).size(Scl.scl(modUiScale) * 5 * 8f);
                    tt.add(lockButton).size(Scl.scl(modUiScale) * 3 * 8f);

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
                                    if(u.getPlayer() != null) return u.getPlayer().name;
                                    else if(u.type != null) return u.type.localizedName;
                                }
                                else if(getTarget() instanceof Building b) return b.block.localizedName;
                                return "";
                            });

                            if(modUiScale < 1) label2.setFontScale(Scl.scl(modUiScale));
                            tool2.add(label2);

                        });
                        to.row();
                        Label label2 = new Label(()->getTarget() == null ? "(" + 0 + ", " + 0 + ")" : "(" + Strings.fixed(getTarget().x() / tilesize, 2) + ", " + Strings.fixed(getTarget().y() / tilesize, 2) + ")");
                        if(modUiScale < 1) label2.setFontScale(Scl.scl(modUiScale));
                        to.add(label2);
                    })));
                });
                t.row();
                t.table(tt -> {
                    tt.defaults().width(Scl.scl(modUiScale) * 23 * 8f).height(Scl.scl(modUiScale) * 4f * 8f).top();
                    for(Element bar : bars){
                        bar.setScale(Scl.scl(modUiScale));
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
            }).padRight(Scl.scl(modUiScale) * 24 * 8f);
            table.row();
            table.update(() -> {
                try {
                    BarInfo.getInfo(getTarget());
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
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
                if(modUiScale < 1) label.setFontScale(Scl.scl(modUiScale));

                t.add(label);
            }).size(Scl.scl(modUiScale) * 4 * 8f);

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
                                ttt.add(image).size(iconLarge * Scl.scl(modUiScale < 1 ? modUiScale : 1));
                                ttt.pack();
                            }));

                            add(new Table(ttt -> {
                                ttt.bottom().left();
                                Label label = new Label(() -> amount + "");
                                if(modUiScale < 1) label.setFontScale(Scl.scl(modUiScale));
                                ttt.add(label);
                                ttt.pack();
                            }));

                            add(new Table(ttt -> {
                                ttt.top().right();
                                Image image = new Image(Icon.warning.getRegion()).setScaling(Scaling.fit);
                                image.update(() -> image.setColor(Tmp.c2.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f))));
                                ttt.add(image).size(Scl.scl(modUiScale < 1 ? modUiScale : 1) * 12f);
                                ttt.visible(() -> group.effect == StatusEffects.boss);
                                ttt.pack();
                            }));
                        }}).pad(2f * Scl.scl(modUiScale));
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
                    if(row % 4 == 0) tx.row();
                }
            });
            table.row();
        }
    }

    public void addWaveTable(){
        if(uiIndex != 1) return;
        ScrollPane wavePane = new ScrollPane(new Image(clear).setScaling(Scaling.fit), Styles.smallPane);
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
            table.left();
            table.defaults().width(Scl.scl(modUiScale) * 32 * 8f).maxHeight(Scl.scl(modUiScale) * 32 * 8f).align(Align.left);
            table.add(new Table(Tex.button, t -> t.add(wavePane)){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                    Draw.reset();
                }
            }).padRight(Scl.scl(modUiScale) * 39 * 8f);

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
                    head.center();
                    Label label = new Label(() -> "[#" + coreItems.teams[finalI].color.toString() + "]" + coreItems.teams[finalI].name + "[]");
                    if(modUiScale < 1) label.setFontScale(Scl.scl(modUiScale));
                    head.add(label);
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
                                image.addListener(new Tooltip(t -> {

                                    Label label = new Label(() -> "([#" + Tmp.c1.set(Color.green).lerp(Color.red, 1 - core.healthf()).toString() + "]" + Strings.fixed(core.health, 2) + "[]/" + Strings.fixed(core.block.health, 2) + ")");
                                    if(modUiScale < 1) label.setFontScale(Scl.scl(modUiScale));
                                    t.background(Tex.button).add(label);
                                }));
                                s.add(image).size(iconLarge * Scl.scl(modUiScale < 1 ? modUiScale : 1));
                            }));

                            add(new Table(s -> {
                                s.bottom().defaults().growX().height(Scl.scl(modUiScale) * 9f).pad(4 * Scl.scl(modUiScale));
                                s.add(new Bar(() -> "", () -> Pal.health, core::healthf));
                                s.pack();
                            }));
                        }});
                        tt.row();
                        Label label = new Label(() -> "(" + (int) core.x / 8 + ", " + (int) core.y / 8 + ")");
                        if(modUiScale < 1) label.setFontScale(Scl.scl(modUiScale));
                        tt.add(label);
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
                Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                if(result == null || !result.isDescendantOf(corePane)){
                    scene.setScrollFocus(null);
                }
            }
            coreScrollPos = corePane.getScrollY();
        });
        corePane.setWidget(new Table(tx -> tx.table(this::setCore).left()));
        corePane.setOverscroll(false, false);

        coreTable = new Table(table -> {
            table.left();
            table.defaults().width(Scl.scl(modUiScale) * 50 * 8f).height(Scl.scl(modUiScale) * 32 * 8f).align(Align.left);
            table.add(new Table(Tex.button, t -> t.add(corePane)){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                    Draw.reset();
                }
            }).padRight(Scl.scl(modUiScale) * 39 * 8f);

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
                    if(tile.floor().uiIcon != atlas.find("error")) image.image(tile.floor().uiIcon);
                    if(tile.overlay().uiIcon != atlas.find("error")) image.image(tile.overlay().uiIcon);
                    if(tile.block().uiIcon != atlas.find("error")) image.image(tile.block().uiIcon);
                });
                head.row();
                Label label = new Label(() -> tile == null ? "(null, null)" : "(" + tile.x + ", " + tile.y + ")");
                if(modUiScale < 1) label.setFontScale(Scl.scl(modUiScale));
                head.add(label).center();
            });
        });
    }

    public void addTileTable(){
        if(uiIndex != 3) return;
        tileTable = new Table(table -> {
            table.left();
            table.defaults().minWidth(Scl.scl(modUiScale) * 32 * 8f).minHeight(Scl.scl(modUiScale) * 20 * 8f).align(Align.left);
            table.add(new Table(Tex.button, t->{
                t.update(()->{
                    t.clearChildren();
                    setTile(t);
                });
            }){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                    Draw.reset();
                }
            }).padRight(Scl.scl(modUiScale) * 39 * 8f);

            table.fillParent = true;
            table.visibility = () -> uiIndex == 3;
        });
    }

    public void setItem(Table table){
        table.table(t -> {
            for(int i = 0; i < coreItems.tables.size; i++){
                if((state.rules.pvp && coreItems.teams[i] != player.team()) || coreItems.teams[i].cores().isEmpty()) continue;
                int finalI = i;
                Label label = new Label(() -> "[#" + coreItems.teams[finalI].color.toString() + "]" + coreItems.teams[finalI].name + "[]");
                if(modUiScale < 1) label.setFontScale(Scl.scl(modUiScale));
                t.background(Tex.underline2).add(label).center();
                t.row();
                t.add(coreItems.tables.get(finalI)).left();
                t.row();
            }
        });
    }

    public void addItemTable(){
        if(uiIndex != 4) return;
        ScrollPane itemPane = new ScrollPane(new Image(clear).setScaling(Scaling.fit), Styles.smallPane);
        itemPane.setScrollingDisabled(true, false);
        itemPane.setScrollYForce(tileScrollPos);
        itemPane.update(() -> {
            if(itemPane.hasScroll()){
                Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                if(result == null || !result.isDescendantOf(itemPane)){
                    scene.setScrollFocus(null);
                }
            }
            itemScrollPos = itemPane.getScrollY();
        });
        itemPane.setWidget(new Table(this::setItem).left());
        itemPane.setOverscroll(false, false);

        itemTable = new Table(table -> {
            table.left();
            table.defaults().width(Scl.scl(modUiScale) * 50 * 8f).height(Scl.scl(modUiScale) * 32 * 8f).align(Align.left);
            table.add(new Table(Tex.button, t -> t.add(itemPane)){
                @Override
                protected void drawBackground(float x, float y) {
                    if(getBackground() == null) return;
                    Draw.color(color.r, color.g, color.b, (settings.getInt("uiopacity") / 100f) * this.parentAlpha);
                    getBackground().draw(x, y, width, height);
                    Draw.reset();
                }
            }).padRight(Scl.scl(modUiScale) * 39 * 8f);

            table.fillParent = true;
            table.visibility = () -> uiIndex == 4;
        });
    }
}
