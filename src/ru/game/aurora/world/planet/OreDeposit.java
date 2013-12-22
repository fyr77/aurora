/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 16:34
 */
package ru.game.aurora.world.planet;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;

public class OreDeposit extends BasePositionable implements PlanetObject {
    private static final long serialVersionUID = -407383614466292775L;

    public static class OreUnit implements InventoryItem {
        private static final long serialVersionUID = -5597582503966190176L;
        private OreType type;

        public OreUnit(OreType type) {
            this.type = type;
        }

        @Override
        public String getName() {
            return type.name();
        }

        @Override
        public Image getImage() {
            return ResourceManager.getInstance().getImage(type.getSpriteName());
        }

        @Override
        public void onReturnToShip(World world, int amount) {
            final int resAmount = amount * type.getResCountForUnit();
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.resources_transfer"), amount, type.name(), resAmount));
            world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() + resAmount);
        }

        @Override
        public boolean isDumpable() {
            return true;
        }

        @Override
        public boolean isUsable() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OreUnit oreUnit = (OreUnit) o;

            return type == oreUnit.type;

        }

        @Override
        public int hashCode() {
            return type != null ? type.hashCode() : 0;
        }
    }

    public static enum OreType {
        IRON(1, 3, "iron_deposit"),
        GOLD(3, 3, "gold_deposit");

        private final int resCountForUnit;

        private final int mineTime;

        private final String spriteName;

        OreType(int resCountForUnit, int mineTime, String spriteName) {
            this.resCountForUnit = resCountForUnit;
            this.mineTime = mineTime;
            this.spriteName = spriteName;
        }

        public int getResCountForUnit() {
            return resCountForUnit;
        }

        public int getMineTime() {
            return mineTime;
        }

        public String getSpriteName() {
            return spriteName;
        }
    }

    private OreType type;

    private int currentMiningProgress;

    private int amount;

    private Planet myPlanet;


    public OreDeposit(Planet myPlanet, int x, int y, OreType type, int amount) {
        super(x, y);
        this.myPlanet = myPlanet;
        this.currentMiningProgress = type.getMineTime();
        this.type = type;
        this.amount = amount;
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
    public void onShotAt(int damage) {

    }

    @Override
    public void onPickedUp(World world) {
        final int miningPower = world.getPlayer().getLandingParty().calcMiningPower();
        currentMiningProgress -= miningPower;
        if (currentMiningProgress <= 0) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.ore.mined") + " " + getName());
            currentMiningProgress = type.getMineTime();
            world.getPlayer().getLandingParty().pickUp(new OreUnit(type), 1);
            amount--;
            if (amount == 0) {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.ore.depleted"));
            }
        } else {
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.ore.mining"), getName(), miningPower, currentMiningProgress));
        }
    }

    @Override
    public boolean isAlive() {
        return amount > 0;
    }

    @Override
    public String getName() {
        return type.name();
    }

    @Override
    public void printStatusInfo() {
    }

    @Override
    public void update(GameContainer container, World world) {

    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        g.drawImage(ResourceManager.getInstance().getImage(type.getSpriteName()), camera.getXCoordWrapped(x, myPlanet.getWidth()), camera.getYCoordWrapped(y, myPlanet.getHeight()));
    }
}
