/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 13.08.13
 * Time: 17:53
 */
package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.Image;
import ru.game.aurora.application.CommonRandom;

import java.io.Serializable;


public class PlantSpeciesDesc implements Serializable
{
    private static final long serialVersionUID = 1l;

    private transient Image image;

    private byte preferredSurfaceType;

    private String name = "Unknown alien plant";

    /**
     * If true, this plant grows randomly on its own
     * If false, it grows in large groups
     */
    private boolean isSingle;

    public PlantSpeciesDesc(byte preferredSurfaceType) {
        this.preferredSurfaceType = preferredSurfaceType;
        this.isSingle = CommonRandom.getRandom().nextBoolean();
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

    public boolean isSingle() {
        return isSingle;
    }

    public String getName() {
        return name;
    }
}
