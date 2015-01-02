/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 16:34
 */
package ru.game.aurora.world.planet;

import org.newdawn.slick.Image;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.ScanGroup;
import ru.game.aurora.world.World;

public class OreDeposit extends BaseGameObject {
    private static final long serialVersionUID = 2L;

    public static class OreUnit implements InventoryItem {
        private static final long serialVersionUID = -5597582503966190176L;
        private final OreType type;

        public OreUnit(OreType type) {
            this.type = type;
        }

        @Override
        public String getName() {
            return type.name();
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public Image getImage() {
            return ResourceManager.getInstance().getImage(type.getSpriteName());
        }

        @Override
        public double getPrice() {
            return 0;
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
        public int getWeight() {
            return 1;
        }

        @Override
        public boolean canBeSoldTo(Faction faction) {
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

    private final OreType type;

    private int currentMiningProgress;

    private int amount;

    public OreDeposit(Planet myPlanet, int x, int y, OreType type, int amount) {
        super(x, y, type.getSpriteName());
        this.currentMiningProgress = type.getMineTime();
        this.type = type;
        this.amount = amount;
    }


    @Override
    public boolean canBeInteracted() {
        return true;
    }

    @Override
    public ScanGroup getScanGroup() {
        return ScanGroup.RESOURCE;
    }


    @Override
    public void interact(World world) {
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

}
