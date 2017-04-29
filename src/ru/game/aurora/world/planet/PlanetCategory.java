/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 19:01
 */
package ru.game.aurora.world.planet;

import org.slf4j.LoggerFactory;
import ru.game.aurora.application.Localization;

public enum PlanetCategory {
    GAS_GIANT("gas_giant"),

    PLANET_FULL_STONE("full_stone", SurfaceTypes.DIRT, SurfaceTypes.ROCKS, SurfaceTypes.STONES),

    PLANET_ROCK("rock", SurfaceTypes.WATER, SurfaceTypes.DIRT, SurfaceTypes.ROCKS, SurfaceTypes.STONES),

    PLANET_ICE("ice", SurfaceTypes.WATER, SurfaceTypes.ICE, SurfaceTypes.STONES, SurfaceTypes.ROCKS, SurfaceTypes.SNOW),

    PLANET_WATER("water", SurfaceTypes.WATER, SurfaceTypes.STONES, SurfaceTypes.DIRT);

    private PlanetCategory(String localizationKey, Byte... availableSurfaceTypes) {
        this.availableSurfaceTypes = availableSurfaceTypes;
        this.localizationKey = localizationKey;
    }

    private final String localizationKey;

    // not byte, as it is used to call generic method selectRandomElement()
    public final Byte[] availableSurfaceTypes;

    public static enum GasGiantColors {
        YELLOW,
        BLUE,
        RED
    }

    public String getLocalizationText(){
        String text = Localization.getText("planets", localizationKey);

        if(text != null){
            return Localization.getText("planets", localizationKey);
        }
        else{
            LoggerFactory.getLogger(PlanetCategory.class).info("Localization text not found from bundle \"planets\", key \"" + localizationKey + "\"");
            return this.toString();
        }
    }
}
