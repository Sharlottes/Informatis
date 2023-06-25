package informatis.ui.windows.barDataCollectors;

import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import informatis.SVars;
import informatis.ui.components.SBar;

public class BarData extends SBar.SBarData {
    public final TextureRegion icon;
    public final Prov<String> nameProv;

    public BarData(Prov<String> nameProv, Color fromColor, Color toColor, Floatp fraction, TextureRegion icon) {
        super(nameProv.get(), fromColor, toColor, fraction);
        this.nameProv = nameProv;
        this.icon = icon;
    }

    public BarData(Prov<String> nameProv, Color fromColor, Color toColor, Floatp fraction) {
        this(nameProv, fromColor, toColor, fraction, SVars.clear);
    }

    public BarData(Prov<String> nameProv, Color color, Floatp fraction) {
        this(nameProv, color, color, fraction, SVars.clear);
    }

    public BarData(Prov<String> nameProv, Color color, Floatp fraction, TextureRegion icon) {
        this(nameProv, color, color, fraction, icon);
    }
}