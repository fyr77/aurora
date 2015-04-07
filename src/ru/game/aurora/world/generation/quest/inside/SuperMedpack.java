package ru.game.aurora.world.generation.quest.inside;

import org.newdawn.slick.Image;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.Resources;
import ru.game.aurora.player.engineering.EngineeringProject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.UsableItem;

import java.util.Map;

/**
 * Super medpack crafted from biodata from parallel world
 */
public class SuperMedpack extends UsableItem {


    private static final long serialVersionUID = 7731141908856823318L;

    public SuperMedpack() {
        super("supermedpack");
    }

    @Override
    public String getName() {
        return Localization.getText("items", "supermedpack.name");
    }

    @Override
    public String getDescription() {
        return Localization.getText("items", "supermedpack.desc");
    }

    @Override
    public void useIt(World world, int amount) {
        super.useIt(world, amount);
        world.getPlayer().getLandingParty().resetHp(world);
    }

    @Override
    public Image getImage() {
        return ResourceManager.getInstance().getImage("super_medpack");
    }

    @Override
    public double getPrice() {
        return 0;
    }

    @Override
    public boolean isDumpable() {
        return false;
    }

    @Override
    public boolean isUsable() {
        return true;
    }

    public static class SuperMedpackCraftProject extends EngineeringProject {

        private static final long serialVersionUID = 7590398157274520538L;

        public SuperMedpackCraftProject(String id, String icon, int length) {
            super(id, icon, length);
        }

        @Override
        public Map<InventoryItem, Integer> getCost() {
            Map<InventoryItem, Integer> cost = getSimpleResourceCost(Resources.RU, 5);
            cost.put(Resources.CELLS_FROM_PARALLEL_WORLD, 1);
            return cost;
        }

        @Override
        public void onCompleted(World world) {
            world.getPlayer().getInventory().add(new SuperMedpack(), 1);
        }
    }
}
