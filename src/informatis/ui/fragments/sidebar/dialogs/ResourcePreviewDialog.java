package informatis.ui.fragments.sidebar.dialogs;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.Elem;
import arc.struct.*;
import arc.util.*;
import informatis.ui.components.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import java.lang.reflect.Field;
import java.util.Objects;

public class ResourcePreviewDialog extends BaseDialog {
    private final PageTabsFragment tabsFragment = new PageTabsFragment(
            "Textures", new TexturePreviewFragment(),
            "Styles", new StylePreviewFragment(),
            "Colors", new ColorPreviewFragment()
    );

    public ResourcePreviewDialog() {
        super("resource previews");
        setFillParent(true);
        addCloseButton();

        cont.add(tabsFragment).growX();
        cont.row();
        cont.add(tabsFragment.content).grow();
    }
}

class TexturePreviewFragment extends Table {
    boolean showName = false;
    String search = "";

    private final TabsFragment tabsFragment = new TabsFragment("Texture Resources", "Icon Resources");

    public TexturePreviewFragment() {
        super();

        ScrollPane pane = new ScrollPane(tabsFragment.currentTabIndex == 0 ? buildTexResources() : buildIconResources());
        refreshPane(pane);

        add(tabsFragment).growX();
        row();
        table(options -> {
            options.top().center();
            options.table(t -> {
                t.button(Icon.zoom, () -> refreshPane(pane));
                t.field(search, value -> search = value).get().keyDown(KeyCode.enter, () -> refreshPane(pane));
            }).minWidth(200).pad(20);
            options.check("show resource with its name", showName, (checkBox) -> {
                showName = !showName;
                refreshPane(pane);
            }).pad(20);
        }).padBottom(20f).growX();
        row();
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
    private final PageTabsFragment tabsFragment = new PageTabsFragment(
            "Drawable", buildStyleTable(Drawable.class),
            "ButtonStyle", buildStyleTable(Button.ButtonStyle.class),
            "TextButtonStyle", buildStyleTable(TextButton.TextButtonStyle.class),
            "ImageButtonsStyle", buildStyleTable(ImageButton.ImageButtonStyle.class)
    );

    public StylePreviewFragment() {
        super();

        add(tabsFragment).growX();
        row();
        pane(tabsFragment.content).grow();
    }

    private Table buildStyleTable(Class<?> styleClass) {
        return new Table(table -> {
            table.top().left().defaults().labelAlign(Align.center).center().maxHeight(50).pad(10).grow();

            Seq<Field> defaultStyles = Seq.select(Styles.class.getFields(), field -> field.getType().equals(styleClass));
            if (styleClass.equals(Drawable.class)) {
                for (Field field : defaultStyles) {
                    table.table(tt -> {
                        tt.left();
                        tt.add(field.getName());
                        tt.image(Reflect.<Drawable>get(field)).grow().padLeft(40f);
                    });
                    table.row();
                }
                return;
            }

            Seq<Field> styleFields = Seq.select(styleClass.getFields(), style -> defaultStyles.contains(field -> {
                Object value = Reflect.get(Reflect.get(field), style);
                return !(value == null || value instanceof Font || value.toString().matches("^\\d*.\\d*$")); //wtf
            }));

            table.add("styles\\fields").center();
            styleFields.each(style -> table.table(tt -> {
                tt.center();
                tt.add(style.getName());
            }));
            table.row();
            table.image().height(4f).color(Pal.accent).growX().colspan(styleFields.size + 1).row();
            for (Field defaultStyle : defaultStyles) {
                table.add(defaultStyle.getName());
                styleFields.each(styleField -> {
                    table.table(tt -> {
                        Object value = Reflect.get(Reflect.get(defaultStyle), styleField); //<defaultStyle>.<styleField>  ex) <Styles.clearNonei>.<imageUpColor>
                        tt.center();
                        if (value == null) tt.add("");
                        else if (value instanceof Drawable drawable) tt.image(drawable).grow();
                        else tt.add(value.toString()).color(value.toString().matches("^#?[a-fA-F0-9]{6,8}$")
                            ? Color.valueOf(value.toString())
                            : Color.gray
                        );
                    });
                });
                table.row();
                table.image().height(4f).color(Pal.gray).growX().colspan(styleFields.size + 1);
                table.row();
            }
        });
    }
}

class ColorPreviewFragment extends Table {
    private final PageTabsFragment tabsFragment = new PageTabsFragment(
            "Colors", buildColors(Color.class),
            "Pal", buildColors(Pal.class)
    );
    private final ColorMixer colorMixer = new ColorMixer();

    public ColorPreviewFragment() {
        super();

        add(colorMixer).growX();
        row();
        add(tabsFragment).growX();
        row();
        pane(tabsFragment.content).grow();
    }

    Table buildColors(Class<?> colorClass) {
        final String[] cachedId = {""};
        return new IntervalTableWrapper(t -> {
            t.top().left().defaults().maxWidth(300).pad(20);

            float sum = 0;
            for(Field colorField : colorClass.getDeclaredFields()) {
                if(!colorField.getType().equals(Color.class)) continue;

                Color color = Reflect.get(colorField);
                sum += t.table(colorCell -> {
                    colorCell.left();
                    colorCell.image().size(30).color(color);
                    colorCell.add(colorField.getName()).padLeft(10);
                    colorCell.clicked(() -> {
                        if(colorMixer.currentField != null) colorMixer.currentField.setter.get(color);
                    });
                }).tooltip("#" + color.toString()).growX().maxWidth();

                if(sum >= getWidth()) {
                    sum -= getWidth();
                    t.row();
                }
            }
        }, table -> {
            if(!Objects.equals(cachedId[0], getWidth() + ":" + tabsFragment.currentTabIndex)) {
                cachedId[0] = getWidth()+":"+tabsFragment.currentTabIndex;
                return true;
            }
            return false;
        }, 60).build();
    }

    static class ColorMixer extends Table {
        @Nullable
        public FieldColorLabel currentField;
        public float colorMixProgress = 0;
        public Color color1 = Color.white, color2 = Color.white;

        public ColorMixer() {
            top().center().defaults().pad(20);

            table(table -> {
                table.add(new FieldColorLabel(() -> color1, color -> color1 = color));
                table.slider(0, 100, 1, 0, progress -> colorMixProgress = progress);
                table.add(new FieldColorLabel(() -> color2, color -> color2 = color));
            });
            row();
            add(new ColorLabel(() -> color1.cpy().lerp(color2, colorMixProgress / 100)));
        }

        class FieldColorLabel extends ColorLabel {
            public final Cons<Color> setter;

            public FieldColorLabel(Prov<Color> color, Cons<Color> setter) {
                super(color);
                this.setter = setter;
            }

            protected Image buildImage() {
                return new Image() {
                    @Override
                    public void draw() {
                        this.setColor(FieldColorLabel.this.color.get());
                        super.draw();
                        int size = 8;
                        Draw.color(currentField == FieldColorLabel.this ? Pal.accent : Pal.gray);
                        Draw.alpha(parentAlpha);
                        Lines.stroke(Scl.scl(3f));
                        Lines.rect(x - size / 2f, y - size / 2f, width + size, height + size);
                        Draw.reset();
                    }
                    {
                        clicked(() -> {
                            if(currentField == FieldColorLabel.this) currentField = null;
                            else currentField = FieldColorLabel.this;
                        });
                    }
                };
            }

            protected Element buildLabel() {
                TextField field = Elem.newField(color.get().toString(), (value) -> setter.get(Color.valueOf(value.matches("^#?[a-fA-F0-9]{6,8}$") ? value : "ffffff")));
                field.update(() -> {
                    field.setText(color.get().toString());
                });
                return field;
            }
        }

        static class ColorLabel extends Table {
            protected final Prov<Color> color;

            public ColorLabel(Prov<Color> color) {
                this.color = color;
                add(buildImage()).size(30).pad(10);
                add(buildLabel());
            }

            protected Image buildImage() {
                return new Image(){
                    @Override
                    public void draw() {
                        this.setColor(ColorLabel.this.color.get());
                        super.draw();
                    }
                };
            }

            protected Element buildLabel() {
                return new Label(() -> color.get().toString());
            }
        }
    }
}
