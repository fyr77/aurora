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
 * Date: 22.05.14
 * Time: 14:52
 */
public class BasePlanetObject extends BasePositionable implements PlanetObject
{
    private static final long serialVersionUID = 4531335829876484566L;

    protected String image;

    protected Planet myPlanet;

    protected ScanGroup scanGroup = null;

    public BasePlanetObject(int x, int y, String image, Planet myPlanet) {
        super(x, y);
        this.image = image;
        this.myPlanet = myPlanet;
    }

    public BasePlanetObject(int x, int y, String image, Planet myPlanet, ScanGroup scanGroup) {
        super(x, y);
        this.image = image;
        this.myPlanet = myPlanet;
        this.scanGroup = scanGroup;
    }

    @Override
    public ScanGroup getScanGroup() {
        return scanGroup;
    }

    @Override
    public boolean canBePickedUp() {
        return false;
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
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void printStatusInfo() {
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        graphics.drawImage(ResourceManager.getInstance().getImage(image), camera.getXCoordWrapped(x, myPlanet.getWidth()), camera.getYCoordWrapped(y, myPlanet.getHeight()));
    }

    @Override
    public void update(GameContainer container, World world) {

    }
}
