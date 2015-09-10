package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;

/**
 * Space debris are left after a ship is destroyed
 */
public class SpaceDebris extends BaseGameObject {
    private static final long serialVersionUID = 7779753986433190967L;
    private final GameObject dropItem;

    public SpaceDebris(int x, int y, ProbabilitySet<GameObject> dropList) {
        this(x, y, "debris", dropList);
    }

    public SpaceDebris(int x, int y, String imageId, ProbabilitySet<GameObject> dropList) {
        super(x, y, new Drawable(imageId));
        this.dropItem = dropList.getRandom();
    }

    public SpaceDebris(int x, int y, String imageId, GameObject dropItem) {
        super(x, y, imageId);
        this.dropItem = dropItem;
    }

    @Override
    public boolean interact(World world) {
        isAlive = false;
        dropItem.interact(world);

        return true;
    }

    @Override
    public String getName() {
        return Localization.getText("gui", "space.debris.name");
    }

    @Override
    public String getScanDescription(World world) {
        return Localization.getText("gui", "space.debris.desc");
    }

    public static class ResourceDebris extends BaseGameObject {

        private static final long serialVersionUID = 1L;

        private final int amount;

        public ResourceDebris(int amount) {
            super(0, 0);
            this.amount = amount;
        }

        @Override
        public boolean interact(World world) {
            world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() + amount);
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "space.debris.message"), amount));

            return true;
        }
    }

    public static class ItemDebris extends BaseGameObject {

        private static final long serialVersionUID = 1L;
        private InventoryItem item;

        public ItemDebris(InventoryItem item) {
            super(0, 0);
            this.item = item;
        }

        @Override
        public String getName() {
            return item.getName();
        }

        @Override
        public boolean interact(World world) {
            isAlive = false;
            item.onReceived(world, 1);

            return true;
        }

        @Override
        public void draw(GameContainer container, Graphics g, Camera camera, World world) {
            getImage().draw(camera.getXCoord(x), camera.getYCoord(y));
        }
    }
}
