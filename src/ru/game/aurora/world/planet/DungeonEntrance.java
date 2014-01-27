package ru.game.aurora.world.planet;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.Dungeon;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 09.01.14
 * Time: 15:07
 */
public class DungeonEntrance extends BasePositionable implements PlanetObject
{
    private static final long serialVersionUID = 1L;

    private String sprite;

    private Dungeon dungeon;

    private Planet myPlanet;

    public DungeonEntrance(Planet myPlanet, int x, int y, String sprite, Dungeon dungeon) {
        super(x, y);
        this.myPlanet = myPlanet;
        this.sprite = sprite;
        this.dungeon = dungeon;
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
        dungeon.enter(world);
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public String getName() {
        return "Entrance";
    }

    @Override
    public void printStatusInfo() {
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        final Image image = ResourceManager.getInstance().getImage(sprite);
        graphics.drawImage(image, camera.getXCoordWrapped(x, myPlanet.getWidth()) - image.getWidth() / 2, camera.getYCoordWrapped(y, myPlanet.getHeight()) - image.getHeight() / 2);
    }

    @Override
    public void update(GameContainer container, World world) {

    }
}