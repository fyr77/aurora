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
import ru.game.aurora.application.Localization;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

public class GasGiant extends BasePlanet {
    private static final long serialVersionUID = 1L;

    private PlanetCategory.GasGiantColors color;

    public GasGiant(int x, int y, StarSystem owner, PlanetCategory.GasGiantColors color) {
        super(x, y, 1, owner, PlanetAtmosphere.NO_ATMOSPHERE, PlanetCategory.GAS_GIANT);
        this.color = color;
    }

    public PlanetCategory.GasGiantColors getColor() {
        return color;
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
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
    }

    @Override
    public String getScanText() {
        return Localization.getText("gui", "scan.gas_giant");
    }
}
