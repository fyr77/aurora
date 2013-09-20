/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.09.13
 * Time: 18:48
 */
package ru.game.aurora.world.planet;


import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

public class GasGiant extends BasePlanet
{
    private static final long serialVersionUID = -4326457278081029734L;

    private transient Image sprite;

    public GasGiant(int x, int y, StarSystem owner) {
        super(1, y, owner, PlanetAtmosphere.NO_ATMOSPHERE, x, PlanetCategory.GAS_GIANT);
    }

    @Override
    public void drawOnGlobalMap(GameContainer container, Graphics graphics, Camera camera, int tileX, int tileY) {
        if (!camera.isInViewport(globalX, globalY)) {
            return;
        }
        if (sprite == null) {
            sprite = PlanetSpriteGenerator.getInstance().createPlanetSprite(camera, category, size, false);
        }
        graphics.drawImage(sprite, camera.getXCoord(globalX) + (camera.getTileWidth() - sprite.getWidth()) / 2, camera.getYCoord(globalY) + (camera.getTileHeight() - sprite.getHeight()) / 2);

    }


    @Override
    public boolean canBeEntered() {
        return false;
    }

    @Override
    public boolean canBeLanded() {
        return false;
    }

    @Override
    public void enter(World world) {
    }

    @Override
    public void update(GameContainer container, World world) {
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
    }
}
