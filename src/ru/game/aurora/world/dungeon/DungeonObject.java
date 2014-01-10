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
import ru.game.aurora.world.AuroraTiledMap;
import ru.game.aurora.world.Movable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.PlanetObject;

/**
 * Person or object that you can meet on planet surface
 */
public class DungeonObject extends Movable implements PlanetObject {

    private static final long serialVersionUID = 1L;

    protected AuroraTiledMap myMap;

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

    public DungeonObject(AuroraTiledMap map, int groupId, int objectId) {
        super(map.getMap().getObjectX(groupId, objectId) / AuroraGame.tileSize, map.getMap().getObjectY(groupId, objectId) / AuroraGame.tileSize - 1); // -1 because Y coord in editor starts from 1
        myMap = map;
        TiledMap impl = map.getMap();

        this.imageName = impl.getObjectProperty(groupId, objectId, "image", null);
        this.imageTileX = Integer.parseInt(impl.getObjectProperty(groupId, objectId, "imageTileX", "-1"));
        this.imageTileY = Integer.parseInt(impl.getObjectProperty(groupId, objectId, "imageTileY", "-1"));
        this.name = impl.getObjectName(groupId, objectId);
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
        return name;
    }

    @Override
    public void printStatusInfo() {
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
        graphics.drawImage(image, camera.getXCoord(x) + getOffsetX(), camera.getYCoord(y) + getOffsetY());
    }

}
