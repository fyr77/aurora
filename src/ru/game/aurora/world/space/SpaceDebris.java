package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.World;

/**
 * Space debris are left after a ship is destroyed
 */
public class SpaceDebris extends BaseSpaceObject {
    public static class ResourceDebris extends BaseSpaceObject {

        private static final long serialVersionUID = -7839504515681696314L;

        private int amount;

        public ResourceDebris(int amount) {
            super(0, 0);
            this.amount = amount;
        }

        @Override
        public void onContact(World world) {
            world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() + amount);
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "space.debris.message"), amount));
        }
    }

    private static final long serialVersionUID = 7779753986433190967L;

    private ProbabilitySet<SpaceObject> dropList;

    private boolean pickedUp = false;

    public SpaceDebris(int x, int y, ProbabilitySet<SpaceObject> dropList) {
        super(x, y);
        this.dropList = dropList;
    }

    @Override
    public void onContact(World world) {
        pickedUp = true;
        SpaceObject loot = dropList.getRandom();
        loot.onContact(world);
    }

    @Override
    public void onAttack(World world, SpaceObject attacker, int dmg) {

    }

    @Override
    public boolean isAlive() {
        return !pickedUp;
    }

    @Override
    public String getName() {
        return Localization.getText("gui", "space.debris.name");
    }

    @Override
    public String getScanDescription(World world) {
        return Localization.getText("gui", "space.debris.desc");
    }

    @Override
    public AlienRace getRace() {
        return null;
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        graphics.drawImage(ResourceManager.getInstance().getImage("debris"), camera.getXCoord(x), camera.getYCoord(y));
    }

    @Override
    public void update(GameContainer container, World world) {

    }
}
