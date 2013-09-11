/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 11.09.13
 * Time: 17:20
 */
package ru.game.aurora.world.dungeon;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.tiled.TiledMap;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.PlanetObject;

/**
 * Person or object that you can meet on planet surface
 */
public class DungeonObject extends BasePositionable implements PlanetObject
{
    private static final long serialVersionUID = -6444096027905590867L;

    private String name;

    private String imageName;

    public DungeonObject(TiledMap map, int groupId, int objectId) {
        super(map.getObjectX(groupId, objectId) / AuroraGame.tileSize, map.getObjectY(groupId, objectId) / AuroraGame.tileSize);
        this.imageName = map.getObjectProperty(groupId, objectId, "image", null);
        this.name = map.getObjectName(groupId, objectId);
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
    public void onShotAt(int damage) {
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
        return name;
    }

    @Override
    public void printStatusInfo() {
    }

    @Override
    public void update(GameContainer container, World world) {
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        graphics.drawImage(ResourceManager.getInstance().getImage(imageName), camera.getXCoord(x), camera.getYCoord(y));
    }

}
