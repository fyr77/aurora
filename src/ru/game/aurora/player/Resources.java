package ru.game.aurora.player;

import org.newdawn.slick.Image;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;

/**
 */

public enum Resources implements InventoryItem {
    RU("RU") {
        @Override
        public Image getImage() {
            return ResourceManager.getInstance().getImage("ru_icon");
        }

        @Override
        public double getPrice() {
            return 0;
        }

        @Override
        public void onReturnToShip(World world, int amount) {
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
            return 0;
        }
    },
    CREDITS(Localization.getText("gui", "credits")) {
        @Override
        public Image getImage() {
            return ResourceManager.getInstance().getImage("credits_icon");
        }

        @Override
        public void onReturnToShip(World world, int amount) {

        }

        @Override
        public double getPrice() {
            return 0;
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
            return 0;
        }
    },
    CELLS_FROM_PARALLEL_WORLD(Localization.getText("journal", "inside.bio_remains_name")) {
        @Override
        public Image getImage() {
            return ResourceManager.getInstance().getImage("bio_remains");
        }

        @Override
        public void onReturnToShip(World world, int amount) {

        }

        @Override
        public double getPrice() {
            return 0;
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
            return 0;
        }
    };

    private final String name;

    Resources(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
