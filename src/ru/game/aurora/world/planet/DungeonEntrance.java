package ru.game.aurora.world.planet;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.Dungeon;
import ru.game.aurora.world.ScanGroup;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 09.01.14
 * Time: 15:07
 */
public class DungeonEntrance extends BaseGameObject {
    private static final long serialVersionUID = 2L;

    private final String sprite;

    private final Dungeon dungeon;

    private final Planet myPlanet;

    private boolean canBeEntered = true;

    private String messageIdIfCanNotEnter = null;

    public DungeonEntrance(Planet myPlanet, int x, int y, String sprite, Dungeon dungeon) {
        super(x, y);
        this.myPlanet = myPlanet;
        this.sprite = sprite;
        this.dungeon = dungeon;
    }

    @Override
    public ScanGroup getScanGroup() {
        return ScanGroup.OTHER;
    }


    /**
     * Locks this dungeon. Player can not enter it, and is shown a message instead
     */
    public void setLocked(String messageId) {
        canBeEntered = false;
        messageIdIfCanNotEnter = messageId;
    }

    @Override
    public boolean canBeInteracted(World world) {
        return true;
    }

    @Override
    public boolean interact(World world) {
        if (canBeEntered) {
            if (world.getCurrentRoom() instanceof Planet) {
                dungeon.getMap().setUserData(world.getCurrentRoom());
            }
            dungeon.enter(world);
        } else {
            GameLogger.getInstance().logMessage(Localization.getText("gui", messageIdIfCanNotEnter));
        }
        
        return true;
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
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
        final Image image = ResourceManager.getInstance().getImage(sprite);
        graphics.drawImage(image, camera.getXCoordWrapped(x, myPlanet.getWidth()) - image.getWidth() / 2, camera.getYCoordWrapped(y, myPlanet.getHeight()) - image.getHeight() / 2);
    }

    @Override
    public void update(GameContainer container, World world) {

    }

    @Override
    public Image getImage() {
        return ResourceManager.getInstance().getImage(sprite);
    }
}
