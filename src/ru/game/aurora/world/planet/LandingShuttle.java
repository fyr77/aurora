package ru.game.aurora.world.planet;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 11.09.13
 * Time: 12:47
 */

public class LandingShuttle extends BasePositionable implements PlanetObject
{
    private static final long serialVersionUID = -6432856422423659187L;

    private Planet myPlanet;

    public LandingShuttle(Planet planet, int x, int y) {
        super(x, y);
        this.myPlanet = planet;
    }

    @Override
    public ScanGroup getScanGroup() {
        return null;
    }

    @Override
    public boolean canBePickedUp() {
        return true;
    }

    @Override
    public boolean canBeShotAt() {
        return false;
    }

    @Override
    public void onShotAt(World world, int damage) {
    }

    @Override
    public void onPickedUp(World world) {
        myPlanet.leavePlanet(world);
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public String getName() {
        return "Shuttle";
    }

    @Override
    public void printStatusInfo() {
    }

    @Override
    public void update(GameContainer container, World world) {
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        graphics.drawImage(ResourceManager.getInstance().getImage("shuttle"), camera.getXCoordWrapped(x, myPlanet.getWidth()), camera.getYCoordWrapped(y, myPlanet.getHeight()));
    }
}
