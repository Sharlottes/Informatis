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
import informatis.ui.Updatable;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.Vars.ui;

public class QuickSchemFragment extends Table implements Updatable {
    static float schemScrollPos, tagScrollPos;
    static boolean schemShown;
    static Schematic firstSchematic;
    static final Seq<String> selectedTags = new Seq<>();
    static Runnable rebuildList = () -> {};
    float heat;

    public QuickSchemFragment() {
        setSchemTable();
    }

    @Override
    public void update() {
        heat += Time.delta;
        if(heat>=60f) {
            heat = 0;
            setSchemTable();
        }
    }

    public void setSchemTable() {
       clear();
       if(!settings.getBool(("schem"))) return;
       right();
       button(bundle.get("hud.schematic-list"), Icon.downOpen, Styles.squareTogglet, () -> schemShown = !schemShown).width(160f).height(60f).checked(b -> {
            Image image = (Image)b.getCells().first().get();
            image.setDrawable(schemShown ? Icon.upOpen : Icon.downOpen);
            return schemShown;
        }).row();
       collapser(t -> {
            t.background(Styles.black8).defaults().maxHeight(72 * 8f).maxWidth(160f);
            rebuildList = () -> {
                t.clearChildren();
                ScrollPane pane1 = t.pane(Styles.noBarPane, p -> {
                    p.left().defaults().pad(2).height(42f);
                    try {
                        for(String tag : (Seq<String>)SUtils.invoke(ui.schematics, "tags")){
                            p.button(tag, Styles.togglet, () -> {
                                if(selectedTags.contains(tag)){
                                    selectedTags.remove(tag);
                                }else{
                                    selectedTags.add(tag);
                                }
                                rebuildList.run();
                            }).checked(selectedTags.contains(tag)).with(c -> c.getLabel().setWrap(false));
                        }
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }).fillX().height(42f).get();
                pane1.update(() -> {
                    Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                    if(pane1.hasScroll() && (result == null || !result.isDescendantOf(pane1)))
                        scene.setScrollFocus(null);
                    tagScrollPos = pane1.getScrollX();
                });

                pane1.setOverscroll(false, false);
                pane1.setScrollingDisabled(false, true);
                pane1.setScrollXForce(tagScrollPos);

                t.row();

                ScrollPane pane = t.pane(Styles.noBarPane, p -> {
                    p.table(tt -> {
                        firstSchematic = null;

                        tt.button("@editor.import", Icon.download, this::showImport).width(160f).height(64f).row();
                        for(Schematic s : schematics.all()){
                            if(selectedTags.any() && !s.labels.containsAll(selectedTags)) continue;
                            if(firstSchematic == null) firstSchematic = s;

                            Button[] sel = {null};
                            sel[0] = tt.button(b -> {
                                b.top();
                                b.margin(0f);
                                b.table(buttons -> {
                                    buttons.left();
                                    buttons.defaults().size(162/4f);

                                    ImageButton.ImageButtonStyle style = Styles.clearNonei;

                                    buttons.button(Icon.info, style, () -> showInfo(s));
                                    buttons.button(Icon.upload, style, () -> showExport(s));
                                    buttons.button(Icon.pencil, style, () -> {
                                        new Dialog("@schematic.rename"){{
                                            setFillParent(true);

                                            cont.margin(30);

                                            cont.add("@schematic.tags").padRight(6f);
                                            cont.table(tags -> buildTags(s, tags, false)).maxWidth(400f).fillX().left().row();

                                            cont.margin(30).add("@name").padRight(6f);
                                            TextField nameField = cont.field(s.name(), null).size(400f, 55f).left().get();

                                            cont.row();

                                            cont.margin(30).add("@editor.description").padRight(6f);
                                            TextField descField = cont.area(s.description(), Styles.areaField, t -> {}).size(400f, 140f).left().get();

                                            Runnable accept = () -> {
                                                s.tags.put("name", nameField.getText());
                                                s.tags.put("description", descField.getText());
                                                s.save();
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

                                    if(s.hasSteamID()){
                                        buttons.button(Icon.link, style, () -> platform.viewListing(s));
                                    }else{
                                        buttons.button(Icon.trash, style, () -> {
                                            if(s.mod != null){
                                                ui.showInfo(Core.bundle.format("mod.item.remove", s.mod.meta.displayName()));
                                            }else{
                                                ui.showConfirm("@confirm", "@schematic.delete.confirm", () -> {
                                                    schematics.remove(s);
                                                    setSchemTable();
                                                });
                                            }
                                        });
                                    }

                                }).growX().height(50f);
                                b.row();
                                b.stack(new SchematicsDialog.SchematicImage(s).setScaling(Scaling.fit), new Table(n -> {
                                    n.top();
                                    n.table(Styles.black3, c -> {
                                        Label label = c.add(s.name()).style(Styles.outlineLabel).color(Color.white).top().growX().maxWidth(200f - 8f).get();
                                        label.setEllipsis(true);
                                        label.setAlignment(Align.center);
                                    }).growX().margin(1).pad(4).maxWidth(Scl.scl(160f - 8f)).padBottom(0);
                                })).size(160f);
                            }, () -> {
                                if(sel[0].childrenPressed()) return;
                                control.input.useSchematic(s);

                            }).pad(4).style(Styles.cleari).get();

                            sel[0].getStyle().up = Tex.pane;
                            tt.row();
                        }

                        if(firstSchematic == null){
                            tt.add(bundle.get("none"));
                        }
                    });
                }).grow().get();

                pane.update(() -> {
                    Element result = scene.hit(input.mouseX(), input.mouseY(), true);
                    if(pane.hasScroll() && (result == null || !result.isDescendantOf(pane)))
                        scene.setScrollFocus(null);
                    schemScrollPos = pane.getScrollY();
                });

                pane.setOverscroll(false, false);
                pane.setScrollingDisabled(true, false);
                pane.setScrollYForce(schemScrollPos);
            };
            rebuildList.run();
        }, true, () -> schemShown);
    }

    void showInfo(Schematic schematic){
        try {
            ((SchematicsDialog.SchematicInfoDialog) SUtils.invoke(ui.schematics, "info")).show(schematic);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    void checkTags(Schematic s){
        boolean any = false;
        Seq<String> seq = null;
        try {
            seq = (Seq<String>) SUtils.invoke(ui.schematics, "tags");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        if(seq == null) return;
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
        Seq<String> tags = null;
        try {
            tags = (Seq<String>) SUtils.invoke(ui.schematics, "tags");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        if(tags == null) return;
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
        Seq<String> tags = null;
        try {
            tags = (Seq<String>) SUtils.invoke(ui.schematics, "tags");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        if(tags == null) return;
        Seq<String> finalTags = tags;
        ui.showTextInput("@schematic.addtag", "", "", out -> {
            if(finalTags.contains(out)){
                ui.showInfo("@schematic.tagexists");
            }else{
                finalTags.add(out);
                tagsChanged();
                result.get(out);
            }
        });
    }

    void showNewIconTag(Cons<String> cons){
        Seq<String> tags = null;
        try {
            tags = (Seq<String>) SUtils.invoke(ui.schematics, "tags");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        if(tags == null) return;
        Seq<String> finalTags = tags;
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
                                if(!u.isHidden() && u.unlockedNow() && u.hasEmoji() && !finalTags.contains(u.emoji())){
                                    t.button(new TextureRegionDrawable(u.uiIcon), Styles.cleari, iconMed, () -> {
                                        String out = u.emoji() + "";

                                        finalTags.add(out);
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
        Seq<String> tags = null;
        try {
            tags = (Seq<String>) SUtils.invoke(ui.schematics, "tags");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        if(tags == null) return;

        //sort by order in the main target array. the complexity of this is probably awful
        Seq<String> finalTags = tags;
        schem.labels.sort((Floatf<String>) finalTags::indexOf);

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
