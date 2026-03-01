package informatis;

import arc.Events;
import informatis.shaders.*;
import arc.graphics.g2d.TextureRegion;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.game.EventType;
import mindustry.type.UnitType;

import static arc.Core.atlas;
import static mindustry.Vars.content;

public class SVars {
    public static final TextureRegion clear = atlas.find("clear");
    public static final TextureRegion error = atlas.find("error");
    public static informatis.core.Pathfinder pathfinder;

    public static void init() {
        pathfinder = new informatis.core.Pathfinder();
    }

    public static float maxShieldAmongUnits = 0;

    static {
        Events.on(EventType.ClientLoadEvent.class, e -> {
            for(UnitType unitType : content.units()) {
                ShieldRegenFieldAbility ability = (ShieldRegenFieldAbility) unitType.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility);
                if(ability == null) continue;
                if(ability.max > maxShieldAmongUnits) maxShieldAmongUnits = ability.max;
            }
        });
    }
}