package informatis.ui.fragments;

import arc.Core;
import arc.func.Cons;
import arc.func.Floatf;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.Elem;
import arc.struct.Seq;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.mod.Mods;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.ui.Styles.*;

public class QuickSchemFragment extends Table {
    private final Seq<String> selectedTags = new Seq<>();

    private static final ImageButton.ImageButtonStyle schmaticButtonStyle = new ImageButton.ImageButtonStyle() {{
        down = flatDown;
        up = Tex.pane;
        over = flatOver;
        disabled = black8;
        imageDisabledColor = Color.lightGray;
        imageUpColor = Color.white;
    }};

    public QuickSchemFragment() {
        right().defaults().growX().minWidth(180f);
        Button button = add(new Button(Styles.squareTogglet) {{
            margin(0);
            add(bundle.get("hud.schematic-list"));
            Image image = image(Icon.downOpenSmall).padLeft(8f).get();
            clicked(() -> {
                image.setDrawable(isChecked() ? Icon.upOpenSmall : Icon.downOpenSmall);
            });
        }}).height(60f).get();
        row();
        collapser(rebuildBody(), true, button::isChecked);
        Table table = (Table) scene.find("minimap/position");
        table.row();
        table.add(this);
    }

    public Table rebuildBody() {
        // scheme-size crashed the mod
        Mods.LoadedMod schemSizeMod = mods.getMod("scheme-size");
        if(schemSizeMod == null || schemSizeMod.enabled()) return new Table();

        visible = settings.getBool(("schem"));
        if(!visible) return new Table();

        return new Table(t -> {
            t.background(Styles.black8).defaults().maxHeight(72 * 8f).growX();
            t.pane(noBarPane, tagsListTable -> {
                tagsListTable.left().defaults().pad(2).height(42f);

                Seq<String> tags = Reflect.get(ui.schematics, "tags");
                for(String tag : tags){
                    tagsListTable.button(tag, togglet, () -> {
                        if(selectedTags.contains(tag)) selectedTags.remove(tag);
                        else selectedTags.add(tag);

                        rebuildBody();
                    }).wrapLabel(false);
                }
            }).pad(8, 0, 8, 0).maxHeight(42f).scrollX(true).scrollY(false);
            t.row();
            t.pane(noBarPane, schemListTable -> {
                schemListTable.button("@mods.reload", Icon.redo, this::rebuildBody).growX();
                schemListTable.row();
                schemListTable.button("@editor.import", Icon.download, () -> ui.schematics.showImport()).growX();
                schemListTable.row();

                if(schematics.all().isEmpty()) {
                    schemListTable.add(bundle.get("none"));
                    return;
                }

                for(Schematic schematic : schematics.all()) {
                    if(selectedTags.any() && !schematic.labels.containsAll(selectedTags)) continue;
                    schemListTable.add(new Button() {{
                        top().margin(0f);
                        clicked(() -> {
                            if(childrenPressed()) return;
                            control.input.useSchematic(schematic);
                        });
                        table(toolbarTable -> {
                            toolbarTable.left().defaults().size(162 / 4f);

                            toolbarTable.button(Icon.info, cleari, () -> ui.schematics.showInfo(schematic));
                            toolbarTable.button(Icon.upload, cleari, () -> ui.schematics.showExport(schematic));
                            toolbarTable.button(Icon.pencil, cleari, () -> showRename(schematic));
                            if(schematic.hasSteamID()) {
                                toolbarTable.button(Icon.link, cleari, () -> platform.viewListing(schematic));
                            } else {
                                toolbarTable.button(Icon.trash, cleari, () -> {
                                    if(schematic.mod != null) {
                                        ui.showInfo(Core.bundle.format("mod.item.remove", schematic.mod.meta.displayName()));
                                    } else {
                                        ui.showConfirm("@confirm", "@schematic.delete.confirm", () -> {
                                            schematics.remove(schematic);
                                            rebuildBody();
                                        });
                                    }
                                });
                            }
                        }).growX().height(50f);
                        row();
                        stack(
                            new SchematicsDialog.SchematicImage(schematic).setScaling(Scaling.fit),
                            new Table(t -> {
                                t.top();
                                t.table(black3, tt -> tt.add(schematic.name(), outlineLabel).ellipsis(true).labelAlign(Align.center))
                                        .top().growX().marginTop(4f);
                            })
                        ).size(160f);
                    }}).pad(4).style(schmaticButtonStyle);
                    schemListTable.row();
                }
            }).growY().scrollX(false).scrollY(true);
        });
    }

    void showRename(Schematic schematic) {
        new Dialog("@schematic.rename"){{
            setFillParent(true);

            cont.margin(30);

            cont.add("@schematic.tags").padRight(6f);
            cont.table(tags -> buildTags(schematic, tags, false)).maxWidth(400f).fillX().left().row();

            cont.margin(30).add("@name").padRight(6f);
            TextField nameField = cont.field(schematic.name(), null).size(400f, 55f).left().get();

            cont.row();

            cont.margin(30).add("@editor.description").padRight(6f);
            TextField descField = cont.area(schematic.description(), Styles.areaField, t -> {}).size(400f, 140f).left().get();

            Runnable accept = () -> {
                schematic.tags.put("name", nameField.getText());
                schematic.tags.put("description", descField.getText());
                schematic.save();
                hide();
                rebuildBody();
            };

            buttons.defaults().size(120, 54).pad(4);
            buttons.button("@ok", accept).disabled(b -> nameField.getText().isEmpty());
            buttons.button("@cancel", this::hide);

            keyDown(KeyCode.enter, () -> {
                if(!nameField.getText().isEmpty() && Core.scene.getKeyboardFocus() != descField){
                    accept.run();
                }
            });
            keyDown(KeyCode.escape, this::hide);
            keyDown(KeyCode.back, this::hide);
            show();
        }};
    }

    void tagsChanged() {
        Seq<String> tags = Reflect.get(ui.schematics, "tags");
        Core.settings.putJson("schematic-tags", String.class, tags);
    }

    void addTag(Schematic s, String tag){
        s.labels.add(tag);
        s.save();
        tagsChanged();
    }

    void removeTag(Schematic s, String tag){
        s.labels.remove(tag);
        s.save();
        tagsChanged();
    }

    //shows a dialog for creating a new tag
    void showNewTag(Cons<String> result){
        Seq<String> tags = Reflect.get(ui.schematics, "tags");
        ui.showTextInput("@schematic.addtag", "", "", out -> {
            if(tags.contains(out)){
                ui.showInfo("@schematic.tagexists");
            }else{
                tags.add(out);
                tagsChanged();
                result.get(out);
            }
        });
    }

    void showNewIconTag(Cons<String> cons){
        Seq<String> tags = Reflect.get(ui.schematics, "tags");
        new Dialog(){{
            closeOnBack();
            setFillParent(true);

            cont.pane(t ->
                    resized(true, () -> {
                        t.clearChildren();
                        t.marginRight(19f);
                        t.defaults().size(48f);

                        int cols = (int)Math.min(20, Core.graphics.getWidth() / Scl.scl(52f));

                        for(ContentType ctype : defaultContentIcons){
                            t.row();
                            t.image().colspan(cols).growX().width(Float.NEGATIVE_INFINITY).height(3f).color(Pal.accent);
                            t.row();

                            int i = 0;
                            for(UnlockableContent u : content.getBy(ctype).<UnlockableContent>as()){
                                if(!u.isHidden() && u.unlockedNow() && u.hasEmoji() && !tags.contains(u.emoji())){
                                    t.button(new TextureRegionDrawable(u.uiIcon), Styles.cleari, iconMed, () -> {
                                        String out = u.emoji();

                                        tags.add(out);
                                        tagsChanged();
                                        cons.get(out);

                                        hide();
                                    });

                                    if(++i % cols == 0) t.row();
                                }
                            }
                        }
                    })
            );
            buttons.button("@back", Icon.left, this::hide).size(210f, 64f);
        }}.show();
    }

    void buildTags(Schematic schem, Table t, boolean name){
        t.clearChildren();
        t.left();
        Seq<String> tags = Reflect.get(ui.schematics, "tags");

        //sort by order in the main target array. the complexity of this is probably awful
        schem.labels.sort((Floatf<String>) tags::indexOf);

        if(name) t.add("@schematic.tags").padRight(4);
        t.pane(s -> {
            s.left();
            s.defaults().pad(3).height(42f);
            for(var tag : schem.labels){
                s.table(Tex.button, i -> {
                    i.add(tag).padRight(4).height(42f).labelAlign(Align.center);
                    i.button(Icon.cancelSmall, Styles.emptyi, () -> {
                        removeTag(schem, tag);
                        buildTags(schem, t, name);
                    }).size(42f).padRight(-9f).padLeft(-9f);
                });
            }

        }).fillX().left().height(42f).scrollY(false);

        t.button(Icon.addSmall, () -> {
            var dialog = new BaseDialog("@schematic.addtag");
            dialog.addCloseButton();
            dialog.cont.pane(p -> {
                p.clearChildren();

                float sum = 0f;
                Table current = new Table().left();
                for(var tag : tags){
                    if(schem.labels.contains(tag)) continue;

                    var next = Elem.newButton(tag, () -> {
                        addTag(schem, tag);
                        buildTags(schem, t, name);
                        dialog.hide();
                    });
                    next.getLabel().setWrap(false);

                    next.pack();
                    float w = next.getPrefWidth() + Scl.scl(6f);

                    if(w + sum >= Core.graphics.getWidth() * (Core.graphics.isPortrait() ? 1f : 0.8f)){
                        p.add(current).row();
                        current = new Table();
                        current.left();
                        current.add(next).height(42f).pad(2);
                        sum = 0;
                    }else{
                        current.add(next).height(42f).pad(2);
                    }

                    sum += w;
                }

                if(sum > 0){
                    p.add(current).row();
                }

                Cons<String> handleTag = res -> {
                    dialog.hide();
                    addTag(schem, res);
                    buildTags(schem, t, name);
                };

                p.row();

                p.table(v -> {
                    v.left().defaults().fillX().height(42f).pad(2);
                    v.button("@schematic.texttag", Icon.add, () -> showNewTag(handleTag)).wrapLabel(false).get().getLabelCell().padLeft(4);
                    v.button("@schematic.icontag", Icon.add, () -> showNewIconTag(handleTag)).wrapLabel(false).get().getLabelCell().padLeft(4);
                });
            });
            dialog.show();
        }).size(42f).tooltip("@schematic.addtag");
    }
}
