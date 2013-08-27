/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 13.08.13
 * Time: 17:53
 */
package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.Image;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.SurfaceTypes;

import java.io.Serializable;


public class PlantSpeciesDesc implements Serializable
{
    private static final long serialVersionUID = 1l;

    private transient Image image;

    /**
     * This plant will grow only on this type of tiles
     */
    private byte preferredSurfaceType;

    /**
     * This plant will grow somewhere around given latitude
     * Value from 0 (Equator) to 1 (Pole)
     */
    private double preferredLatitude;

    private double areaHeight;

    private String name = "Unknown alien plant";

    // probability of this plant to be generated on a tile, if all other conditions match
    private double baseProbability;

    private boolean growsOnWater = false;

    private boolean growsOnMountains = false;

    public PlantSpeciesDesc(byte preferredSurfaceType, double preferredLatitude, double areaHeight, double baseProbability, boolean growsOnWater, boolean growsOnMountains)
    {
        this.preferredSurfaceType = preferredSurfaceType;
        this.preferredLatitude = preferredLatitude;
        this.areaHeight = areaHeight;
        this.baseProbability = baseProbability;
        this.growsOnWater = growsOnWater;
        this.growsOnMountains = growsOnMountains;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        if (image == null) {
            AnimalGenerator.getInstance().getImageForPlant(this);
        }
        return image;
    }

    public byte getPreferredSurfaceType() {
        return preferredSurfaceType;
    }

    public double getBaseProbability() {
        return baseProbability;
    }

    public String getName() {
        return name;
    }

    public boolean canPlantOnTile(int x, int y, Planet planet)
    {
        byte value = planet.getTileTypeAt(x, y);

        return y > planet.getHeight() * preferredLatitude
                && y <= planet.getHeight() * (preferredLatitude + areaHeight)
                && SurfaceTypes.sameBaseSurfaceType(value, preferredSurfaceType)
                && !(SurfaceTypes.getType(value) == SurfaceTypes.WATER && !growsOnWater)
                && !(SurfaceTypes.isMountain(value) && !growsOnMountains);

    }
}
