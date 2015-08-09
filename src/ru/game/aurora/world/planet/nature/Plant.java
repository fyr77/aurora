/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.08.13
 * Time: 14:41
 */
package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.World;


public class Plant extends BaseGameObject {
    private static final long serialVersionUID = 1L;

    private PlantSpeciesDesc desc;

    public Plant(int x, int y, PlantSpeciesDesc desc) {
        super(x, y);
        this.desc = desc;
    }

    public PlantSpeciesDesc getDesc() {
        return desc;
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
        if (drawable == null || drawable.getImage() == null) {
            drawable = new Drawable(desc.getImage());
        }
        super.draw(container, graphics, camera, world);
    }

    public void setDesc(PlantSpeciesDesc desc) {
        this.desc = desc;
    }

    @Override
    public String getName() {
        return desc.getName();
    }

    @Override
    public int getDrawOrder() {
        return 100; // plants are always on top
    }
}
