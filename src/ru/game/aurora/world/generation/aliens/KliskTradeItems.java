package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.player.Resources;
import ru.game.aurora.player.SellOnlyInventoryItem;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.player.research.BaseResearchWithFixedProgress;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;

/**
 * Items that can be purchased from klisk
 */
public class KliskTradeItems
{
    public static class AdvancedRadarsSellItem extends SellOnlyInventoryItem
    {
        public AdvancedRadarsSellItem()
        {
            super( "items"
                    , "advanced_radar_tech"
                    , new Drawable("antenna_module")
                    , 15
                    , true);
        }

        @Override
        public void onReceived(World world, int amount) {
            world.getPlayer().getEarthState().getAvailableUpgrades().add(new ShipUpgrade("advanced_radar", "antenna_module", "upgrades") {

                @Override
                public void onInstalled(World world, Ship ship) {
                    ship.changeRangeBuff(1);
                }

                @Override
                public void onRemoved(World world, Ship ship) {
                    ship.changeRangeBuff(-1);
                }

                @Override
                public int getSpace() {
                    return 10;
                }
            });
        }
    }

    public static class ResourceSellItem extends SellOnlyInventoryItem
    {
        public ResourceSellItem() {
            super("items", "resources_sell", new Drawable("ru_icon"), 1, false);
        }

        @Override
        public void onReceived(World world, int amount) {
            world.getPlayer().changeResource(world, Resources.RU, Configuration.getIntProperty("trade.resources_per_credit"));
        }
    }

    public static class AlienAlloysSellItem extends SellOnlyInventoryItem
    {

        public AlienAlloysSellItem() {
            super("items", "alien_materials_tech", new Drawable("hull_module"), 20, true);
        }

        @Override
        public void onReceived(World world, int amount) {
            world.getPlayer().getResearchState().addNewAvailableProject(world.getResearchAndDevelopmentProjects().getResearchProjects().remove("loot.materials"));
        }

        @Override
        public boolean canBeSoldTo(World world, Faction faction) {
            return world.getResearchAndDevelopmentProjects().getResearchProjects().containsKey("loot.materials");
        }
    }

    public static class ScienceTheorySellItem extends SellOnlyInventoryItem
    {

        private static final long serialVersionUID = 1L;

        final String name;

        public ScienceTheorySellItem(String name)
        {
            super("research", "alien_" + name, name + "_theory", Configuration.getDoubleProperty("trade.theory_price"), true);
            this.name = name;
        }

        @Override
        public void onReceived(World world, int amount) {
            world.getPlayer().getResearchState().getCompletedProjects().add(new BaseResearchWithFixedProgress(name, "technology_research", 0, 150));
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/crew/gordon/science_theories/gordon_science_theories_" + name + ".json"));
            world.getPlayer().getShip().getCrewMembers().get("gordon").changeReputation(1);
        }
    }
}
