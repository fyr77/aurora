/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 13.08.13
 * Time: 17:53
 */
package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.Image;

import java.io.Serializable;


public class PlantSpeciesDesc implements Serializable
{
    private static final long serialVersionUID = 1l;

    private transient Image image;

    public void setImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }
}
