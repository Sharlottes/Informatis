package informatis.ui.dialogs;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import arc.util.Scaling;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.*;

import java.lang.reflect.Field;


public class ResourcePreviewDialog extends BaseDialog {
    boolean showName = false;
    int currentTab = 0;

    public ResourcePreviewDialog() {
        super("resource previews");
        setFillParent(true);
        addCloseButton();

        cont.table(t -> {
            t.top().center();
            t.table(Tex.underline, tabTable -> {
                String[] tabs = {"Textures", "Styles"};
                for(int i = 0; i < tabs.length; i++) {
                    int j = i;
                    TextButton button = new TextButton(tabs[j], Styles.flatToggleMenut);
                    button.clicked(() -> {
                        currentTab = j;
                        button.toggle();
                        for(int elemI = 0; elemI < tabTable.getChildren().size; elemI++) {
                            ((Button) tabTable.getChildren().get(elemI)).setChecked(currentTab == elemI);
                        }
                        refreshResourceTable();
                    });
                    tabTable.add(button).minHeight(50).grow();
                }
            }).grow().row();
            t.table(tt -> tt.add(rebuildResourceList())).name("resource").grow();
        }).grow();
    }
    
    void refreshResourceTable() {
        Table resource = find("resource");
        resource.clearChildren();
        resource.add(rebuildResourceList());
    }

    float scrollY;
    Table rebuildResourceList() {
        return new Table(pane -> {
            Cons[] builders = {
                (Cons<Table>) ppane -> {
                    ppane.table(options -> {
                        options.top().left();
                        options.check("show resource with its name", showName, (checkBox) -> {
                            showName = !showName;
                            refreshResourceTable();
                        }).grow();
                    }).padBottom(20f).growX().row();
                    ScrollPane contentPane = new ScrollPane(new Table(content -> {
                        buildTitle(content, "Texture Resources").row();
                        buildTexResources(content).row();
                        buildTitle(content, "Icon Resources").row();
                        buildIconResources(content);
                    }));
                    contentPane.scrolled(y -> {
                        scrollY = contentPane.getScrollY();
                    });
                    contentPane.layout();
                    contentPane.setScrollY(scrollY);
                    ppane.add(contentPane).grow();
                },
                (Cons<Table>) ppane -> {
                    buildTitle(ppane, "Styles Resources").row();
                    ppane.pane(this::buildStyleResources).scrollX(true);
                }
            };
            pane.table(ppane -> {
                ppane.left().top();
                builders[currentTab].get(ppane);
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
                                    ? value.toString().matches("/[a-f0-9]{8}/gi") //TODO - 이거 왜 안먹힘
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
            addResourceImage(table, entry.value, entry.key, 0);
            if(++i % 15 == 0) table.row();
        }
        return table;
    }

    Table buildTexResources(Table table) {
        Field[] fields = Tex.class.getDeclaredFields();
        for(int i = 0; i < fields.length;) {
            try {
                Field field = fields[i];
                addResourceImage(table, (Drawable) field.get(null), field.getName(), i);
                if(++i % 15 == 0) table.row();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return table;
    }

    void addResourceImage(Table table, Drawable res, String name, int i) {
        table.table(t -> {
            t.center().defaults();

            Image image = new Image(res).setScaling(Scaling.bounded);
            Label label = new Label(name);
            label.setWidth(100);
            label.setFontScale(0.75f);
            label.setAlignment(Align.center);
            if(showName) {
                t.stack(
                    new Table(tt -> {
                        tt.center();
                        tt.add(image).size(100);
                    }),
                    new Table(tt -> {
                        tt.center();
                        tt.addChild(label);
                        label.setPosition(t.x + t.getWidth() / 2, label.y + (name.length() >= 13 && i % 2 == 0 ? -label.getHeight() * 0.9f : 0));
                        tt.pack();
                    })
                ).center().maxSize(100);
            }
            else t.add(image).size(100);
        }).size(100).pad(10).tooltip(name);
    }
}
