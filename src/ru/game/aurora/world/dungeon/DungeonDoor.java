package ru.game.aurora.world.dungeon;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.tiled.TiledMap;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.AuroraTiledMap;
import ru.game.aurora.world.World;

/**
 * Date: 06.10.13
 * Time: 10:41
 */
public class DungeonDoor extends DungeonObject {
    private static final long serialVersionUID = -1572145819737469957L;

    private final String imageOpenName;
    private int imageOpenTileX = -1;
    private int imageOpenTileY = -1;
    private transient Image imageOpen = null;

    private final String imageCloseName;
    private int imageCloseTileX = -1;
    private int imageCloseTileY = -1;
    private transient Image imageClose = null;

    //true for opened, false for closed
    private boolean state;

    public String getDoorName() {
        return doorName;
    }

    private final String doorName;

    public DungeonDoor(AuroraTiledMap map, int groupId, int objectId) {
        super(map, groupId, objectId);
        TiledMap impl = map.getMap();
        state = !impl.getObjectProperty(groupId, objectId, "initialState", "closed").equals("closed");

        this.imageOpenName = impl.getObjectProperty(groupId, objectId, "imageOpen", null);
        this.imageOpenTileX = Integer.parseInt(impl.getObjectProperty(groupId, objectId, "imageOpenTileX", "-1"));
        this.imageOpenTileY = Integer.parseInt(impl.getObjectProperty(groupId, objectId, "imageOpenTileY", "-1"));

        this.imageCloseName = impl.getObjectProperty(groupId, objectId, "imageClose", null);
        this.imageCloseTileX = Integer.parseInt(impl.getObjectProperty(groupId, objectId, "imageCloseTileX", "-1"));
        this.imageCloseTileY = Integer.parseInt(impl.getObjectProperty(groupId, objectId, "imageCloseTileY", "-1"));

        this.doorName = impl.getObjectName(groupId, objectId);
    }

    @Override
    public void update(GameContainer container, World world) {
        if (!world.isUpdatedThisFrame()) {
            return;
        }
        world.getCurrentDungeon().getMap().setTilePassable(x, y, state);
    }

    private Image getImageOpen() {
        if (imageOpenTileX < 0) {
            //this is a standalone image
            return ResourceManager.getInstance().getImage(imageOpenName);
        } else {
            return ResourceManager.getInstance().getSpriteSheet(imageOpenName).getSprite(imageOpenTileX, imageOpenTileY);
        }
    }

    private Image getImageClose() {
        if (imageCloseTileX < 0) {
            //this is a standalone image
            return ResourceManager.getInstance().getImage(imageCloseName);
        } else {
            return ResourceManager.getInstance().getSpriteSheet(imageCloseName).getSprite(imageCloseTileX, imageCloseTileY);
        }
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
        if (imageOpen == null) {
            imageOpen = getImageOpen();
        }
        if (imageClose == null) {
            imageClose = getImageClose();
        }
        if (state) {
            graphics.drawImage(imageOpen, camera.getXCoord(x), camera.getYCoord(y));
        } else {
            graphics.drawImage(imageClose, camera.getXCoord(x), camera.getYCoord(y));
        }
    }

    public boolean isOpen() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
