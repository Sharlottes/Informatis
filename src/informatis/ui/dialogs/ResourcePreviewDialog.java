package informatis.ui.dialogs;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import informatis.ui.components.TabsFragment;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import java.lang.reflect.Field;

public class ResourcePreviewDialog extends BaseDialog {
    private final TabsFragment tabsFragment = new TabsFragment("Textures", "Styles", "Colors");
    private final Table[] fragments = new Table[]{ new TexturePreviewFragment(), new StylePreviewFragment(), new ColorPreviewFragment() };

    public ResourcePreviewDialog() {
        super("resource previews");
        setFillParent(true);
        addCloseButton();

        cont.add(tabsFragment).growX();
        cont.row();
        Table table = cont.table(t -> t.add(fragments[tabsFragment.currentTabIndex]).grow()).grow().get();

        tabsFragment.eventEmitter.subscribe(TabsFragment.Event.TabChanged, () -> {
            table.clearChildren();
            table.add(fragments[tabsFragment.currentTabIndex]).grow();
        });
    }
}

class TexturePreviewFragment extends Table {
    boolean showName = false;
    String search = "";

    private final TabsFragment tabsFragment = new TabsFragment("Texture Resources", "Icon Resources");

    public TexturePreviewFragment() {
        super();

        ScrollPane pane = new ScrollPane(tabsFragment.currentTabIndex == 0 ? buildTexResources() : buildIconResources());
        add(tabsFragment).growX();
        row();
        table(options -> {
            top().center();
            table(t -> {
                t.button(Icon.zoom, () -> refreshPane(pane));
                t.field(search, value -> search = value).get().keyDown(KeyCode.enter, () -> refreshPane(pane));
            }).minWidth(200).pad(20);
            check("show resource with its name", showName, (checkBox) -> {
                showName = !showName;
                refreshPane(pane);
            }).pad(20);
        }).padBottom(20f).growX().row();

        refreshPane(pane);
        add(pane).grow();

        tabsFragment.eventEmitter.subscribe(TabsFragment.Event.TabChanged, () -> refreshPane(pane));
    }

    void refreshPane(ScrollPane pane) {
        pane.setWidget(tabsFragment.currentTabIndex == 0 ? buildTexResources() : buildIconResources());
    }

    Table buildIconResources() {
        return new Table(table -> {
            int r = 0;
            for(ObjectMap.Entry<String, TextureRegionDrawable> entry : Icon.icons.entries()) {
                if(!entry.key.contains(search)) continue;
                addResourceImage(table, entry.value, entry.key, 0);
                if(++r % 15 == 0) table.row();
            }
        });
    }

    Table buildTexResources() {
        return new Table(table -> {
            Field[] fields = Tex.class.getDeclaredFields();
            int r = 0;
            for(int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if(!field.getName().contains(search)) continue;
                addResourceImage(table, Reflect.get(field), field.getName(), i);
                if(++r % 15 == 0) table.row();
            }
        });
    }

    void addResourceImage(Table table, Drawable res, String name, int i) {
        table.table(t -> {
            t.center().defaults();

            Image image = new Image(res).setScaling(Scaling.bounded);
            if(!showName) {
                t.add(image).size(100);
            } else {
                Label label = new Label(name);
                label.setWidth(100);
                label.setFontScale(0.75f);
                label.setAlignment(Align.center);
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
        }).size(100).pad(10).tooltip(name);
    }
}

class StylePreviewFragment extends Table {
    private static final Class<?>[] styleClasses = { Drawable.class, Button.ButtonStyle.class, TextButton.TextButtonStyle.class, ImageButton.ImageButtonStyle.class };
    private final TabsFragment tabsFragment = new TabsFragment("Drawable", "ButtonStyle", "TextButtonStyle", "ImageButtonsStyle");

    public StylePreviewFragment() {
        super();

        add(tabsFragment).growX();
        row();
        ScrollPane pane = pane(buildStyleTable(styleClasses[tabsFragment.currentTabIndex])).grow().get();
        tabsFragment.eventEmitter.subscribe(TabsFragment.Event.TabChanged, () ->
                pane.setWidget(buildStyleTable(styleClasses[tabsFragment.currentTabIndex]))
        );
    }

    private Table buildStyleTable(Class<?> classz) {
        return new Table(table -> {
            table.top().left().defaults().labelAlign(Align.center).center().maxHeight(50).pad(10).grow();

            Seq<Field> allStyles = Seq.select(Styles.class.getFields(), field -> field.getType().equals(classz));
            if (classz.equals(Drawable.class)) {
                for (Field field : allStyles) {
                    table.table(tt -> {
                        tt.left();
                        tt.add(field.getName());
                        tt.image(Reflect.<Drawable>get(field)).grow().padLeft(40f);
                    });
                    table.row();
                }
                return;
            }

            Seq<Field> stylesWithoutNumeric = Seq.select(classz.getFields(), style -> allStyles.contains(field -> {
                Object value = Reflect.get(Reflect.get(field), style);
                return !(value == null || value instanceof Font || value.toString().matches("^\\d*.\\d*$")); //wtf
            }));

            stylesWithoutNumeric.each(style -> table.table(tt -> {
                tt.center();
                tt.add(style.getName());
            }));
            table.row();
            table.image().height(4f).color(Pal.accent).growX().colspan(stylesWithoutNumeric.size + 1).row();
            for (Field field : allStyles) {
                try {
                    table.add(field.getName());
                    Object style = field.get(null);
                    stylesWithoutNumeric.each(styleField -> {
                        Object value =Reflect.get(style, styleField);
                        table.table(tt -> {
                            tt.center();
                            if (value == null) tt.add("").color(Color.gray);
                            else if (value instanceof Drawable drawable) tt.image(drawable).grow();
                            else tt
                                        .add(value.toString())
                                        .color(value.toString().matches("^#?[a-fA-F0-9]{6,8}$")
                                                ? Color.valueOf(value.toString())
                                                : Color.gray
                                        );
                        });
                    });
                    table.row();
                    //divider smh?
                    table.image().height(4f).color(Pal.gray).growX().colspan(stylesWithoutNumeric.size + 1).row();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}

class ColorPreviewFragment extends Table {
    private static final Class<?>[] colorClasses = { Color.class, Pal.class };
    private final TabsFragment tabsFragment = new TabsFragment("Colors", "Pal");
    private final ColorMixer colorMixer = new ColorMixer();

    public ColorPreviewFragment() {
        super();

        add(colorMixer).growX();
        row();
        add(tabsFragment).growX();
        row();
        ScrollPane pane = new ScrollPane(buildColors(colorClasses[tabsFragment.currentTabIndex]));
        add(pane).grow();
        tabsFragment.eventEmitter.subscribe(TabsFragment.Event.TabChanged, () -> pane.setWidget(buildColors(colorClasses[tabsFragment.currentTabIndex])));
    }

    Table buildColors(Class<?> target) {
        return new Table(t -> {
            t.top().left();
            Field[] palFields = target.getDeclaredFields();
            int row = 0;
            for(Field palField : palFields) {
                Object obj = Reflect.get(palField);
                if(!(obj instanceof Color color)) continue;

                t.table(colorCell -> {
                    colorCell.left();
                    colorCell.image().size(30).color(color).tooltip("#" + color.toString());
                    colorCell.add(palField.getName()).padLeft(10);
                }).maxWidth(300).growX().pad(20).get().clicked(() -> {
                    if(colorMixer.colorMixSelectIndex == 0) {
                        colorMixer.colorInput1 = color.toString();
                        colorMixer.color1 = color;
                    } else {
                        colorMixer.colorInput2 = color.toString();
                        colorMixer.color2 = color;
                    }
                    colorMixer.mixedColor = colorMixer.color1.cpy().lerp(colorMixer.color2, colorMixer.colorMixProg / 100);
                    //refreshPane();
                });
                if(++row % 8 == 0) t.row();
            }
        });
    }

    class ColorMixer extends Table {
        public String colorInput1 = "ffffffff", colorInput2 = "ffffffff";
        public float colorMixProg = 0;
        public int colorMixSelectIndex = 0;
        public Color color1 = Color.white, color2 = Color.white, mixedColor = Color.white;

        public ColorMixer() {

            top().center().defaults().pad(20);

            add("Mix");
            add(new Image() {
                @Override
                public void draw() {
                    super.draw();

                    int size = 8;
                    Draw.color(colorMixSelectIndex == 0 ? Pal.accent : Pal.gray);
                    Draw.alpha(parentAlpha);
                    Lines.stroke(Scl.scl(3f));
                    Lines.rect(x - size / 2f, y - size / 2f, width + size, height + size);
                    Draw.reset();
                }
            }).size(30).color(color1).pad(10).get().clicked(() -> colorMixSelectIndex = 0);
            field(colorInput1, field -> {
                colorInput1 = field;
                color1 = Color.valueOf(field.matches("^#?[a-fA-F0-9]{6,8}$") ? field : "ffffff");
            });
            slider(0, 100, 1, colorMixProg, prog -> {
                colorMixProg = prog;
                mixedColor = color1.cpy().lerp(color2, prog / 100);
            });
            add(new Image() {
                @Override
                public void draw() {
                    super.draw();

                    int size = 8;
                    Draw.color(colorMixSelectIndex == 1 ? Pal.accent : Pal.gray);
                    Draw.alpha(parentAlpha);
                    Lines.stroke(Scl.scl(3f));
                    Lines.rect(x - size / 2f, y - size / 2f, width + size, height + size);
                    Draw.reset();
                }
            }).size(30).color(color2).pad(10).get().clicked(() -> colorMixSelectIndex = 1);
            field(colorInput2, field -> {
                colorInput2 = field;
                color2 = Color.valueOf(field.matches("^#?[a-fA-F0-9]{6,8}$") ? field : "ffffff");
            });
            row();
            add(new Image(){
                @Override
                public void draw() {
                    this.setColor(mixedColor);
                    super.draw();
                }
            }).size(30).pad(10);
            label(() -> mixedColor.toString());
        }
    }
}