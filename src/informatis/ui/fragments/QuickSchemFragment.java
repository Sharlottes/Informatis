package informatis.ui.fragments;

import informatis.SUtils;
import arc.Core;
import arc.func.Cons;
import arc.func.Floatf;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.Elem;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.Vars.ui;
import static mindustry.ui.Styles.*;

public class QuickSchemFragment extends Table {
    static float schemScrollPos, tagScrollPos;
    static boolean schemShown;
    static Schematic firstSchematic;
    static final Seq<String> selectedTags = new Seq<>();
    static Runnable rebuildList = () -> {};

    public QuickSchemFragment() {
        setSchemTable();
        Table table = ((Table) scene.find("minimap/position")).row();
        table.add(this);
    }

    public void setSchemTable() {
       clear();
       if(!settings.getBool(("schem"))) return;
       right();
       button(bundle.get("hud.schematic-list"), Icon.downOpen, Styles.squareTogglet, () -> {
           if(!schemShown) setSchemTable();
           schemShown = !schemShown;
       }).width(160f).height(60f).checked(b -> {
            Image image = (Image)b.getCells().first().get();
            image.setDrawable(schemShown ? Icon.upOpen : Icon.downOpen);
            return schemShown;
        }).row();
       collapser(t -> {
            t.background(Styles.black8).defaults().maxHeight(72 * 8f).maxWidth(160f);
            ScrollPane tagPane = new ScrollPane(new Table(p -> {
                p.left().defaults().pad(2).height(42f);

                Seq<String> tags = Reflect.get(ui.schematics, "tags");
                for(String tag : tags){
                    p.button(tag, Styles.togglet, () -> {
                        if(selectedTags.contains(tag)) selectedTags.remove(tag);
                        else selectedTags.add(tag);

                        setSchemTable();
                    }).checked(selectedTags.contains(tag)).with(c -> c.getLabel().setWrap(false));
                }
            }), Styles.noBarPane);
            tagPane.update(() -> {
                Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                if(tagPane.hasScroll() && (result == null || !result.isDescendantOf(tagPane)))
                    scene.setScrollFocus(null);
                tagScrollPos = tagPane.getScrollX();
            });
            tagPane.setOverscroll(false, false);
            tagPane.setScrollingDisabled(false, true);
            tagPane.setScrollXForce(tagScrollPos);

            ScrollPane schematicsPane = new ScrollPane(new Table(p -> {
                p.table(tt -> {
                    tt.button("@editor.import", Icon.download, this::showImport).width(160f).height(64f);
                    tt.row();
                    if(schematics.all().isEmpty()) tt.add(bundle.get("none"));

                    for(Schematic schematic : schematics.all()){
                        if(selectedTags.any() && !schematic.labels.containsAll(selectedTags)) continue;

                        Button button = new Button();
                        button.top().margin(0f);
                        button.table(buttons -> {
                            buttons.left();
                            buttons.defaults().size(162/4f);

                            ImageButton.ImageButtonStyle style = Styles.clearNonei;

                            buttons.button(Icon.info, style, () -> showInfo(schematic));
                            buttons.button(Icon.upload, style, () -> showExport(schematic));
                            buttons.button(Icon.pencil, style, () -> {
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
                                        setSchemTable();
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
                            });

                            if(schematic.hasSteamID()) {
                                buttons.button(Icon.link, style, () -> platform.viewListing(schematic));
                            } else {
                                buttons.button(Icon.trash, style, () -> {
                                    if(schematic.mod != null){
                                        ui.showInfo(Core.bundle.format("mod.item.remove", schematic.mod.meta.displayName()));
                                    }else{
                                        ui.showConfirm("@confirm", "@schematic.delete.confirm", () -> {
                                            schematics.remove(schematic);
                                            setSchemTable();
                                        });
                                    }
                                });
                            }

                        }).growX().height(50f);
                        button.row();
                        button.stack(new SchematicsDialog.SchematicImage(schematic).setScaling(Scaling.fit), new Table(n -> {
                            n.top();
                            n.table(Styles.black3, c -> {
                                Label label = c.add(schematic.name()).style(Styles.outlineLabel).color(Color.white).top().growX().maxWidth(200f - 8f).get();
                                label.setEllipsis(true);
                                label.setAlignment(Align.center);
                            }).growX().margin(1).pad(4).maxWidth(Scl.scl(160f - 8f)).padBottom(0);
                        })).size(160f);
                        button.clicked(() -> {
                            if(button.childrenPressed()) return;
                            control.input.useSchematic(schematic);
                        });
                        tt.add(button).pad(4).style(
                             new ImageButton.ImageButtonStyle() {{
                                down = flatDown;
                                up = Tex.pane;
                                over = flatOver;
                                disabled = black8;
                                imageDisabledColor = Color.lightGray;
                                imageUpColor = Color.white;
                            }});
                        tt.row();
                    }

                });
            }), Styles.noBarPane);

            schematicsPane.update(() -> {
                Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                if(schematicsPane.hasScroll() && (result == null || !result.isDescendantOf(schematicsPane)))
                    scene.setScrollFocus(null);
                schemScrollPos = schematicsPane.getScrollY();
            });

            schematicsPane.setOverscroll(false, false);
            schematicsPane.setScrollingDisabled(true, false);
            schematicsPane.setScrollYForce(schemScrollPos);

           t.add(tagPane).fillX().height(42f).row();
           t.add(schematicsPane).grow();
        }, true, () -> schemShown);
    }

    void showInfo(Schematic schematic){
        Reflect.<SchematicsDialog.SchematicInfoDialog>get(ui.schematics, "info").show(schematic);
    }

    void checkTags(Schematic s){
        boolean any = false;
        Seq<String> seq = Reflect.get(ui.schematics, "tags");
        for(var tag : s.labels){
            if(!seq.contains(tag)){
                seq.add(tag);
                any = true;
            }
        }
        if(any) setSchemTable();
    }

    void showImport(){
        BaseDialog dialog = new BaseDialog("@editor.export");
        dialog.cont.pane(p -> {
            p.margin(10f);
            p.table(Tex.button, t -> {
                TextButton.TextButtonStyle style = Styles.cleart;
                t.defaults().size(280f, 60f).left();
                t.row();
                t.button("@schematic.copy.import", Icon.copy, style, () -> {
                    dialog.hide();
                    try{
                        Schematic s = Schematics.readBase64(Core.app.getClipboardText());
                        s.removeSteamID();
                        schematics.add(s);
                        setSchemTable();
                        ui.showInfoFade("@schematic.saved");
                        checkTags(s);
                        showInfo(s);
                    }catch(Throwable e){
                        ui.showException(e);
                    }
                }).marginLeft(12f).disabled(b -> Core.app.getClipboardText() == null || !Core.app.getClipboardText().startsWith(schematicBaseStart));
                t.row();
                t.button("@schematic.importfile", Icon.download, style, () -> platform.showFileChooser(true, schematicExtension, file -> {
                    dialog.hide();

                    try{
                        Schematic s = Schematics.read(file);
                        s.removeSteamID();
                        schematics.add(s);
                        setSchemTable();
                        showInfo(s);
                        checkTags(s);
                    }catch(Exception e){
                        ui.showException(e);
                    }
                })).marginLeft(12f);
                t.row();
                if(steam){
                    t.button("@schematic.browseworkshop", Icon.book, style, () -> {
                        dialog.hide();
                        platform.openWorkshop();
                    }).marginLeft(12f);
                }
            });
        });

        dialog.addCloseButton();
        dialog.show();
    }

    void showExport(Schematic s){
        BaseDialog dialog = new BaseDialog("@editor.export");
        dialog.cont.pane(p -> {
            p.margin(10f);
            p.table(Tex.button, t -> {
                TextButton.TextButtonStyle style = Styles.cleart;
                t.defaults().size(280f, 60f).left();
                if(steam && !s.hasSteamID()){
                    t.button("@schematic.shareworkshop", Icon.book, style,
                            () -> platform.publish(s)).marginLeft(12f);
                    t.row();
                    dialog.hide();
                }
                t.button("@schematic.copy", Icon.copy, style, () -> {
                    dialog.hide();
                    ui.showInfoFade("@copied");
                    Core.app.setClipboardText(schematics.writeBase64(s));
                }).marginLeft(12f);
                t.row();
                t.button("@schematic.exportfile", Icon.export, style, () -> {
                    dialog.hide();
                    platform.export(s.name(), schematicExtension, file -> Schematics.write(s, file));
                }).marginLeft(12f);
            });
        });

        dialog.addCloseButton();
        dialog.show();
    }

    void tagsChanged(){
        rebuildList.run();
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
                                        String out = u.emoji() + "";

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

        Seq<String> finalTags1 = tags;
        t.button(Icon.addSmall, () -> {
            var dialog = new BaseDialog("@schematic.addtag");
            dialog.addCloseButton();
            dialog.cont.pane(p -> {
                p.clearChildren();

                float sum = 0f;
                Table current = new Table().left();
                for(var tag : finalTags1){
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
