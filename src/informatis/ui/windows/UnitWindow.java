package informatis.ui.windows;

import arc.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.Bits;
import arc.struct.*;
import arc.util.*;
import informatis.ui.components.SBar;
import mindustry.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.Units;
import mindustry.entities.abilities.*;
import mindustry.entities.units.*;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;

import java.lang.reflect.Field;
import java.util.Objects;

import static arc.Core.bundle;
import static informatis.SVars.*;
import static informatis.SUtils.*;
import static informatis.ui.components.SIcons.*;
import static informatis.ui.components.SIcons.liquid;
import static mindustry.Vars.*;

public class UnitWindow extends Window {
    int barSize = 6;
    float usedPayload;
    float barScrollPos;
    float lastWidth;
    final Seq<Color> lastColors = new Seq<>();
    final Bits statuses = new Bits();
    public Teamc lastTarget, target;
    public boolean locked;
    Seq<BarData> data = new Seq<>();
    public static UnitWindow currentWindow;

    public UnitWindow() {
        super(Icon.units, "unit");
        currentWindow = this;
    }

    @Override
    public void build() {
        super.build();
        Element titlePane = ((Table) ((ScrollPane) ((Table) getChildren().first()).getChildren().first()).getWidget()).getChildren().first();
        titlePane.update(() -> titlePane.setColor(currentWindow == this ? Pal.accent : Color.white));
        Events.run(EventType.Trigger.update, () -> {
            if(!locked) target = getTarget();
            if(hasMouse()) currentWindow = this;
            try {
                data = getInfo(target);
            } catch (IllegalAccessException | NoSuchFieldException err) {
                err.printStackTrace();
            }
        });
    }

    @Override
    protected void buildBody(Table table) {
        Image profileImage = new Image() {
            final int size = 8;
            @Override
            public void draw() {
                super.draw();

                Draw.color(locked ? Pal.accent : Pal.gray);
                Draw.alpha(parentAlpha);
                Lines.stroke(Scl.scl(3f));
                Lines.rect(x-size/2f, y-size/2f, width+size, height+size);
                Draw.reset();
            }
        };
        profileImage.update(() -> {
            TextureRegion region = clear;
            if (target instanceof Unit u && u.type != null) region = u.type.uiIcon;
            else if (target instanceof Building b) {
                if (b instanceof ConstructBlock.ConstructBuild cb) region = cb.current.uiIcon;
                else if (b.block != null) region = b.block.uiIcon;
            }
            profileImage.setDrawable(region);
        });
        profileImage.clicked(() -> locked = !locked);
        Label profileLabel = new Label(() -> {
            if (target instanceof Unit u && u.type != null) return u.type.localizedName;
            if (target instanceof Building b && b.block != null) {
                if (target instanceof ConstructBlock.ConstructBuild cb) return cb.current.localizedName;
                return b.block.localizedName;
            }
            return "";
        });
        profileLabel.clicked(() -> moveCamera(target));

        ScrollPane barPane = new ScrollPane(buildBarList(), Styles.noBarPane);
        barPane.update(() -> {
            if (lastTarget != target) {
                lastTarget = target;
                for (int i = 0; i < barSize; i++) {
                    Color color = i >= data.size ? Color.clear : data.get(i).color;
                    if (i >= lastColors.size) lastColors.add(color);
                    else lastColors.set(i, color);
                }
            }

            if (((Table) barPane.getWidget()).getChildren().size - 1 != barSize) {
                barPane.setWidget(buildBarList());
            }

            if (barPane.hasScroll()) {
                Element result = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if (result == null || !result.isDescendantOf(barPane)) {
                    Core.scene.setScrollFocus(null);
                }
            }
            barScrollPos = barPane.getScrollY();
        });
        barPane.setScrollingDisabledX(true);
        barPane.setScrollYForce(barScrollPos);

        table.top().background(Styles.black8);
        table.table(profile -> profile
            .table(title -> {
                title.center();
                title.add(profileImage).size(iconMed);
                title.add(profileLabel).padLeft(12f).padRight(12f).color(Pal.accent);
            })
            .tooltip(tool -> {
                tool.background(Styles.black6);
                tool.label(() -> target instanceof Unit u ? u.isPlayer() ? u.getPlayer().name : "AI" : "").row();
                tool.label(() -> target.tileX() + ", " + target.tileY()).row();
                tool.label(() -> target instanceof Unit u ? "[accent]"+ Strings.fixed(u.armor, 0) + "[] Armor" : "");
            }).get()
        ).margin(3f).growX().row();
        table.table().update(tt -> {
            tt.clear();
            if(target instanceof Unit u && u.type != null && u.hasWeapons()) {
                for(int r = 0; r < u.type.weapons.size; r++){
                    Weapon weapon = u.type.weapons.get(r);
                    WeaponMount mount = u.mounts[r];
                    tt.table(ttt -> {
                        ttt.left();
                        ttt.stack(
                                new Table(o -> {
                                    o.left();
                                    o.add(new Image(Core.atlas.isFound(weapon.region) ? weapon.region : u.type.uiIcon){
                                        @Override
                                        public void draw(){
                                            y -= (mount.reload) / weapon.reload * weapon.recoil;
                                            super.draw();
                                        }
                                    }).size(iconLarge).scaling(Scaling.bounded);
                                }),
                                new Table(h -> {
                                    h.defaults().growX().height(9f).width(iconLarge).padTop(18f);
                                    h.add(new SBar(
                                            () -> "",
                                            () -> Pal.accent.cpy().lerp(Color.orange, mount.reload / weapon.reload),
                                            () -> mount.reload / weapon.reload).rect().init());
                                    h.pack();
                                })
                        );
                    }).pad(4);
                    if((r + 1) % 4 == 0) tt.row();
                }
            }
        }).margin(4f).growX().row();
        table.table(state -> {
            state.left();
            state.table().update(t -> {
                if (!(target instanceof Payloadc payloader)) {
                    t.clear();
                    usedPayload = -1;
                    return;
                }

                if(usedPayload == payloader.payloadUsed() && lastWidth == getWidth()) return;
                if(usedPayload != payloader.payloadUsed()) usedPayload = payloader.payloadUsed();
                if(lastWidth != getWidth()) lastWidth = getWidth();

                t.clear();
                t.top().left();
                Seq<Payload> payloads = payloader.payloads();
                for (int i = 0, m = payloads.size; i < m; i++) {
                    Payload payload = payloads.get(i);
                    Image image = new Image(payload.icon());
                    image.clicked(() -> ui.content.show(payload.content()));
                    image.hovered(() -> image.setColor(Tmp.c1.set(image.color).lerp(Color.lightGray, Mathf.clamp(Time.delta))));
                    image.exited(() -> image.setColor(Tmp.c1.set(image.color).lerp(Color.white, Mathf.clamp(Time.delta))));
                    t.add(image).size(iconSmall).tooltip(l -> l.label(() -> payload.content().localizedName).style(Styles.outlineLabel));
                    if ((i + 1) % Math.max(6, Math.round((getWidth() - 24) / iconSmall)) == 0) t.row();
                }
            });

            state.table().update(t -> {
                if (!(target instanceof Statusc st)) {
                    t.clear();
                    statuses.clear();
                    return;
                }
                Bits applied = st.statusBits();

                if((applied == null || statuses.equals(st.statusBits())) && lastWidth == getWidth()) return;
                if(!statuses.equals(st.statusBits())) statuses.set(applied);
                if(lastWidth != getWidth()) lastWidth = getWidth();

                t.clear();
                t.top().left();
                Seq<StatusEffect> contents = Vars.content.statusEffects();
                for (int i = 0, m = Vars.content.statusEffects().size; i < m; i++) {
                    StatusEffect effect = contents.get(i);
                    if (Objects.requireNonNull(applied).get(effect.id) && !effect.isHidden()) {
                        Image image = new Image(effect.uiIcon);
                        image.clicked(() -> ui.content.show(effect));
                        image.hovered(() -> image.setColor(Tmp.c1.set(image.color).lerp(Color.lightGray, Mathf.clamp(Time.delta))));
                        image.exited(() -> image.setColor(Tmp.c1.set(image.color).lerp(Color.white, Mathf.clamp(Time.delta))));
                        t.add(image).size(iconSmall).tooltip(l -> l.label(() -> effect.localizedName + " [lightgray]" + UI.formatTime(st.getDuration(effect))).style(Styles.outlineLabel));
                        if (i + 1 % Math.max(6, Math.round((getWidth() - 24) / iconSmall)) == 0) t.row();
                    }
                }
            });
        }).growX().row();
        table.image().color((target == null ? player.unit() : target).team().color).height(4f).growX().row();
        table.add(barPane).grow().padTop(12f);
    }

    Table buildBarList() {
        return new Table(table -> {
            table.top();
            for (int i = 0; i < barSize; i++) {
                table.add(addBar(i)).growX().row();
            }
        });
    }

    Table addBar(int index) {
        return new Table(bar -> {
            bar.add(new SBar(
                    () -> index >= data.size ? "[lightgray]<Empty>[]" : data.get(index).name,
                    () -> index >= data.size ? Color.clear : data.get(index).color,
                    () -> lastColors.get(index),
                    () -> index >= data.size ? 0 : data.get(index).number)
            ).height(4 * 8f).growX();
            if(index >= data.size) return;
            Image icon = new Image(){
                @Override
                public void draw() {
                    validate();
                    if(index >= data.size) return;
                    float x = this.x + imageX;
                    float y = this.y + imageY;
                    float width = imageWidth * this.scaleX;
                    float height = imageHeight * this.scaleY;
                    Draw.color(Color.white);
                    Draw.alpha(parentAlpha * color.a);
                    BarData da = data.get(index);
                    if(hasMouse()) getDrawable().draw(x, y, width, height);
                    else {
                        da.icon.draw(x, y, width, height);
                        if(ScissorStack.push(Tmp.r1.set(ScissorStack.peek().x + x,  ScissorStack.peek().y + y, width, height * da.number))) {
                            Draw.color(da.color);
                            da.icon.draw(x, y, width, height);
                            ScissorStack.pop();
                        }
                    }
                }
            };
            icon.setDrawable(data.get(index).icon);
            bar.add(icon).size(iconMed * 0.75f).padLeft(8f);
        });
    }
    public Seq<BarData> getInfo(Teamc target) throws IllegalAccessException, NoSuchFieldException {
        data.clear();

        if(target instanceof Healthc healthc){
            data.add(new BarData(bundle.format("shar-stat.health", formatNumber(healthc.health())), Pal.health, healthc.healthf(), health));
        }

        if(target instanceof Unit unit){
            float max = ((ShieldRegenFieldAbility) Vars.content.units().copy().max(ut -> {
                ShieldRegenFieldAbility ability = (ShieldRegenFieldAbility) ut.abilities.find(ab -> ab instanceof ShieldRegenFieldAbility);
                if(ability == null) return 0;
                return ability.max;
            }).abilities.find(abil -> abil instanceof ShieldRegenFieldAbility)).max;

            data.add(new BarData(bundle.format("shar-stat.shield", formatNumber(unit.shield())), Pal.surge, unit.shield() / max, shield));
            data.add(new BarData(bundle.format("shar-stat.capacity", unit.stack.item.localizedName, formatNumber(unit.stack.amount), formatNumber(unit.type.itemCapacity)), unit.stack.amount > 0 && unit.stack().item != null ? unit.stack.item.color.cpy().lerp(Color.white, 0.15f) : Color.white, unit.stack.amount / (unit.type.itemCapacity * 1f), item));
            if(target instanceof Payloadc pay) data.add(new BarData(bundle.format("shar-stat.payloadCapacity", formatNumber(Mathf.round(Mathf.sqrt(pay.payloadUsed()))), formatNumber(Mathf.round(Mathf.sqrt(unit.type().payloadCapacity)))), Pal.items, pay.payloadUsed() / unit.type().payloadCapacity));
            if(state.rules.unitAmmo) data.add(new BarData(bundle.format("shar-stat.ammos", formatNumber(unit.ammo()), formatNumber(unit.type().ammoCapacity)), unit.type().ammoType.color(), unit.ammof()));
        }

        else if(target instanceof Building build){
            if(build.block.hasLiquids) data.add(new BarData(bundle.format("shar-stat.capacity", build.liquids.currentAmount() < 0.01f ? build.liquids.current().localizedName : bundle.get("bar.liquid"), formatNumber(build.liquids.currentAmount()), formatNumber(build.block.liquidCapacity)), build.liquids.current().color, build.liquids.currentAmount() / build.block.liquidCapacity, liquid));

            if(build.block.hasPower && build.block.consPower != null){
                ConsumePower cons = build.block.consPower;
                data.add(new BarData(bundle.format("shar-stat.power", formatNumber(build.power.status * 60f * (cons.buffered ? cons.capacity : cons.usage)), formatNumber(60f * (cons.buffered ? cons.capacity : cons.usage))), Pal.powerBar, Mathf.zero(cons.requestedPower(build)) && build.power.graph.getPowerProduced() + build.power.graph.getBatteryStored() > 0f ? 1f : build.power.status, power));
            }
            if(build.block.hasItems) {
                float value;
                if (target instanceof CoreBlock.CoreBuild cb) value = cb.storageCapacity * Vars.content.items().count(UnlockableContent::unlockedNow);
                else if(target instanceof StorageBlock.StorageBuild sb && !sb.canPickup() && sb.linkedCore instanceof CoreBlock.CoreBuild cb) value = cb.storageCapacity * Vars.content.items().count(UnlockableContent::unlockedNow);
                else value = build.block.itemCapacity;
                data.add(new BarData(bundle.format("shar-stat.capacity", bundle.get("category.items"), formatNumber(build.items.total()), value), Pal.items, build.items.total() / value, item));
            }
        }

        if(target instanceof ReloadTurret.ReloadTurretBuild || target instanceof MassDriver.MassDriverBuild){
            float pro;
            if(target instanceof ReloadTurret.ReloadTurretBuild turret) pro = turret.reloadCounter / ((ReloadTurret)turret.block).reload;
            else {
                MassDriver.MassDriverBuild mass = (MassDriver.MassDriverBuild) target;
                pro = mass.reloadCounter;
            }
            data.add(new BarData(bundle.format("shar-stat.reload", formatNumber(pro * 100f)), Pal.accent.cpy().lerp(Color.orange, pro), pro, reload));
        }

        if(target instanceof ForceProjector.ForceBuild force){
            ForceProjector forceBlock = (ForceProjector) force.block;
            float max = forceBlock.shieldHealth + forceBlock.phaseShieldBoost * force.phaseHeat;
            data.add(new BarData(bundle.format("shar-stat.shield", formatNumber(max-force.buildup), formatNumber(max)), Pal.shield, (max-force.buildup)/max, shield));
        }

        if(target instanceof MendProjector.MendBuild || target instanceof OverdriveProjector.OverdriveBuild || target instanceof ConstructBlock.ConstructBuild || target instanceof Reconstructor.ReconstructorBuild || target instanceof UnitFactory.UnitFactoryBuild || target instanceof Drill.DrillBuild || target instanceof GenericCrafter.GenericCrafterBuild) {
            float pro;
            if(target instanceof MendProjector.MendBuild mend){
                pro = (float) mend.sense(LAccess.progress);
                Tmp.c1.set(Pal.heal);
            }
            else if(target instanceof OverdriveProjector.OverdriveBuild over){
                OverdriveProjector block = (OverdriveProjector)over.block;
                Field ohno = OverdriveProjector.OverdriveBuild.class.getDeclaredField("charge");
                ohno.setAccessible(true);
                pro = (float) ohno.get(over)/((OverdriveProjector)over.block).reload;
                Tmp.c1.set(Color.valueOf("feb380"));

                data.add(new BarData(bundle.format("bar.boost", (int)(over.realBoost() * 100)), Pal.accent, over.realBoost() / (block.hasBoost ? block.speedBoost + block.speedBoostPhase : block.speedBoost)));
            }
            else if(target instanceof ConstructBlock.ConstructBuild construct){
                pro = construct.progress;
                Tmp.c1.set(Pal.darkerMetal);
            }
            else if(target instanceof UnitFactory.UnitFactoryBuild factory){
                pro = factory.fraction();
                Tmp.c1.set(Pal.darkerMetal);

                if(factory.unit() == null) data.add(new BarData("[lightgray]" + Iconc.cancel, Pal.power, 0f));
                else {
                    float value = factory.team.data().countType(factory.unit());
                    data.add(new BarData(bundle.format("bar.unitcap", Fonts.getUnicodeStr(factory.unit().name), formatNumber(value), formatNumber(Units.getCap(factory.team))), Pal.power, value / Units.getCap(factory.team)));
                }
            }
            else if(target instanceof Reconstructor.ReconstructorBuild reconstruct){
                pro = reconstruct.fraction();
                Tmp.c1.set(Pal.darkerMetal);

                if(reconstruct.unit() == null) data.add(new BarData("[lightgray]" + Iconc.cancel, Pal.power, 0f));
                else {
                    float value = reconstruct.team.data().countType(reconstruct.unit());
                    data.add(new BarData(bundle.format("bar.unitcap", Fonts.getUnicodeStr(reconstruct.unit().name), formatNumber(value), formatNumber(Units.getCap(reconstruct.team))), Pal.power, value / Units.getCap(reconstruct.team)));
                }

            }
            else if(target instanceof Drill.DrillBuild drill){
                pro = (float) drill.sense(LAccess.progress);
                Tmp.c1.set(drill.dominantItem == null ? Pal.items : drill.dominantItem.color);

                data.add(new BarData(bundle.format("bar.drillspeed", formatNumber(drill.lastDrillSpeed * 60 * drill.timeScale())), Pal.ammo, drill.warmup));
            }
            else {
                GenericCrafter.GenericCrafterBuild crafter = (GenericCrafter.GenericCrafterBuild) target;
                GenericCrafter block = (GenericCrafter) crafter.block;

                pro = (float) crafter.sense(LAccess.progress);
                if(block.outputItem != null) Tmp.c1.set(block.outputItem.item.color);
                else if(block.outputLiquid != null) Tmp.c1.set(block.outputLiquid.liquid.color);
                else Tmp.c1.set(Pal.items);
            }

            data.add(new BarData(bundle.format("shar-stat.progress", formatNumber(pro * 100f)), Tmp.c1, pro));
        }

        if(target instanceof PowerGenerator.GeneratorBuild generator){
            data.add(new BarData(bundle.format("shar-stat.powerIn", formatNumber(generator.getPowerProduction() * generator.timeScale() * 60f)), Pal.powerBar, generator.productionEfficiency, power));
        }

        if(target instanceof PowerNode.PowerNodeBuild || target instanceof PowerTurret.PowerTurretBuild) {
            float value, max;
            if(target instanceof PowerNode.PowerNodeBuild node){
                max = node.power.graph.getLastPowerStored();
                value = node.power.graph.getLastCapacity();

                data.add(new BarData(bundle.format("bar.powerlines", node.power.links.size, ((PowerNode)node.block).maxNodes), Pal.items, (float)node.power.links.size / (float)((PowerNode)node.block).maxNodes));
                data.add(new BarData(bundle.format("shar-stat.powerOut", "-" + formatNumber(node.power.graph.getLastScaledPowerOut() * 60f)), Pal.powerBar, node.power.graph.getLastScaledPowerOut() / node.power.graph.getLastScaledPowerIn(), power));
                data.add(new BarData(bundle.format("shar-stat.powerIn", formatNumber(node.power.graph.getLastScaledPowerIn() * 60f)), Pal.powerBar, node.power.graph.getLastScaledPowerIn() / node.power.graph.getLastScaledPowerOut(), power));
                data.add(new BarData(bundle.format("bar.powerbalance", (node.power.graph.getPowerBalance() >= 0 ? "+" : "") + formatNumber(node.power.graph.getPowerBalance() * 60)), Pal.powerBar, node.power.graph.getLastPowerProduced() / node.power.graph.getLastPowerNeeded(), power));
            }
            else { //TODO: why is this different
                PowerTurret.PowerTurretBuild turret = (PowerTurret.PowerTurretBuild) target;
                max = turret.block.consPower.usage;
                value = turret.power.status * turret.power.graph.getLastScaledPowerIn();
            }

            data.add(new BarData(bundle.format("shar-stat.power", formatNumber(Math.max(value, max) * 60), formatNumber(max * 60)), Pal.power, value / max));
        }

        if(target instanceof ItemTurret.ItemTurretBuild turret) {
            ItemTurret block = (ItemTurret)turret.block;
            data.add(new BarData(bundle.format("shar-stat.capacity", turret.hasAmmo() ? block.ammoTypes.findKey(turret.peekAmmo(), true).localizedName : bundle.get("stat.ammo"), formatNumber(turret.totalAmmo), formatNumber(block.maxAmmo)), turret.hasAmmo() ? block.ammoTypes.findKey(turret.peekAmmo(), true).color : Pal.ammo, turret.totalAmmo / (float)block.maxAmmo, ammo));
        }

        if(target instanceof LiquidTurret.LiquidTurretBuild turret){
            data.add(new BarData(bundle.format("shar-stat.capacity", turret.liquids.currentAmount() < 0.01f ? turret.liquids.current().localizedName : bundle.get("stat.ammo"), formatNumber(turret.liquids.get(turret.liquids.current())), formatNumber(turret.block.liquidCapacity)), turret.liquids.current().color, turret.liquids.get(turret.liquids.current()) / turret.block.liquidCapacity, liquid));
        }

        if(target instanceof AttributeCrafter.AttributeCrafterBuild || target instanceof ThermalGenerator.ThermalGeneratorBuild || (target instanceof SolidPump.SolidPumpBuild crafter && ((SolidPump)crafter.block).attribute != null)) {
            float display, pro;
            if (target instanceof AttributeCrafter.AttributeCrafterBuild crafter) {
                AttributeCrafter block = (AttributeCrafter) crafter.block;
                display = (block.baseEfficiency + Math.min(block.maxBoost, block.boostScale * block.sumAttribute(block.attribute, crafter.tileX(), crafter.tileY()))) * 100f;
                pro = block.boostScale * crafter.attrsum / block.maxBoost;
            }
            else if (target instanceof ThermalGenerator.ThermalGeneratorBuild thermal) {
                ThermalGenerator block = (ThermalGenerator) thermal.block;
                float max = Vars.content.blocks().max(b -> b instanceof Floor f && f.attributes != null ? f.attributes.get(block.attribute) : 0).asFloor().attributes.get(block.attribute);
                display = block.sumAttribute(block.attribute, thermal.tileX(), thermal.tileY()) * 100;
                pro = block.sumAttribute(block.attribute, thermal.tileX(), thermal.tileY()) / block.size / block.size / max;
            }
            else {
                SolidPump.SolidPumpBuild crafter = (SolidPump.SolidPumpBuild) target;
                SolidPump block = (SolidPump) crafter.block;
                float fraction = Math.max(crafter.validTiles + crafter.boost + (block.attribute == null ? 0 : block.attribute.env()), 0);
                float max = Vars.content.blocks().max(b -> b instanceof Floor f && f.attributes != null ? f.attributes.get(block.attribute) : 0).asFloor().attributes.get(block.attribute);
                display = Math.max(block.sumAttribute(block.attribute, crafter.tileX(), crafter.tileY()) / block.size / block.size + block.baseEfficiency, 0f) * 100 * block.percentSolid(crafter.tileX(), crafter.tileY());
                pro = fraction / max;
            }

            data.add(new BarData(bundle.format("shar-stat.attr", Mathf.round(display)), Pal.ammo, pro));
        }

        if(target instanceof UnitAssembler.UnitAssemblerBuild assemblerBuild) {
            UnitAssembler.AssemblerUnitPlan plan = assemblerBuild.plan();

            UnitType unit = assemblerBuild.unit();
            if(unit == null) data.add(new BarData("[lightgray]" + Iconc.cancel, Pal.power, 0f));
            else data.add(new BarData(bundle.format("shar-stat.progress", Math.round(assemblerBuild.progress * 100 * 100) / 100), Pal.power, assemblerBuild.progress));

            for(PayloadStack stack : plan.requirements) {
                int value = assemblerBuild.blocks.get(stack.item);
                int max = stack.amount;
                float pro = value / (max * 1f);
                data.add(new BarData(stack.item.localizedName + ": " + value + " / " + max, Pal.accent.cpy().lerp(Color.orange, pro), pro, stack.item.fullIcon));
            }
        }

        return data;
    }

    static class BarData {
        public String name;
        public Color color;
        public float number;
        public Drawable icon = new TextureRegionDrawable(clear);

        BarData(String name, Color color, float number) {
            this.name = name;
            this.color = color;
            this.number = number;
        }

        BarData(String name, Color color, float number, TextureRegion icon) {
            this(name, color, number);
            this.icon = new TextureRegionDrawable(icon);
        }
    }
}
