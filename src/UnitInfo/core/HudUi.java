package UnitInfo.core;

import UnitInfo.ui.*;
import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.*;
import arc.scene.*;
import arc.scene.event.HandCursorListener;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
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
    float waveScrollPos;
    float coreScrollPos;
    float tileScrollPos;


    @Nullable UnitType type;
    Element image;
    Color lastItemColor = Pal.items;
    float heat;
    float heat2;
    float a;
    int uiIndex = 0;

    //to update wave, core table
    int maxwave;
    int coreamount;

    @SuppressWarnings("unchecked")
    public <T extends Teamc> T getUnit(){
        Seq<Unit> units = Groups.unit.intersect(Core.input.mouseWorldX(), Core.input.mouseWorldY(), 4, 4);
        if(units.size > 0) return (T) units.peek();
        if(getTile() != null && getTile().build != null) return (T) getTile().build;
        else return (T) player.unit();
    }

    public @Nullable Tile getTile(){
        return Vars.world.tileWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());
    }

    public void setEvent(){
        Events.run(EventType.Trigger.draw, () -> {
            if(getUnit() == null || !Core.settings.getBool("select")) return;

            Posc entity = getUnit();
            for(int i = 0; i < 4; i++){
                float rot = i * 90f + 45f + (-Time.time) % 360f;
                float length = (entity instanceof Unit ? ((Unit)entity).hitSize : entity instanceof Building ? ((Building)entity).block.size * tilesize : 0) * 1.5f + 2.5f;
                Draw.color(Tmp.c1.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f)).a(settings.getInt("selectopacity") / 100f));
                Draw.rect("select-arrow", entity.x() + Angles.trnsx(rot, length), entity.y() + Angles.trnsy(rot, length), length / 1.9f, length / 1.9f, rot - 135f);
                Draw.reset();
            }
        });
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
                Seq<Button> buttons = Seq.with(null, null, null, null, null);
                buttons.items[0] = t.button(Icon.units, Styles.clearToggleTransi, () -> {
                    uiIndex = 0;
                    buttons.items[0].setChecked(true);
                    buttons.items[1].setChecked(false);
                    buttons.items[2].setChecked(false);
                    buttons.items[3].setChecked(false);
                    buttons.items[4].setChecked(false);
                    label.setText(Core.bundle.get("hud.unit"));
                    addBars();
                    addWeapon();
                    addUnitTable();
                    addWaveTable();
                    addCoreTable();
                    addTileTable();
                    table.removeChild(baseTable);
                    labelTable.setPosition(buttons.items[uiIndex].x, buttons.items[uiIndex].y);
                    baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, tileTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
                    a = 1f;
                }).size(5*8f).get();
                t.row();
                buttons.items[1] = t.button(Icon.fileText, Styles.clearToggleTransi, () -> {
                    uiIndex = 1;
                    buttons.items[0].setChecked(false);
                    buttons.items[1].setChecked(true);
                    buttons.items[2].setChecked(false);
                    buttons.items[3].setChecked(false);
                    buttons.items[4].setChecked(false);
                    label.setText(Core.bundle.get("hud.wave"));
                    addBars();
                    addWeapon();
                    addUnitTable();
                    addWaveTable();
                    addCoreTable();
                    addTileTable();
                    table.removeChild(baseTable);
                    labelTable.setPosition(buttons.items[uiIndex].x, buttons.items[uiIndex].y);
                    baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, tileTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
                    a = 1f;
                }).size(5*8f).get();
                t.row();
                buttons.items[2] = t.button(Icon.commandRally, Styles.clearToggleTransi, () -> {
                    uiIndex = 2;
                    buttons.items[0].setChecked(false);
                    buttons.items[1].setChecked(false);
                    buttons.items[2].setChecked(true);
                    buttons.items[3].setChecked(false);
                    buttons.items[4].setChecked(false);
                    label.setText(Core.bundle.get("hud.core"));
                    addBars();
                    addWeapon();
                    addUnitTable();
                    addWaveTable();
                    addCoreTable();
                    addTileTable();
                    table.removeChild(baseTable);
                    labelTable.setPosition(buttons.items[uiIndex].x, buttons.items[uiIndex].y);
                    baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, tileTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
                    a = 1f;
                }).size(5*8f).get();
                t.row();
                buttons.items[3] = t.button(Icon.grid, Styles.clearToggleTransi, () -> {
                    uiIndex = 3;
                    buttons.items[0].setChecked(false);
                    buttons.items[1].setChecked(false);
                    buttons.items[2].setChecked(false);
                    buttons.items[3].setChecked(true);
                    buttons.items[4].setChecked(false);
                    label.setText(Core.bundle.get("hud.tile"));
                    addBars();
                    addWeapon();
                    addUnitTable();
                    addWaveTable();
                    addCoreTable();
                    addTileTable();
                    table.removeChild(baseTable);
                    labelTable.setPosition(buttons.items[uiIndex].x, buttons.items[uiIndex].y);
                    baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, tileTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
                    a = 1f;
                }).size(5*8f).get();
                t.row();
                buttons.items[4] = t.button(Icon.cancel, Styles.clearToggleTransi, () -> {
                    uiIndex = 4;
                    buttons.items[0].setChecked(false);
                    buttons.items[1].setChecked(false);
                    buttons.items[2].setChecked(false);
                    buttons.items[3].setChecked(false);
                    buttons.items[4].setChecked(true);
                    label.setText(Core.bundle.get("hud.cancel"));
                    addBars();
                    addWeapon();
                    addUnitTable();
                    addWaveTable();
                    addCoreTable();
                    addTileTable();
                    table.removeChild(baseTable);
                    labelTable.setPosition(buttons.items[uiIndex].x, buttons.items[uiIndex].y);
                    baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, tileTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
                    a = 1f;
                }).size(5*8f).get();
            });
            baseTable = table.table(tt -> tt.stack(unitTable, coreTable, waveTable, labelTable).align(Align.left).left().visible(() -> settings.getBool("infoui"))).get();
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
        bars.add(new SBar(
            () -> {
                float hp = 0f;
                if(getUnit() instanceof Healthc) hp = Mathf.round(((Healthc)getUnit()).health(), 1);
                return Core.bundle.format("shar-stat.health", hp);
            },
            () -> Pal.health,
            () -> {
                float hp = 0f;

                if(getUnit() instanceof Healthc) hp = ((Healthc)getUnit()).healthf();
                return Mathf.clamp(hp);
            }
        ));
        bars.add(new SBar(
            () -> {
                if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild) {
                    float value = Mathf.clamp(((Turret.TurretBuild)((BlockUnitUnit)getUnit()).tile()).reload / ((Turret)((BlockUnitUnit)getUnit()).tile().block).reloadTime) * 100f;
                    return Core.bundle.format("shar-stat.reload", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2)));
                }
                if(getUnit() instanceof Turret.TurretBuild){
                    float value = Mathf.clamp(((Turret.TurretBuild)getUnit()).reload / ((Turret)((Turret.TurretBuild)getUnit()).block).reloadTime) * 100f;
                    return Core.bundle.format("shar-stat.reload", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2)));
                }
                if(getUnit() instanceof Shieldc)
                    return Core.bundle.format("shar-stat.shield", Strings.fixed(((Shieldc)getUnit()).shield(),1));
                return "[lightgray]<Empty>[]";
            },
            () ->{
                if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild)
                    return Pal.accent.cpy().lerp(Color.orange, Mathf.clamp(((Turret.TurretBuild)((BlockUnitUnit)getUnit()).tile()).reload / ((Turret)((BlockUnitUnit)getUnit()).tile().block).reloadTime));
                if(getUnit() instanceof Turret.TurretBuild)
                    return Pal.accent.cpy().lerp(Color.orange, Mathf.clamp(((Turret.TurretBuild)getUnit()).reload / ((Turret)((Turret.TurretBuild)getUnit()).block).reloadTime));
                if(getUnit() instanceof Shieldc)
                    return Pal.surge;
                return Color.clear;
            },
            () -> {
                if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild)
                    return Mathf.clamp(((Turret.TurretBuild)((BlockUnitUnit)getUnit()).tile()).reload / ((Turret)((BlockUnitUnit)getUnit()).tile().block).reloadTime);
                if(getUnit() instanceof Turret.TurretBuild)
                    return Mathf.clamp(((Turret.TurretBuild)getUnit()).reload / ((Turret)((Turret.TurretBuild)getUnit()).block).reloadTime);
                if(getUnit() instanceof Shieldc) {
                    float max1 = ((ShieldRegenFieldAbility)content.units().copy().filter(ut -> ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility) != null).sort(ut -> ((ShieldRegenFieldAbility)ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max).peek().abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max;
                    float max2 = 0f;
                    if(!(getUnit() instanceof Unit)) return 0f;
                    if(((Unit)getUnit()).type().abilities.find(abil -> abil instanceof ForceFieldAbility) != null) max2 = ((ForceFieldAbility) ((Unit)getUnit()).type().abilities.find(abil -> abil instanceof ForceFieldAbility)).max;
                    return Mathf.clamp(((Unit)getUnit()).shield() / Math.max(max1, max2));
                }
                return 0f;
            }
        ));
        bars.add(new Stack(){{
            add(new Table(t -> {
                t.top().defaults().width(Scl.scl(23 * 8f)).height(Scl.scl(4f * 8f));
                t.add(new SBar(
                    () -> {
                        if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild){
                            if(((BlockUnitUnit)getUnit()).tile() instanceof ItemTurret.ItemTurretBuild)
                                return bundle.format("shar-stat.itemAmmo", ((ItemTurret.ItemTurretBuild) ((BlockUnitUnit)getUnit()).tile()).totalAmmo, ((ItemTurret)((BlockUnitUnit)getUnit()).tile().block).maxAmmo);
                            else if(((BlockUnitUnit)getUnit()).tile() instanceof LiquidTurret.LiquidTurretBuild){
                                LiquidTurret.LiquidTurretBuild entity = ((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit)getUnit()).tile());
                                return bundle.format("shar-stat.liquidAmmo", entity == null || entity.liquids == null ? 0 : Mathf.round(entity.liquids.get(entity.liquids.current()) * 10) / 10.0 + " / " + Mathf.round(entity.block.liquidCapacity));
                            }
                            else if(((BlockUnitUnit)getUnit()).tile() instanceof PowerTurret.PowerTurretBuild){
                                PowerTurret.PowerTurretBuild entity = ((PowerTurret.PowerTurretBuild)((BlockUnitUnit)getUnit()).tile());
                                float max = entity.block.consumes.getPower().usage;
                                float v = entity.power.status * entity.power.graph.getLastScaledPowerIn();

                                return bundle.format("shar-stat.power", (int)(Math.min(v,max) * 60), (int)(max * 60));
                            }
                        }
                        if(getUnit() instanceof Turret.TurretBuild){
                            if(getUnit() instanceof ItemTurret.ItemTurretBuild)
                                return bundle.format("shar-stat.itemAmmo", ((ItemTurret.ItemTurretBuild) getUnit()).totalAmmo, ((ItemTurret)((ItemTurret.ItemTurretBuild) getUnit()).block).maxAmmo);
                            else if(getUnit() instanceof LiquidTurret.LiquidTurretBuild){
                                LiquidTurret.LiquidTurretBuild entity = getUnit();
                                return bundle.format("shar-stat.liquidAmmo", entity == null || entity.liquids == null ? 0 : Mathf.round(entity.liquids.get(entity.liquids.current()) * 10) / 10.0 + " / " + Mathf.round(entity.block.liquidCapacity));
                            }
                            else if(getUnit() instanceof PowerTurret.PowerTurretBuild){
                                PowerTurret.PowerTurretBuild entity = getUnit();
                                float max = entity.block.consumes.getPower().usage;
                                float v = entity.power.status * entity.power.graph.getLastScaledPowerIn();
                                return bundle.format("shar-stat.power", (int)(Math.min(v,max) * 60), (int)(max * 60));
                            }
                        }
                        if(getUnit() instanceof Unit)
                            return bundle.format("shar-stat.itemCapacity", ((Unit)getUnit()).stack().amount, ((Unit)getUnit()).type().itemCapacity);
                        return "[lightgray]<Empty>[]";
                    },
                    () -> {
                        if(getUnit() instanceof BlockUnitUnit){
                            if(((BlockUnitUnit)getUnit()).tile() instanceof ItemTurret.ItemTurretBuild) {
                                if(((ItemTurret.ItemTurretBuild)((BlockUnitUnit) getUnit()).tile()).hasAmmo()) lastItemColor = ((ItemTurret) ((BlockUnitUnit) getUnit()).tile().block).ammoTypes.findKey(((ItemTurret.ItemTurretBuild)((BlockUnitUnit) getUnit()).tile()).peekAmmo(), true).color;
                                else lastItemColor = Pal.ammo;
                            }
                            else if(((BlockUnitUnit)getUnit()).tile() instanceof LiquidTurret.LiquidTurretBuild){
                                LiquidTurret.LiquidTurretBuild entity = ((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit)getUnit()).tile());
                                lastItemColor = entity.liquids.current().color;
                            }
                            else if(((BlockUnitUnit)getUnit()).tile() instanceof PowerTurret.PowerTurretBuild){
                                lastItemColor = Pal.powerBar;
                            }
                        }
                        else if(getUnit() instanceof Turret.TurretBuild){
                            if(getUnit() instanceof ItemTurret.ItemTurretBuild) {
                                if(((ItemTurret.ItemTurretBuild)getUnit()).hasAmmo()) lastItemColor = ((ItemTurret) ((ItemTurret.ItemTurretBuild)getUnit()).block).ammoTypes.findKey(((ItemTurret.ItemTurretBuild)getUnit()).peekAmmo(), true).color;
                                else lastItemColor = Pal.ammo;
                            }
                            else if(getUnit() instanceof LiquidTurret.LiquidTurretBuild){
                                lastItemColor = ((LiquidTurret.LiquidTurretBuild)getUnit()).liquids.current().color;
                            }
                            else if(getUnit() instanceof PowerTurret.PowerTurretBuild){
                                lastItemColor = Pal.powerBar;
                            }
                        }
                        else if(getUnit() instanceof Unit && ((Unit)getUnit()).stack().item != null && ((Unit)getUnit()).stack().amount > 0)
                            lastItemColor = ((Unit)getUnit()).stack().item.color.cpy().lerp(Color.white, 0.15f);
                        else lastItemColor = Color.clear;
                        return lastItemColor;
                    },
                    () -> {
                        if(getUnit() instanceof BlockUnitUnit) {
                            if(((BlockUnitUnit)getUnit()).tile() instanceof ItemTurret.ItemTurretBuild) {
                                return ((ItemTurret.ItemTurretBuild) ((BlockUnitUnit) getUnit()).tile()).totalAmmo / (((ItemTurret) ((BlockUnitUnit) getUnit()).tile().block).maxAmmo * 1f);
                            }
                            else if(((BlockUnitUnit)getUnit()).tile() instanceof LiquidTurret.LiquidTurretBuild){
                                LiquidTurret.LiquidTurretBuild entity = ((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit)getUnit()).tile());
                                return entity.liquids.get(entity.liquids.current()) / entity.block.liquidCapacity;
                            }
                            else if(((BlockUnitUnit)getUnit()).tile() instanceof PowerTurret.PowerTurretBuild){
                                Building entity = ((BlockUnitUnit)getUnit()).tile();
                                float max = entity.block.consumes.getPower().usage;
                                float v = entity.power.status * entity.power.graph.getLastScaledPowerIn();
                                return v/max;
                            }
                        }
                        if(getUnit() instanceof Turret.TurretBuild) {
                            if(getUnit() instanceof ItemTurret.ItemTurretBuild) {
                                return (((ItemTurret.ItemTurretBuild) getUnit()).totalAmmo / (((ItemTurret) ((ItemTurret.ItemTurretBuild)getUnit()).block).maxAmmo * 1f));
                            }
                            else if(getUnit() instanceof LiquidTurret.LiquidTurretBuild){
                                LiquidTurret.LiquidTurretBuild entity = getUnit();
                                return entity.liquids.get(entity.liquids.current()) / entity.block.liquidCapacity;
                            }
                            else if(getUnit() instanceof PowerTurret.PowerTurretBuild){
                                Building entity = getUnit();
                                float max = entity.block.consumes.getPower().usage;
                                float v = entity.power.status * entity.power.graph.getLastScaledPowerIn();
                                return v/max;
                            }
                        }
                        if(getUnit() instanceof Unit)
                            return Mathf.clamp(((Unit)getUnit()).stack().amount / (((Unit)getUnit()).type().itemCapacity * 1f));
                        return 0f;
                    }
                )).growX().left();
            }));
            add(new Table(){{
                left();
                update(() -> {
                    if((!(getUnit() instanceof BlockUnitUnit) || (
                        !(((BlockUnitUnit)getUnit()).tile() instanceof ItemTurret.ItemTurretBuild)
                        && !(((BlockUnitUnit)getUnit()).tile() instanceof LiquidTurret.LiquidTurretBuild)
                        && !(((BlockUnitUnit)getUnit()).tile() instanceof PowerTurret.PowerTurretBuild)))
                    || (!(getUnit() instanceof Turret.TurretBuild) || (
                        !(getUnit() instanceof ItemTurret.ItemTurretBuild)
                            && !(getUnit() instanceof LiquidTurret.LiquidTurretBuild)
                            && !(getUnit() instanceof PowerTurret.PowerTurretBuild)))){
                        clearChildren();
                        image = null;
                        return;
                    }

                    if(getUnit() instanceof BlockUnitUnit){
                        Element imaget = new Element();
                        if(((BlockUnitUnit)getUnit()).tile() instanceof ItemTurret.ItemTurretBuild){
                            MultiReqImage itemReq = new MultiReqImage();
                            for(Item item : ((ItemTurret) ((BlockUnitUnit) getUnit()).tile().block).ammoTypes.keys())
                                itemReq.add(new ReqImage(item.uiIcon, () -> ((ItemTurret.ItemTurretBuild)((BlockUnitUnit) getUnit()).tile()).hasAmmo()));
                            imaget = itemReq;

                            if(((ItemTurret.ItemTurretBuild)((BlockUnitUnit) getUnit()).tile()).hasAmmo())
                                imaget = new Image(((ItemTurret) ((BlockUnitUnit) getUnit()).tile().block).ammoTypes.findKey(((ItemTurret.ItemTurretBuild)((BlockUnitUnit) getUnit()).tile()).peekAmmo(), true).uiIcon).setScaling(Scaling.fit);

                        }
                        else if(((BlockUnitUnit)getUnit()).tile() instanceof LiquidTurret.LiquidTurretBuild){
                            LiquidTurret.LiquidTurretBuild entity = ((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit)getUnit()).tile());
                            Func<Building, Liquid> current;
                            current = entity1 -> entity1.liquids == null ? Liquids.water : entity1.liquids.current();

                            MultiReqImage liquidReq = new MultiReqImage();
                            for(Liquid liquid : ((LiquidTurret) ((BlockUnitUnit) getUnit()).tile().block).ammoTypes.keys())
                                liquidReq.add(new ReqImage(liquid.uiIcon, () -> ((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit) getUnit()).tile()).hasAmmo()));
                            imaget = liquidReq;

                            if(((LiquidTurret.LiquidTurretBuild)((BlockUnitUnit) getUnit()).tile()).hasAmmo())
                                imaget = new Image(current.get(entity).uiIcon).setScaling(Scaling.fit);
                        }
                        else if(((BlockUnitUnit)getUnit()).tile() instanceof PowerTurret.PowerTurretBuild){
                            imaget = new ReqImage(Icon.power.getRegion(), () -> ((BlockUnitUnit)getUnit()).tile().power.status * ((BlockUnitUnit)getUnit()).tile().power.graph.getLastScaledPowerIn() > 0f){
                                {
                                    {
                                    add(new Image(Icon.power.getRegion()));
                                    add(new Element(){
                                        @Override
                                        public void draw(){
                                            Building entity = ((BlockUnitUnit)getUnit()).tile();
                                            float max = entity.block.consumes.getPower().usage;
                                            float v = entity.power.status * entity.power.graph.getLastScaledPowerIn();

                                            Lines.stroke(Scl.scl(2f), Pal.removeBack);
                                            Draw.alpha(1 - v/max);
                                            Lines.line(x, y - 2f + height, x + width, y - 2f);
                                            Draw.color(Pal.remove);
                                            Draw.alpha(1 - v/max);
                                            Lines.line(x, y + height, x + width, y);
                                            Draw.reset();
                                        }
                                    });
                                }}
                            };
                        }

                        if(image != null){
                            if(imaget.getClass() != image.getClass() || imaget.getClass() == Image.class){
                                clearChildren();
                                add(imaget).size(iconSmall).padBottom(2 * 8f).padRight(3 * 8f);
                                image = imaget;
                            }
                        }
                        else {
                            add(imaget).size(iconSmall).padBottom(2 * 8f).padRight(3 * 8f);
                            image = imaget;
                        }
                    }
                    if(getUnit() instanceof Turret.TurretBuild){
                        Element imaget = new Element();
                        if(getUnit() instanceof ItemTurret.ItemTurretBuild){
                            MultiReqImage itemReq = new MultiReqImage();
                            for(Item item : ((ItemTurret) ((ItemTurret.ItemTurretBuild) getUnit()).block).ammoTypes.keys())
                                itemReq.add(new ReqImage(item.uiIcon, () -> ((ItemTurret.ItemTurretBuild)getUnit()).hasAmmo()));
                            imaget = itemReq;

                            if(((ItemTurret.ItemTurretBuild)((BlockUnitUnit) getUnit()).tile()).hasAmmo())
                                imaget = new Image(((ItemTurret) ((ItemTurret.ItemTurretBuild) getUnit()).block).ammoTypes.findKey(((ItemTurret.ItemTurretBuild) getUnit()).peekAmmo(), true).uiIcon).setScaling(Scaling.fit);
                        }
                        else if(getUnit() instanceof LiquidTurret.LiquidTurretBuild){
                            LiquidTurret.LiquidTurretBuild entity = getUnit();
                            MultiReqImage liquidReq = new MultiReqImage();
                            for(Liquid liquid : ((LiquidTurret) ((LiquidTurret.LiquidTurretBuild) getUnit()).block).ammoTypes.keys())
                                liquidReq.add(new ReqImage(liquid.uiIcon, () -> ((LiquidTurret.LiquidTurretBuild) getUnit()).hasAmmo()));
                            imaget = liquidReq;

                            if(((LiquidTurret.LiquidTurretBuild) getUnit()).hasAmmo())
                                imaget = new Image(entity.liquids.current().uiIcon).setScaling(Scaling.fit);
                        }
                        else if(getUnit() instanceof PowerTurret.PowerTurretBuild){
                            imaget = new ReqImage(Icon.power.getRegion(), () -> ((PowerTurret.PowerTurretBuild)getUnit()).power.status * ((PowerTurret.PowerTurretBuild)getUnit()).power.graph.getLastScaledPowerIn() > 0f){{
                                add(new Image(Icon.power.getRegion()));
                                add(new Element(){
                                    @Override
                                    public void draw(){
                                        Building entity = getUnit();
                                        float max = entity.block.consumes.getPower().usage;
                                        float v = entity.power.status * entity.power.graph.getLastScaledPowerIn();

                                        Lines.stroke(Scl.scl(2f), Pal.removeBack);
                                        Draw.alpha(1 - v/max);
                                        Lines.line(x, y - 2f + height, x + width, y - 2f);
                                        Draw.color(Pal.remove);
                                        Draw.alpha(1 - v/max);
                                        Lines.line(x, y + height, x + width, y);
                                        Draw.reset();
                                    }
                                });
                            }};
                        }

                        if(image != null){
                            if(imaget.getClass() != image.getClass() || imaget.getClass() == Image.class){
                                clearChildren();
                                add(imaget).size(iconSmall).padBottom(2 * 8f).padRight(3 * 8f);
                                image = imaget;
                            }
                        }
                        else {
                            add(imaget).size(iconSmall).padBottom(2 * 8f).padRight(3 * 8f);
                            image = imaget;
                        }
                    }
                });
                pack();
            }});
            add(new Table(t -> {
                t.left();
                t.add(new Image(){
                    {
                        update(() -> {
                            if(getUnit() instanceof Unit && ((Unit)getUnit()).stack().item != null && ((Unit)getUnit()).stack.amount > 0)
                                setDrawable(((Unit)getUnit()).stack().item.uiIcon);
                            else setDrawable(Core.atlas.find("clear"));
                        });
                    }
                    @Override
                    public void draw() {
                        if(getUnit() instanceof Building || (getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild)) return;
                        super.draw();
                    }
                }.setScaling(Scaling.fit)).size(Scl.scl(30f)).padBottom(Scl.scl(4 * 8f)).padRight(Scl.scl(6 * 8f));
                t.pack();
            }));
        }});
        bars.add(new SBar(
                () -> {
                    if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit) getUnit()).tile() instanceof Turret.TurretBuild){
                        Turret.TurretBuild entity = ((Turret.TurretBuild)((BlockUnitUnit) getUnit()).tile());
                        float value = Mathf.clamp(heat2 / ((Turret)entity.block).chargeTime) * 100f;
                        return Core.bundle.format("shar-stat.charge", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2)));
                    }
                    if(getUnit() instanceof Turret.TurretBuild){
                        Turret.TurretBuild entity = getUnit();
                        float value = Mathf.clamp(heat2 / ((Turret)entity.block).chargeTime) * 100f;
                        return Core.bundle.format("shar-stat.charge", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2)));
                    }
                    if(getUnit() instanceof Unit)
                        return Core.bundle.format("shar-stat.commandUnits", Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit()), ((Unit)getUnit()).type().commandLimit);
                    return "[lightgray]<Empty>[]";
                },
                () -> {
                    if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit) getUnit()).tile() instanceof Turret.TurretBuild){
                        Turret.TurretBuild entity = ((Turret.TurretBuild)((BlockUnitUnit) getUnit()).tile());
                        return Pal.surge.cpy().lerp(Pal.accent, heat2 / ((Turret)entity.block).chargeTime);
                    }
                    if(getUnit() instanceof Turret.TurretBuild){
                        return Pal.surge.cpy().lerp(Pal.accent, heat2 / ((Turret)((Turret.TurretBuild)getUnit()).block).chargeTime);
                    }
                    if(getUnit() instanceof Unit)
                        return Pal.powerBar.cpy().lerp(Pal.surge.cpy().mul(Pal.lighterOrange), Mathf.absin(Time.time, 7f / (1f + Mathf.clamp(Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit()) / (((Unit)getUnit()).type().commandLimit * 1f))), 1f));
                    return Color.clear;
                },
                () -> {
                    if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit) getUnit()).tile() instanceof Turret.TurretBuild){
                        return heat2 / ((Turret)(((BlockUnitUnit) getUnit()).tile()).block).chargeTime;
                    }
                    if(getUnit() instanceof Turret.TurretBuild){
                        return heat2 / ((Turret)((Turret.TurretBuild)getUnit()).block).chargeTime;
                    }
                    if(getUnit() instanceof Unit)
                        return Mathf.clamp(Groups.unit.count(u -> u.controller() instanceof FormationAI && ((FormationAI)u.controller()).leader == getUnit()) / (((Unit)getUnit()).type().commandLimit * 1f));
                    return 0f;
                }

        ));
        if(getUnit() instanceof Unit)
            bars.add(new SBar(
                () -> Core.bundle.format("shar-stat.payloadCapacity", Mathf.round(Mathf.sqrt(((Payloadc)getUnit()).payloadUsed())), Mathf.round(Mathf.sqrt(((Unit)getUnit()).type().payloadCapacity))),
                () -> Pal.items,
                () -> Mathf.clamp(((Payloadc)getUnit()).payloadUsed() / ((Unit)getUnit()).type().payloadCapacity),
                () -> getUnit() instanceof Payloadc));

        bars.add(new Stack(){{
            add(new Table(t -> {
                t.defaults().width(Scl.scl(23 * 8f));
                t.defaults().height(Scl.scl(4f * 8f));
                t.top();
                if(getUnit() instanceof Unit) t.add(new SBar(
                        () -> Core.bundle.format("shar-stat.ammos", ((Unit)getUnit()).ammo(), ((Unit)getUnit()).type().ammoCapacity),
                        () -> ((Unit)getUnit()).dead() || getUnit() instanceof BlockUnitc ? Pal.ammo : ((Unit)getUnit()).type().ammoType.color,
                        () -> ((Unit)getUnit()).ammof(),
                        () -> Vars.state.rules.unitAmmo
                )).growX().left();
            }));
            add(new Table(t -> {
                t.left();
                t.add(new Image(){{
                    update(() -> {
                        if(!Vars.state.rules.unitAmmo){
                            setDrawable(Core.atlas.find("clear"));
                            return;
                        }
                        TextureRegion region = Items.copper.uiIcon;
                        if(getUnit() instanceof Unit && ((Unit)getUnit()).type() != null){
                            if(((Unit)getUnit()).type().ammoType == AmmoTypes.thorium) region = Items.thorium.uiIcon;
                            if(((Unit)getUnit()).type().ammoType == AmmoTypes.power || ((Unit)getUnit()).type().ammoType == AmmoTypes.powerLow || ((Unit)getUnit()).type().ammoType == AmmoTypes.powerHigh) region = Icon.powerSmall.getRegion();
                        }
                        setDrawable(region);
                    });
                }}.setScaling(Scaling.fit)).size(Scl.scl(30f)).scaling(Scaling.fit).padBottom(Scl.scl(4 * 8f)).padRight(Scl.scl(6 * 8f));
                t.pack();
            }));
        }});
    }

    public void addWeapon(){
        weapon = new Table(tx -> {
            tx.left().defaults().minSize(Scl.scl(12 * 8f));

            if(getUnit() instanceof Unit && settings.getBool("weaponui") && ((Unit)getUnit()).type != null && ((Unit)getUnit()).type.weapons.size != 0)
                tx.add(new Table(scene.getStyle(Button.ButtonStyle.class).up, tt -> {
                    tt.left().top().defaults().width(Scl.scl(24/3f * 8f)).minHeight(Scl.scl(12/3f * 8f));

                    for(int r = 0; r < ((Unit)getUnit()).type.weapons.size; r++){
                        Weapon weapon = ((Unit)getUnit()).type.weapons.get(r);
                        WeaponMount mount = ((Unit)getUnit()).mounts[r];
                        TextureRegion region = !weapon.name.equals("") && weapon.outlineRegion.found() ? weapon.outlineRegion : ((Unit)getUnit()).type.uiIcon;
                        if(((Unit)getUnit()).type.weapons.size > 1 && r % 3 == 0) tt.row();
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
                                                    getDrawable().draw(x + imageX, y + imageY, originX - imageX, originY - imageY,
                                                            imageWidth, imageHeight, scaleX, scaleY, rotation);
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
                                    h.add(new Stack(){{
                                        add(new Table(e -> {
                                            e.defaults().growX().height(Scl.scl(9)).width(Scl.scl(31.5f)).padTop(Scl.scl(9*2f));
                                            Bar reloadBar = new Bar(
                                                    () -> "",
                                                    () -> Pal.accent.cpy().lerp(Color.orange, mount.reload / weapon.reload),
                                                    () -> mount.reload / weapon.reload);
                                            e.add(reloadBar);
                                            e.pack();
                                        }));
                                    }}).padLeft(Scl.scl(8f));
                                    h.pack();
                                }));
                            }}).left();
                        }).left();
                        tt.center();
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
                                TextureRegion region = Core.atlas.find("clear");
                                if(getUnit() instanceof Unit){
                                    if(getUnit() instanceof BlockUnitUnit && ((Unit)getUnit()).type() != null)
                                        region = ((BlockUnitUnit)getUnit()).tile().block.uiIcon;
                                    else if(getUnit() != null && ((Unit)getUnit()).type() != null)
                                        region = ((Unit)getUnit()).type().uiIcon;
                                }
                                else if(getUnit() instanceof Buildingc){
                                    if(((Buildingc) getUnit()).block() != null) {
                                        region = ((Buildingc) getUnit()).block().uiIcon;
                                    }
                                }
                                setDrawable(region);
                            });
                        }}.setScaling(Scaling.fit)).size(Scl.scl(4f * 8f))));
                        add(new Table(ttt -> {
                            ttt.add(new Stack(){{
                                add(new Table(temp -> {
                                    Image image = new Image(Icon.defenseSmall);
                                    temp.add(image).center();
                                }){
                                    @Override
                                    public void draw() {
                                        if(getUnit() instanceof Building || (getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild)) return;
                                        super.draw();
                                    }
                                });
                                add(new Table(temp -> {
                                    Label label = new Label(() -> (getUnit() instanceof Unit && ((Unit)getUnit()).type() != null ? (int)((Unit)getUnit()).type().armor+"" : ""));
                                    label.setColor(Pal.surge);
                                    label.setFontScale(0.5f);
                                    temp.add(label).center();
                                    temp.pack();
                                }){
                                    @Override
                                    public void draw() {
                                        if(getUnit() instanceof Building || (getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit)getUnit()).tile() instanceof Turret.TurretBuild)) return;
                                        super.draw();
                                    }
                                });
                            }}).padLeft(Scl.scl(2 * 8f)).padBottom(Scl.scl(2 * 8f));
                        }));
                    }};

                    Label label = new Label(() -> {
                        String name = "";
                        if (getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit) getUnit()).tile() instanceof Turret.TurretBuild)
                            name = "[accent]" + ((BlockUnitUnit) getUnit()).tile().block.localizedName + "[]";
                        else if (getUnit() instanceof Unit && ((Unit) getUnit()).type() != null)
                            name = "[accent]" + ((Unit) getUnit()).type().localizedName + "[]";
                        else if (getUnit() instanceof Buildingc && ((Buildingc) getUnit()).block() != null) {
                            name = "[accent]" + ((Buildingc) getUnit()).block().localizedName + "[]";
                        }
                        return name;
                    });

                    label.setFontScale(Scl.scl());
                    TextButton button = Elem.newButton("?", Styles.clearPartialt, () -> {
                        if (getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit) getUnit()).tile() instanceof Turret.TurretBuild)
                            ui.content.show(((BlockUnitUnit) getUnit()).tile().block);
                        else if (getUnit() instanceof Unit && ((Unit) getUnit()).type() != null)
                            ui.content.show(((Unit) getUnit()).type());
                        else if (getUnit() instanceof Buildingc && ((Buildingc) getUnit()).block() != null) {
                            ui.content.show(((Buildingc) getUnit()).block());
                        }
                    });

                    tt.top();
                    tt.add(stack);
                    tt.add(label);
                    tt.add(button).size(Scl.scl(5 * 8f));
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
                if(getUnit() instanceof BlockUnitUnit && ((BlockUnitUnit) getUnit()).tile() instanceof Turret.TurretBuild){
                    Turret.TurretBuild entity = ((Turret.TurretBuild)((BlockUnitUnit) getUnit()).tile());
                    if(entity.charging) heat2 += Time.delta;
                    else heat2 = 0f;
                }
                heat += Time.delta;
                if (heat >= 6) {
                    heat = 0f;
                    if(getUnit() instanceof Unit) type = ((Unit)getUnit()).type();
                    table.removeChild(weapon);
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
        maxwave = settings.getInt("wavemax");
        for(int i = settings.getBool("pastwave") ? 0 : state.wave - 1; i <= Math.min(state.wave + maxwave, winWave - 2); i++){
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
            if(maxwave != settings.getInt("wavemax"))
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
        table.add(new Table(t -> {
            if(Vars.player.unit() == null) return;
            coreamount = Vars.player.unit().team().cores().size;
            for(int r = 0; r < coreamount; r++){
                CoreBlock.CoreBuild core = Vars.player.unit().team().cores().get(r);

                if(coreamount > 1 && r % 3 == 0) t.row();
                else if(r % 3 == 0) t.row();

                t.table(tt -> {
                    tt.add(new Stack(){{
                        add(new Table(s -> {
                            s.left();
                            Image image = new Image(core.block.uiIcon);
                            image.clicked(() -> {
                                if(control.input instanceof DesktopInput) ((DesktopInput) control.input).panning = true;
                                Core.camera.position.set(core.x, core.y);
                            });
                            if(!mobile){
                                HandCursorListener listener1 = new HandCursorListener();
                                image.addListener(listener1);
                                image.update(() -> image.color.lerp(!listener1.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta)));
                            }
                            image.addListener(new Tooltip(t -> t.background(Tex.button).add(new Label(() -> {
                                String color = Tmp.c1.set(Color.green).lerp(Color.red, 1 - core.healthf()).toString();
                                return "([#" + color + "]" + Strings.fixed(core.health, 2) + "[]/" + Strings.fixed(core.block.health, 2) + ")";
                            }))));
                            s.add(image).size(iconLarge).scaling(Scaling.fit);
                        }));

                        add(new Table(s -> {
                            s.bottom().defaults().growX().height(Scl.scl(9)).pad(4);
                            s.add(new Bar(() -> "", () -> Pal.health, core::healthf));
                            s.pack();
                        }));
                    }});
                    tt.row();
                    Label label = new Label(() -> "(" + (int)core.x / 8 + ", " + (int)core.y / 8 + ")");
                    label.setFontScale(Scl.scl());
                    tt.add(label);
                });
            }
        }));
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
            if(coreamount != Vars.player.unit().team().cores().size && Vars.player != null) corePane.setWidget(new Table(tx -> tx.table(this::setCore).left()));
        });
        corePane.setOverscroll(false, false);
        if(Vars.player != null) corePane.setWidget(new Table(tx -> tx.table(this::setCore).left()));
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
                head.table(label -> {
                    label.center();
                    label.label(() -> tile == null ? "(null, null)" : "(" + tile.x + ", " + tile.y + ")");
                });
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
}
