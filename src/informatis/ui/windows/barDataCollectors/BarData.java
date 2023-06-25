package informatis.ui.windows.barDataCollectors;

import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;

import static informatis.SVars.clear;

public
class BarData {
    public Prov<String> name;
    public Prov<Color> color;
    public Prov<Float> number;
    public Prov<TextureRegion> icon;


    BarData(Prov<String> name, Prov<Color> color, Prov<Float> number) {
        this(name, color, number, () -> clear);
    }

    BarData(Prov<String> name, Prov<Color> color, Prov<Float> number, Prov<TextureRegion> icon) {
        this.name = name;
        this.color = color;
        this.number = number;
        this.icon = icon;
    }
}