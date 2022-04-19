package unitinfo.ui.draws;

import arc.scene.Element;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.CheckBox;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.ui.Styles;

import static arc.Core.bundle;
import static arc.Core.settings;

public class OverDraw {
    public TextureRegionDrawable icon;
    public String name;
    public boolean enabled = false;
    public Seq<String> options = new Seq<>();


    OverDraw(String name, TextureRegionDrawable icon) {
        this.name = name;
        this.icon = icon;
    }

    public void displayStats(Table parent) {
        if(options.isEmpty()) return;
        parent.background(Styles.squaret.up);

        options.each(name-> parent.check(bundle.get("setting."+name+".name"), settings.getBool(name), b->settings.put(name, b)).tooltip(t->t.background(Styles.black8).add(bundle.get("setting."+name+".description"))).disabled(!enabled).row());
    }

    public void draw() {}

    public <T> void onEnabled(T param) {
        if(param instanceof Table t) {
            for (int i = 0; i < t.getChildren().size; i++) {
                Element elem = t.getChildren().get(i);
                if (elem instanceof CheckBox cb) cb.setDisabled(!enabled);
            }
        }
    }

    public void registerOption(String name) {
        registerOption(name, settings.has(name) && settings.getBool(name));
    }

    public void registerOption(String name, boolean defaults) {
        options.add(name);
        settings.put(name, defaults);
    }
}

