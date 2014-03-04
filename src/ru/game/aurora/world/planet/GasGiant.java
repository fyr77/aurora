/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.09.13
 * Time: 18:48
 */
package ru.game.aurora.world.planet;


import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

public class GasGiant extends BasePlanet {
    private static final long serialVersionUID = -4326457278081029734L;

    public GasGiant(int x, int y, StarSystem owner) {
        super(x, y, 1, owner, PlanetAtmosphere.NO_ATMOSPHERE, PlanetCategory.GAS_GIANT);
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
    public void returnTo(World world) {
        enter(world);
    }

    @Override
    public void update(GameContainer container, World world) {
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
    }
}
