/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 11.09.13
 * Time: 17:20
 */
package ru.game.aurora.world.dungeon;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
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
public class DungeonObject extends BasePositionable implements PlanetObject {
    private static final long serialVersionUID = -6444096027905590867L;

    private String name;

    /**
     * Either id of a standalone image, or id of a tile set
     */
    private String imageName;

    /**
     * If not -1 - then imageName is tileset id, and these numbers are indices of image in that tileset
     */
    private int imageTileX = -1;

    private int imageTileY = -1;

    private transient Image image = null;

    public DungeonObject(TiledMap map, int groupId, int objectId) {
        super(map.getObjectX(groupId, objectId) / AuroraGame.tileSize, map.getObjectY(groupId, objectId) / AuroraGame.tileSize - 1); // -1 because Y coord in editor starts from 1
        this.imageName = map.getObjectProperty(groupId, objectId, "image", null);
        this.imageTileX = Integer.parseInt(map.getObjectProperty(groupId, objectId, "imageTileX", "-1"));
        this.imageTileY = Integer.parseInt(map.getObjectProperty(groupId, objectId, "imageTileY", "-1"));
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

    private Image getImage() {
        if (imageTileX < 0) {
            //this is a standalone image
            return ResourceManager.getInstance().getImage(imageName);
        } else {
            return ResourceManager.getInstance().getSpriteSheet(imageName).getSprite(imageTileX, imageTileY);
        }
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        if (image == null) {
            image = getImage();
        }
        graphics.drawImage(image, camera.getXCoord(x), camera.getYCoord(y));
    }

}
