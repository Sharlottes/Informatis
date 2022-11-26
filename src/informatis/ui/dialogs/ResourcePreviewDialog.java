package informatis.ui.dialogs;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.scene.style.Drawable;
import arc.scene.style.Style;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Scaling;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.*;

import java.lang.reflect.Field;
import java.util.Arrays;


public class ResourcePreviewDialog extends BaseDialog {
    boolean showName = false;

    public ResourcePreviewDialog() {
        super("resource previews");
        setFillParent(true);
        addCloseButton();

        cont.table(t -> {
            t.top().center();

            t.table(Tex.underline, options -> {
                options.top().center();
                CheckBox box = new CheckBox("show resource with its name");
                box.changed(() -> {
                    showName = !showName;
                    ((ScrollPane) find("resource-pane")).setWidget(rebuildResourceList());
                });
                options.add(box);
            }).padBottom(50f).growX().row();

            t.pane(rebuildResourceList()).name("resource-pane").grow();
        }).grow();
    }

    Table rebuildResourceList() {
        return new Table(pane -> {
            pane.table(ppane -> {
                ppane.left().top();
                buildTitle(ppane, "Texture Resources").row();
                buildTexResources(ppane).row();
                buildTitle(ppane, "Icon Resources").row();
                buildIconResources(ppane);
            }).grow().row();
            pane.table(stylePane -> {
                stylePane.left();
                buildTitle(stylePane, "Styles Resources").row();
                stylePane.pane(this::buildStyleResources).scrollX(true);
            }).grow();
        });
    }

    Table buildTitle(Table table, String title) {
        table.table(Tex.underline2, tex -> tex.add(title)).pad(30f, 0f, 30f, 0f).left();
        return table;
    }

    Table buildStyleResources(Table table) {
        Cons<Class<?>> build = classz -> {
            String[] spliten = classz.getName().split("\\$");
            buildTitle(table, spliten[spliten.length - 1]).marginLeft(20f).row();

            table.table(t -> {
                t.top().left().defaults().center().maxHeight(50).pad(10).grow();

                if(classz.equals(Drawable.class)) {
                    for(Field field : Styles.class.getFields()) {
                        if (!field.getType().equals(Drawable.class)) continue;
                        t.table(tt -> {
                            tt.left();
                            try {
                                tt.add(field.getName());
                                tt.image((Drawable) field.get(null)).padLeft(40f);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }).growX().left().row();
                    }
                    t.row();
                    return;
                }

                t.add("\\");

                Seq<Field> styles = Seq.with(classz.getFields());
                styles.each(style -> t.add(style.getName()));
                t.row();

                for(Field field : Styles.class.getFields()) {
                    if(!field.getType().equals(classz)) continue;
                    t.add(field.getName());

                    try {
                        Object style = field.get(null);
                        styles.each(styleField -> {
                            styleField.setAccessible(true);
                            try {
                                Object value = styleField.get(style);
                                if(value instanceof Drawable drawable) t.image(drawable);
                                else t
                                .add(value != null
                                    ? value instanceof Font font
                                        ? font.getData().toString()
                                        : value.toString()
                                    : "<empty>"
                                )
                                .color(value != null
                                    ? value.toString().matches("/[a-f|A-F|0-9]{8}/")
                                        ? Color.valueOf(value.toString())
                                        : Color.white
                                    : Color.gray
                                );
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    t.row();
                }
            }).grow().row();
        };

        build.get(Drawable.class);
        build.get(Button.ButtonStyle.class);
        build.get(TextButton.TextButtonStyle.class);
        build.get(ImageButton.ImageButtonStyle.class);
        return table;
    }

    Table buildIconResources(Table table) {
        int i = 0;
        for(ObjectMap.Entry<String, TextureRegionDrawable> entry : Icon.icons.entries()) {
            addResourceImage(table, entry.value, entry.key);
            if(++i % (15 * (showName ? 0.5f : 1)) == 0) table.row();
        }
        return table;
    }

    Table buildTexResources(Table table) {
        Field[] fields = Tex.class.getDeclaredFields();
        for(int i = 0; i < fields.length;) {
            try {
                Field field = fields[i];
                addResourceImage(table, (Drawable) field.get(null), field.getName());
                if(++i % (15 * (showName ? 0.5f : 1)) == 0) table.row();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return table;
    }

    void addResourceImage(Table table, Drawable res, String name) {
        table.table(t -> {
            t.center();
            t.image(res).scaling(Scaling.bounded);
            if(showName) {
                t.row();
                t.add(name);
            }
        }).maxSize(100).pad(10).tooltip(name);
    }
}
